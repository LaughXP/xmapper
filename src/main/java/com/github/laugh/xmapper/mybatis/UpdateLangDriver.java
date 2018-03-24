package com.github.laugh.xmapper.mybatis;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author yu.gao 2017-12-18 下午5:06
 */
public class UpdateLangDriver extends AbstractExtendLangDriver {

    @Override
    public Class<?> getSetKey() {
        return UpdateLangDriver.class;
    }

    @Override
    public String createSqlWithModel(Configuration configuration, String script, Class<?> model) {
        Matcher matcher = modelPattern.matcher(script);
        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            sb.append("<set>");

            Set<String> set = collectSkipField(script);
            String updateFieldPattern = "#{%s}";

            for (Field field : FieldUtils.getAllFields(model)) {
                if("id".equals(field.getName())) {
                    continue;
                }
                if(set.contains(String.format(updateFieldPattern, field.getName()))) {
                    continue;
                }
                if (!field.isAnnotationPresent(Invisible.class)) {
                    String tmp = "<if test=\"_field != null\">_column=#{_field},</if>";
                    sb.append(tmp.replaceAll("_field", field.getName()).replaceAll("_column",
                            CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName())));
                }
            }

            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("</set>");

            script = matcher.replaceAll(sb.toString());
            script = "<script>" + script + "</script>";
        }

        return script;
    }

    private Set<String> collectSkipField(String script) {
        Matcher updateSkipMatcher = updateSkipPattern.matcher(script);
        Set<String> set = new HashSet<>();
        while (updateSkipMatcher.find()) {
            set.add(updateSkipMatcher.group());
        }
        return set;
    }
}
