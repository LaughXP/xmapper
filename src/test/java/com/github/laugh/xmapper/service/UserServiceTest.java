package com.github.laugh.xmapper.service;

import com.github.laugh.xmapper.entity.User;
import com.github.pagehelper.PageInfo;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author yu.gao 2018-03-24 下午4:06
 */
@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL)
public class UserServiceTest extends H2TestExecutionListener {

    @Autowired
    private UserService userService;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    @ExpectedDatabase(value = "classpath:user_insert_selective_end.xml", connection = BaseTestExecutionListener.DATA_SOURCE,assertionMode= DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void insert() {
        userService.insert(User.builder().userNo(123L).userName("小红").age(18).build());
    }

    @Test
    @ExpectedDatabase(value = "classpath:user_insert_batch_end.xml", connection = BaseTestExecutionListener.DATA_SOURCE,assertionMode= DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void insertBatch() {
        userService.insertBatch(ImmutableList.of(User.builder().userNo(123L).userName("小红").age(18).build(),User.builder().userNo(124L).userName("小明").age(20).build()));
    }

    @Test
    @DatabaseSetup(value = "classpath:user_select_one_sta.xml", connection = BaseTestExecutionListener.DATA_SOURCE, type = DatabaseOperation.CLEAN_INSERT)
    public void selectSelective() {
        User user = userService.selectSelective(User.builder().userNo(123L).build());
        compareAndPrintSkip(User.builder().userNo(123L).userName("小红").age(18).build(), user);
    }

    @Test
    @DatabaseSetup(value = "classpath:user_select_list_sta.xml", connection = BaseTestExecutionListener.DATA_SOURCE, type = DatabaseOperation.CLEAN_INSERT)
    public void selectListSelective() {
        List<User> users = userService.selectListSelective(User.builder().age(18).build());
        compareAndPrintSkip(ImmutableList.of(User.builder().userNo(123L).userName("小红").age(18).build(),User.builder().userNo(124L).userName("小明").age(18).build()), users);
    }

    @Test
    @DatabaseSetup(value = "classpath:user_update_by_id_sta.xml", connection = BaseTestExecutionListener.DATA_SOURCE, type = DatabaseOperation.CLEAN_INSERT)
    @ExpectedDatabase(value = "classpath:user_update_by_id_end.xml", connection = BaseTestExecutionListener.DATA_SOURCE,assertionMode= DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void updateByIdSelective() {
        userService.updateByIdSelective(User.builder().id(1L).age(200).build());
    }

    @Test
    @DatabaseSetup(value = "classpath:user_update_by_userno_sta.xml", connection = BaseTestExecutionListener.DATA_SOURCE, type = DatabaseOperation.CLEAN_INSERT)
    @ExpectedDatabase(value = "classpath:user_update_by_userno_end.xml", connection = BaseTestExecutionListener.DATA_SOURCE,assertionMode= DatabaseAssertionMode.NON_STRICT_UNORDERED)
    public void updateByUserNoSelective() {
        userService.updateByUserNoSelective(User.builder().userNo(123L).age(300).build());
    }

    @Test
    @DatabaseSetup(value = "classpath:user_select_in_userno_list_sta.xml", connection = BaseTestExecutionListener.DATA_SOURCE, type = DatabaseOperation.CLEAN_INSERT)
    public void selectInUserNoList() {
        List<User> users = userService.selectInUserNoList(ImmutableList.of(123L, 124L));
        compareAndPrintSkip(ImmutableList.of(User.builder().userNo(123L).userName("小红").age(18).build(),User.builder().userNo(124L).userName("小明").age(18).build()), users);
    }

    @Test
    @DatabaseSetup(value = "classpath:user_select_by_name_age_sta.xml", connection = BaseTestExecutionListener.DATA_SOURCE, type = DatabaseOperation.CLEAN_INSERT)
    public void selectByNameAndAge() {
        User user = userService.selectByNameAndAge("小明",18);
        compareAndPrintSkip(User.builder().userNo(124L).userName("小明").age(18).build(), user);
    }

    @Test
    @DatabaseSetup(value = "classpath:user_select_page_sta.xml", connection = BaseTestExecutionListener.DATA_SOURCE, type = DatabaseOperation.CLEAN_INSERT)
    public void selectPage() {
        PageInfo<User> page = userService.selectPage(User.builder().age(18).build(), 1, 2);
        System.out.println(page.getList());
        page = userService.selectPage(User.builder().age(18).build(), 2, 2);
        System.out.println(page.getList());
    }
}