package com.github.laugh.xmapper.mapper;

import com.github.laugh.xmapper.mybatis.InsertBatchLangDriver;
import com.github.laugh.xmapper.mybatis.InsertLangDriver;
import com.github.laugh.xmapper.mybatis.SelectLangDriver;
import com.github.laugh.xmapper.mybatis.UpdateLangDriver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author gaoyu
 *
 * 分页查询推荐您使用PageHelper
 */
public interface BaseMapper<T> {

    /**
     * 插入数据, NULL值不插入
     * @param record 记录
     * @return id
     */
    @Insert("insert into #table #field")
    @Lang(InsertLangDriver.class)
    int insertSelective(T record);

    /**
     * 批量插入数据, NULL值同样插入
     * @param list 数据集合
     */
    @Insert("insert into #table #field")
    @Lang(InsertBatchLangDriver.class)
    void insertBatch(List<T> list);

    /**
     * 查询一个, NULL值不做查询字段
     * @return 账单
     */
    @Select("select #resultMap from #table #field")
    @Lang(SelectLangDriver.class)
    T selectOneSelective(T record);

    /**
     * 查询多个, NULL值不做查询字段
     * @return 账单
     */
    @Select("select #resultMap from #table #field")
    @Lang(SelectLangDriver.class)
    List<T> selectListSelective(T record);

    /**
     * 根据主键更新, NULL值不插入
     */
    @Update("update #table #field where id = #{id}")
    @Lang(UpdateLangDriver.class)
    int updateByIdSelective(T record);
}