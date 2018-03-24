package com.github.laugh.xmapper.mybatis;

import org.apache.ibatis.session.Configuration;

import java.util.regex.Matcher;

/**
 * @author yu.gao 2017-12-18 下午5:02
 */
public class SelectInLangDriver extends AbstractExtendLangDriver {

    @Override
    public Class<?> getSetKey() {
        return SelectInLangDriver.class;
    }

    @Override
    public String createSqlWithModel(Configuration configuration, String script, Class<?> model) {
        Matcher matcher = modelPattern.matcher(script);
        if (matcher.find()) {
            script = matcher.replaceAll("<foreach collection=\"list\" item=\"_item\" open=\"(\" " +
                    "separator=\",\" close=\")\" >#{_item}</foreach>");
        }
        script = "<script>" + script + "</script>";
        return script;
    }

}