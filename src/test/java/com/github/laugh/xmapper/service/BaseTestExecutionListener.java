package com.github.laugh.xmapper.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.runner.RunWith;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * -ea -XX:PermSize=128m
 * @author yu.gao 2017-11-09 下午9:19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/*.xml"})
@TestExecutionListeners({H2TestExecutionListener.class, DbUnitTestExecutionListener.class})
@DbUnitConfiguration(databaseConnection  = {BaseTestExecutionListener.DATA_SOURCE})
public abstract class BaseTestExecutionListener extends DependencyInjectionTestExecutionListener {

    //spring-jdbc中配置的数据源
    public static final String DATA_SOURCE = "master";

    public static final String DATA_TRANS = "transactionManager";

    public AtomicBoolean afterFirst = new AtomicBoolean(false);

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        String[] databaseConnectionBeanNames = null;
        DbUnitConfiguration configuration = testContext.getTestClass().getAnnotation(DbUnitConfiguration.class);
        if (configuration != null) {
            databaseConnectionBeanNames = configuration.databaseConnection();
        }
        String userTableSql = "CREATE TABLE `t_user` (\n" +
                "  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',\n" +
                "  `user_no` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '用户编号',\n" +
                "  `user_name` varchar(255) DEFAULT NULL COMMENT '用户姓名',\n" +
                "  `age` int(11) DEFAULT '0' COMMENT '用户年龄',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  KEY `user_no` (`user_no`) USING BTREE,\n" +
                ")AUTO_INCREMENT=1";
        DruidDataSource druidDataSource = null;
        List<String> tableSql = ImmutableList.of(userTableSql);
        if(databaseConnectionBeanNames != null) {
            for (int i = 0; i < databaseConnectionBeanNames.length; i++) {
                if(afterFirst.get()) {
                   continue;
                }
                String dataSourceBeanName = databaseConnectionBeanNames[i];
                GenericApplicationContext applicationContext = (GenericApplicationContext)testContext.getApplicationContext();
                druidDataSource = (DruidDataSource)applicationContext.getBean(dataSourceBeanName);
            }
            if(!afterFirst.getAndSet(true)) {
                createTable(tableSql, druidDataSource);
            }

        }

        super.prepareTestInstance(testContext);
    }

    private List<String> genTableSql(String[] tableName, DataSource dataSource) throws Exception {
        Connection connection = dataSource.getConnection();
        Statement stat = connection.createStatement();
        List<String> tableSqlAll = new ArrayList<String>();
        for(String single : tableName) {
            stat.execute("show create table " + single);
            ResultSet rs = stat.getResultSet();
            String tableSql = null;
            while (rs.next()) {
                tableSql = rs.getString(rs.getMetaData().getColumnName(2));
            }
            //格式化到h2能识别的sql
            tableSql = StringUtils.replace(tableSql, "ON UPDATE CURRENT_TIMESTAMP ", "");
            tableSql = StringUtils.replace(tableSql, "double", "decimal");
            String[] splitArray = StringUtils.split(tableSql,"\n");
            Preconditions.checkNotNull(splitArray);
            //重命名key,如果是多张表，相同的key会报重名错误，在原来的key名字前面加上表名字
            String uniqueKeySplit = "  UNIQUE KEY `";
            String keySplit = "  KEY `";
            String[] finalSplitArray = new String[splitArray.length];
            for(int i =0;i<splitArray.length;i++) {
                String split = splitArray[i];
                if(StringUtils.startsWith(split, keySplit)) {
                    String[] tmp = StringUtils.splitByWholeSeparator(split, keySplit);
                    Preconditions.checkNotNull(tmp);
                    finalSplitArray[i] =  Joiner.on(single).join(keySplit,tmp[0]);
                } else if(StringUtils.startsWith(split, uniqueKeySplit)) {
                    String[] tmp = StringUtils.splitByWholeSeparator(split, uniqueKeySplit);
                    Preconditions.checkNotNull(tmp);
                    finalSplitArray[i] =  Joiner.on(single).join(uniqueKeySplit,tmp[0]);
                } else {
                    finalSplitArray[i] = splitArray[i];
                }
            }
            tableSql = Joiner.on("\n").join(ArrayUtils.subarray(finalSplitArray,0, finalSplitArray.length -1));
            tableSql += ")AUTO_INCREMENT=1;";
            tableSqlAll.add(tableSql);
            rs.close();
        }
        stat.close();
//        System.out.println(tableSqlAll);
        return tableSqlAll;
    }

    private void createTable(List<String> tableSqlAll,DataSource dataSource) throws Exception {
        Connection connection = dataSource.getConnection();
        for(String tableSql : tableSqlAll) {
            Statement stat = connection.createStatement();
            stat.execute(tableSql);
            stat.close();
        }
        connection.close();
    }

    public <T> void forPrint(List<T> list) {
        System.out.println("==========================");
        for(T t : list) {
            System.out.println(t);
        }
        System.out.println("==========================");
    }

    public <T> void print(T t) {
        System.out.println("==========================");
        System.out.println(t);
        System.out.println("==========================");
    }

    public void compareAndPrintSkip(Object expect, Object res) {
        System.out.println("==========================");
        Table<Object, Object, Integer> table = null;
        try {
            table = compare(expect, res, ImmutableSet.of("id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("expect:      "+expect);
        System.out.println("mockres:     "+res);
        System.out.println(table == null ? "expect equal mockers" : table);
        MatcherAssert.assertThat(table, Matchers.nullValue());
        System.out.println("==========================");
    }

    public void compareAndPrint(Object expect, Object res) {
        System.out.println("==========================");
        Table<Object, Object, Integer> table = null;
        try {
            table = compare(expect, res, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("expect:      "+expect);
        System.out.println("mockres:     "+res);
        System.out.println(table == null ? "expect equal mockers" : table);
        MatcherAssert.assertThat(table, Matchers.nullValue());
        System.out.println("==========================");
    }

    private Table<Object, Object, Integer> compare(Object expect, Object res, Set<String> skipField) throws Exception {
        if(expect == null && res == null) {
            return null;
        } else if(expect == null || res == null) {
            Table<Object, Object, Integer> table = HashBasedTable.create();
            table.put(new Object(), new Object(), 1);
            return table;
        }
        Table<Object, Object, Integer> table = HashBasedTable.create();
        if(expect instanceof Collection) {
            Collection sourceCollection = (Collection) expect;
            Collection targetCollection = (Collection) res;
            int allSize = sourceCollection.size();
            Set<Object> alreadyEqualSet = new HashSet<Object>();
            for (Object oSource : sourceCollection) {
                boolean isEqual = false;
                Table<Object, Object, Integer> tmpTableOne = HashBasedTable.create();
                for(Object oTarget : targetCollection) {
                    Table<Object, Object, Integer> tmpTableTwo = compare(oSource, oTarget, skipField);
                    if( tmpTableTwo == null) {
                        allSize--;
                        isEqual = true;
                        alreadyEqualSet.add(oTarget);
                        break;
                    } else {
                        tmpTableOne.put(oSource, oTarget, tmpTableTwo.get(oSource, oTarget));
                    }
                }
                if(!isEqual) {
                    table.putAll(tmpTableOne);
                }
            }
            return 0 == allSize ? null : clearData(table, alreadyEqualSet);
        } else if(expect instanceof Map) {
//            Map sourceMap = (Map) expect;
//            Map targetMap = (Map) res;
//            for(Object entryObject : sourceMap.entrySet()) {
//                Map.Entry entry = (Map.Entry) entryObject;
//            }
//            return table;
            throw new UnsupportedOperationException("不支持Map属性");
        } else {
            if(!expect.getClass().getName().equals(res.getClass().getName())) {
                return null;
            }
            Class clazz = expect.getClass();
            Field[] fields = clazz.getDeclaredFields();
            if (fields == null || fields.length == 0) {
                return null;
            }
            Integer diffFields = 0;
            for (Field field : fields) {
                if(CollectionUtils.isNotEmpty(skipField) && skipField.contains(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                Object sourceVal = field.get(expect);
                Object targetVal = field.get(res);
                if(sourceVal == null && targetVal == null) {
                    continue;
                }
                if(sourceVal == null || targetVal == null){
                    diffFields++;
                } else {
                    if(isWrapClass(sourceVal.getClass()) && isWrapClass(targetVal.getClass())) {
                        if(!sourceVal.equals(targetVal)) {
                            System.out.println("different field :" + field);
                            diffFields++;
                        }
                    } else if(sourceVal instanceof String && targetVal instanceof String) {
                        if(!sourceVal.equals(targetVal)) {
                            System.out.println("different field :" + field);
                            diffFields++;
                        }
                    }  else if(sourceVal instanceof BigDecimal && targetVal instanceof BigDecimal) {
                        if(!sourceVal.equals(targetVal)) {
                            System.out.println("different field :" + field);
                            diffFields++;
                        }
                    } else if(sourceVal instanceof Enum && targetVal instanceof Enum) {
                        if(!sourceVal.equals(targetVal)) {
                            System.out.println("different field :" + field);
                            diffFields++;
                        }
                    } else{
                        Table<Object, Object, Integer> result = compare(field.get(expect), field.get(res), skipField);
                        if(result != null) {
                            diffFields++;
                            System.out.println("different field :" + field);
                        }
                    }
                }
            }
            if(diffFields > 0) {
                table.put(expect, res, diffFields);
                return table;
            } else {
                return null;
            }
        }
    }

    private Table<Object, Object, Integer> clearData(Table<Object, Object, Integer> table, Set<Object> alreadyEqualSet) {
        Table<Object, Object, Integer> alreadyCLearTable = HashBasedTable.create();
        Map<Object, Map<Object, Integer>> rowMap =table.rowMap();
        Set<Object> alreadyNotEqualSet = new HashSet<Object>();
        for(Map.Entry<Object, Map<Object, Integer>> entryRow : rowMap.entrySet()) {
            Map<Object, Integer> rowMapVal = entryRow.getValue();
            Integer max = Integer.MAX_VALUE;
            Map.Entry<Object, Integer> maxEntry = null;
            for(Map.Entry<Object, Integer> entry : rowMapVal.entrySet()) {
                if(entry.getValue() < max && !alreadyEqualSet.contains(entry.getKey()) && !alreadyNotEqualSet.contains(entry.getKey())) {
                    maxEntry = entry;
                    max = maxEntry.getValue();
                }
            }
            if(maxEntry != null) {
                alreadyCLearTable.put(entryRow.getKey(), maxEntry.getKey(), maxEntry.getValue());
                alreadyNotEqualSet.add(maxEntry.getKey());
            }
        }
        return alreadyCLearTable;
    }

    private boolean isWrapClass(Class clz) {
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }


}
