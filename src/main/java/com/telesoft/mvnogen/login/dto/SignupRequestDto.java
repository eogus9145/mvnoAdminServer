package com.telesoft.mvnogen.login.dto;

import lombok.Data;

@Data
public class SignupRequestDto {
    private String id;
    private String pw;
    private String pwConfirm;
    private String name;
    private String email;
}