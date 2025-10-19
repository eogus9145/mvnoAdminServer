package com.telesoft.mvnogen.login.dto;

import com.telesoft.mvnogen.login.dto.LoginRequestDto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String id;
    private String pw;
	private String ip;
	private String serverName;
	private String adminName;
	private String browser;
	private String adminType;
}