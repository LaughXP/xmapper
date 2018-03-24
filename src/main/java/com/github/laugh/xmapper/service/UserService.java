package com.github.laugh.xmapper.service;

import com.github.laugh.xmapper.entity.User;
import com.github.laugh.xmapper.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yu.gao 2018-03-24 下午3:53
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public void insert(User user) {
        userMapper.insertSelective(user);
    }

    public void insertBatch(List<User> users) {
        userMapper.insertBatch(users);
    }

    public User selectSelective(User user) {
        return userMapper.selectOneSelective(user);
    }

    public List<User> selectListSelective(User user) {
        return userMapper.selectListSelective(user);
    }

    public int updateByIdSelective(User user) {
        return userMapper.updateByIdSelective(user);
    }


    public int updateByUserNoSelective(User user) {
        return userMapper.updateByUserNoSelective(user);
    }

    public List<User> selectInUserNoList(List<Long> record) {
        return userMapper.selectInUserNoList(record);
    }

    public User selectByNameAndAge(String userName, Integer age) {
        return userMapper.selectByNameAndAge(userName, age);
    }
}
