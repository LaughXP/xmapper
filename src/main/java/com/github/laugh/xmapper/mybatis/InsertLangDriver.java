package com.github.laugh.xmapper.mybatis;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.regex.Matcher;

/**
 * @author yu.gao 2017-12-18 下午4:02
 */
public class InsertLangDriver extends AbstractExtendLangDriver {

    @Override
    public Class<?> getSetKey() {
        return InsertLangDriver.class;
    }

    @Override
    public String createSqlWithModel(Configuration configuration, String script, Class<?> model) {
        Matcher matcher = modelPattern.matcher(script);
        if (matcher.find()) {
            StringBuilder key = new StringBuilder();
            StringBuilder value = new StringBuilder();
            key.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            for (Field field : FieldUtils.getAllFields(model)) {
                if("id".equals(field.getName())) {
                    continue;
                }
                if (!field.isAnnotationPresent(Invisible.class)) {
                    String tmpPatternKey = "<if test=\"_field != null\">_column,</if>";
                    String tmpPatternVal = "<if test=\"_field != null\">#{_field},</if>";
                    key.append(
                            tmpPatternKey.replaceAll("_field", field.getName())
                                    .replaceAll("_column", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()))
                    );
                    value.append(tmpPatternVal.replaceAll("_field", field.getName())
                            .replaceAll("_column", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()))
                    );
                }
            }
            key.append("</trim> <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">").append(value.toString()).append("</trim>");
            script = matcher.replaceAll(key.toString());
            script = "<script>" + script + "</script>";
        }
        return script;
    }

}
