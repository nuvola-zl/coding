package com.zhanglin.homepage.common.markdown;

import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Markdown 渲染器：Markdown 原文 → 安全的 HTML。
 *
 * <p>两件事必须一起做：</p>
 * <ol>
 *   <li><b>渲染</b>：commonmark 按 CommonMark 规范把文本转成 HTML，表格靠 gfm-tables 扩展。</li>
 *   <li><b>净化</b>：渲染出来的 HTML 不能直接丢给 {@code th:utext}。
 *       Markdown 语法本身允许内联 HTML，也就是说正文里写
 *       {@code <script>alert(1)</script>} 或 {@code <img src=x onerror=alert(1)>}
 *       会原样进入输出，最后在访客浏览器里执行——这就是存储型 XSS。
 *       这里用 OWASP 的白名单过滤器，只放行排版需要的标签和属性。</li>
 * </ol>
 *
 * <p>白名单思路：不认识的一律丢掉。黑名单（"过滤掉 script 标签"）永远漏，
 * 因为浏览器能执行的东西比你想的多（{@code on* } 事件、{@code javascript:} 协议、
 * {@code <svg><animate>}……）。</p>
 *
 * @author 张林
 */
@Component
public class MarkdownRenderer {

    /** 只允许以这些协议开头的链接，挡掉 javascript: / data: 这类伪协议 */
    private static final List<String> SAFE_URL_PROTOCOLS = List.of("http", "https", "mailto");

    /** 代码块的语言标记，如 class="language-java" */
    private static final Pattern CODE_LANGUAGE = Pattern.compile("language-[a-zA-Z0-9+#_-]{1,20}");

    /** 数字字符实体：&#60; 或 &#x3c; */
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(x?)([0-9a-fA-F]+);");

    /** HTML 语法字符 < > & " '，这几个的实体必须原样保留 */
    private static final Set<Integer> HTML_SYNTAX_CHARS = Set.of(0x26, 0x3C, 0x3E, 0x22, 0x27);

    /** 中文阅读速度经验值：300 字/分钟 */
    private static final int CHARS_PER_MINUTE = 300;

    private final Parser parser;

    private final HtmlRenderer htmlRenderer;

    private final TextContentRenderer textRenderer;

    private final PolicyFactory policy;

    public MarkdownRenderer() {
        List<org.commonmark.Extension> extensions = List.of(TablesExtension.create());
        this.parser = Parser.builder().extensions(extensions).build();
        this.htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
        this.textRenderer = TextContentRenderer.builder().build();
        this.policy = buildPolicy();
    }

    /**
     * Markdown → 净化后的 HTML，可以直接用 th:utext 输出
     */
    public String render(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return "";
        }
        Node document = parser.parse(markdown);
        return decodeNonAsciiEntities(policy.sanitize(htmlRenderer.render(document)));
    }

    /**
     * Markdown → 纯文本。
     *
     * <p>注意不是「渲染成 HTML 再用正则去标签」——那样要处理嵌套、实体转义、注释，
     * 正则很快就会写成一团。commonmark 自带 TextContentRenderer，直接遍历语法树取文本，
     * 干净得多。摘要截取和字数统计都用它。</p>
     */
    public String toPlainText(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return "";
        }
        Node document = parser.parse(markdown);
        // 渲染器会把段落之间留空行，统一压成单个空格，方便截断
        return textRenderer.render(document).replaceAll("\\s+", " ").trim();
    }

    /**
     * 估算阅读时长：按去掉空白后的字符数算，最少 1 分钟
     */
    public int estimateReadingMinutes(String markdown) {
        String plain = toPlainText(markdown).replaceAll("\\s", "");
        if (plain.isEmpty()) {
            return 1;
        }
        return Math.max(1, (int) Math.ceil(plain.length() / (double) CHARS_PER_MINUTE));
    }

    /**
     * 从正文自动生成摘要（后台没填摘要时兜底）
     *
     * @param markdown 正文
     * @param maxChars 最大字符数
     */
    public String autoSummary(String markdown, int maxChars) {
        String plain = toPlainText(markdown);
        if (plain.length() <= maxChars) {
            return plain;
        }
        return plain.substring(0, maxChars) + "…";
    }

    // ==================== 私有方法 ====================

    /**
     * 把没必要转义的数字实体还原成原字符。
     *
     * <p>OWASP 的净化器为了最大化兼容性，会把一大票字符输出成数字实体：中文标点变成
     * {@code &#xff0c;}，连 {@code @} 都变成 {@code &#64;}。浏览器显示是正常的，但
     * 一个中文逗号从 3 字节变成 8 个字符，一篇中文文章的 HTML 能膨胀三四倍，
     * <b>而且查看网页源码时基本没法读</b>。</p>
     *
     * <p>安全边界：只还原 {@code < > & " '} 以外的字符。这五个是 HTML 的语法字符，
     * 还原出来的 {@code <} 会被浏览器当成标签开头——那就等于自己给自己开了一个
     * XSS 后门。其余字符（含所有非 ASCII、{@code @}、{@code =}）在文本节点里没有任何
     * 语法含义，还原是安全的。</p>
     */
    private String decodeNonAsciiEntities(String html) {
        return NUMERIC_ENTITY.matcher(html).replaceAll(match -> {
            int codePoint = match.group(1).isEmpty()
                    ? Integer.parseInt(match.group(2))
                    : Integer.parseInt(match.group(2), 16);
            if (codePoint < 0x20 || codePoint > Character.MAX_CODE_POINT
                    || HTML_SYNTAX_CHARS.contains(codePoint)) {
                return match.group();
            }
            return new String(Character.toChars(codePoint));
        });
    }

    private PolicyFactory buildPolicy() {
        return new HtmlPolicyBuilder()
                // ---- 排版类标签 ----
                .allowElements(
                        "p", "br", "hr",
                        "h1", "h2", "h3", "h4", "h5", "h6",
                        "ul", "ol", "li",
                        "blockquote",
                        "pre", "code",
                        "strong", "em", "del", "sub", "sup",
                        "table", "thead", "tbody", "tr", "th", "td",
                        "a", "img"
                )
                // ---- 链接：只认安全协议，并强制加 rel="nofollow" ----
                // 外链加 nofollow 是为了不让评论区/正文里的链接白嫖本站的 SEO 权重
                .allowAttributes("href", "title").onElements("a")
                .allowUrlProtocols(SAFE_URL_PROTOCOLS.toArray(String[]::new))
                .requireRelNofollowOnLinks()
                // ---- 图片：只允许 http/https 的 src ----
                .allowAttributes("src", "alt", "title").onElements("img")
                // ---- 代码块语言标记：只放行 language-xxx 这种形式 ----
                .allowAttributes("class").matching(CODE_LANGUAGE).onElements("pre", "code")
                // ---- 表格对齐 ----
                .allowAttributes("align").onElements("th", "td")
                .toFactory();
    }

}
