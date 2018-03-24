package com.gaoyu.xmapper.mapper;

import com.gaoyu.xmapper.entity.User;
import com.gaoyu.xmapper.mybatis.MapperExtend;
import com.gaoyu.xmapper.mybatis.SelectInLangDriver;
import com.gaoyu.xmapper.mybatis.SelectLangDriver;
import com.gaoyu.xmapper.mybatis.UpdateLangDriver;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author yu.gao 2018-03-17 下午10:11
 */
@MapperExtend(table = "t_user", model = User.class)
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据user_no更新，NULL值不更新
     */
    @Update("update #table #field where user_no = #{userNo}")
    @Lang(UpdateLangDriver.class)
    int updateByUserNoSelective(User record);

    @Select("select #resultMap from #table where user_no in #field")
    @Lang(SelectInLangDriver.class)
    List<User> selectInUserNoList(List<Long> record);

    @Select("select #resultMap from #table where user_name = #{userName} and age = #{age} limit 1")
    @Lang(SelectLangDriver.class)
    User selectByNameAndAge(@Param("userName")String userName, @Param("age")Integer age);
}
