package com.zhanglin.homepage.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充 createTime / updateTime，业务代码里不用手写
 *
 * @author 张林
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 注意：这里不能用 strictUpdateFill。
        // 实体通常是从数据库查出来的，updateTime 已经有旧值，strictUpdateFill 会跳过；
        // 用 setFieldValByName 强制覆盖才能保证更新时间正确。
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

}
