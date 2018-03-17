package com.gaoyu.xmapper.mybatis;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yu.gao 2017-12-19 下午8:18
 */
public abstract class AbstractExtendLangDriver extends XMLLanguageDriver implements LanguageDriver {

    public final Pattern tablePattern = Pattern.compile("#table");

    public final Pattern resultMapPattern = Pattern.compile("#resultMap");

    public final Pattern modelPattern = Pattern.compile("#field");

    public final Pattern updateSkipPattern = Pattern.compile("#\\{\\w+}");

    private final Set<Class<?>> knownMappers = new HashSet<>();

    private volatile Class<?> mapper;

    private static final Map<Class<?>,Set<Class<?>>> mapperLangContainer = new HashMap<>();

    @Override
    public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
        setMapper(configuration);
        String table = getTable();
        Class<?> model = getModel();
        script = replaceResultMap(script, model);
        script = createSqlWithModel(configuration, script, model);
        script = tablePattern.matcher(script).replaceAll(table);
        return super.createSqlSource(configuration, script, parameterType);
    }

    private String replaceResultMap(String script, Class<?> model) {
        Matcher resultMapMatcher = resultMapPattern.matcher(script);
        if(resultMapMatcher.find()) {
            StringBuilder filedString = new StringBuilder();
            for (Field field : FieldUtils.getAllFields(model)) {
                filedString.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName())).append(" as ").append(field.getName()).append(",");
            }
            filedString.deleteCharAt(filedString.lastIndexOf(","));
            script = resultMapMatcher.replaceAll(filedString.toString());
        }
        return script;
    }

    private void setMapper(Configuration configuration) {
        MapperRegistry mapperRegistry = configuration.getMapperRegistry();
        Collection<Class<?>> mappers = mapperRegistry.getMappers();
        mappers.forEach(clazz ->{
            Set<Class<?>> langSet = mapperLangContainer.get(clazz);
            if(langSet == null ) {
                Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
                langSet = Stream.of(methods).map(method -> {
                    Lang lang = method.getAnnotation(Lang.class);
                    return lang != null ? lang.value() : null;
                }).filter(Objects::nonNull).collect(Collectors.toSet());
                mapperLangContainer.put(clazz, langSet);
            }
            if(!knownMappers.contains(clazz) && langSet.contains(getSetKey())) {
                this.mapper = clazz;
                this.knownMappers.add(clazz);
            }
        });
    }

    public abstract Class<?> getSetKey();

    public String getTable() {
        return this.mapper.getAnnotation(MapperExtend.class).table();
    }

    public Class<?> getModel() {
        return this.mapper.getAnnotation(MapperExtend.class).model();
    }

    public abstract String createSqlWithModel(Configuration configuration, String script, Class<?> model);
}
