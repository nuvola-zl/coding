package com.zhanglin.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhanglin.homepage.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 文章 Mapper
 *
 * @author 张林
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 阅读量 +1。
     *
     * <p>用 SQL 的 {@code view_count = view_count + 1} 而不是「查出来 +1 再写回」：
     * 后者在并发下会丢更新（两个人同时读到 10，各自写回 11，实际应该是 12）。
     * 数据库做自增是原子操作，不需要加锁。</p>
     *
     * @return 影响行数
     */
    @Update("UPDATE article SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementViewCount(@Param("id") Long id);

}
