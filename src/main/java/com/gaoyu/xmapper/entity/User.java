package com.gaoyu.xmapper.entity;

import lombok.*;

/**
 * @author yu.gao 2018-03-17 下午10:05
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User {

    private Long id;

    private Long userNo;

    private String userName;

    private Integer age;
}
