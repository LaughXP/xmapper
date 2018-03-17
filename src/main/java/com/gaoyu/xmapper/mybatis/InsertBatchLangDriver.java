package com.gaoyu.xmapper.mybatis;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.regex.Matcher;

/**
 * @author yu.gao 2017-12-18 下午9:48
 */
public class InsertBatchLangDriver extends AbstractExtendLangDriver {

    @Override
    public Class<?> getSetKey() {
        return InsertBatchLangDriver.class;
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
                    String tmpPatternKey = "_column,";
                    String tmpPatternVal = "#{item._field},";
                    key.append(
                            tmpPatternKey.replaceAll("_field", field.getName())
                                    .replaceAll("_column", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()))
                    );
                    value.append(tmpPatternVal.replaceAll("_field", field.getName())
                            .replaceAll("_column", CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()))
                    );
                }
            }

            key.append("</trim> values <foreach item=\"item\" collection=\"list\" separator=\",\"><trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">").append(value.toString()).append("</trim></foreach>");

            script = matcher.replaceAll(key.toString());
            script = "<script>" + script + "</script>";
        }
        return script;
    }

}

