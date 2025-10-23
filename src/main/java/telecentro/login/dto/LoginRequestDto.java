package telecentro.login.dto;

import lombok.Data;
import telecentro.login.dto.LoginRequestDto;

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