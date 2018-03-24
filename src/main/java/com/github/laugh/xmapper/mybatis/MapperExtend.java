package com.github.laugh.xmapper.mybatis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yu.gao 2017-12-18 下午4:57
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MapperExtend {
    /**
     * 数据库表名
     */
    String table();

    /**
     * 数据库表映射的实体类
     */
    Class<?> model();
}
