package org.example.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String phone;
    private String avatar;
    private String grade;
    private String major;
    private String school;
    private String region;
    private String role;
    private Integer status;
}