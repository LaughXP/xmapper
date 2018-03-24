package com.github.laugh.xmapper.mybatis;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.regex.Matcher;

/**
 * @author yu.gao 2017-12-18 下午5:02
 */
public class SelectLangDriver extends AbstractExtendLangDriver {

    @Override
    public Class<?> getSetKey() {
        return SelectLangDriver.class;
    }

    @Override
    public String createSqlWithModel(Configuration configuration, String script, Class<?> model) {

        Matcher modelMatcher = modelPattern.matcher(script);
        if (modelMatcher.find()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<where>");

            for (Field field : FieldUtils.getAllFields(model)) {
                if (!field.isAnnotationPresent(Invisible.class)) {
                    String tmp = "<if test=\"_field != null\"> AND _column=#{_field}</if>";
                    sb.append(tmp.replaceAll("_field", field.getName()).replaceAll("_column",
                            CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName())));
                }
            }
            sb.append("</where> order by id DESC ");
            script = modelMatcher.replaceAll(sb.toString());
        }
        script = "<script>" + script + "</script>";
        return script;
    }
}