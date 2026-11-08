package com.zhanglin.homepage.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用分页查询参数，Controller 可直接用它接 ?pageNum=1&pageSize=10
 *
 * @author 张林
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 页码，从 1 开始 */
    @Min(value = 1, message = "页码最小为 1")
    private Long pageNum = 1L;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数最大为 100")
    private Long pageSize = 10L;

    /**
     * 转成 MyBatis-Plus 的分页对象
     */
    public <T> Page<T> toPage() {
        return new Page<>(pageNum == null ? 1L : pageNum, pageSize == null ? 10L : pageSize);
    }

}
