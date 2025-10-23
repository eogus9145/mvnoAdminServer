package telecentro.login.dto;

import lombok.Data;
import telecentro.login.dto.AuthRequestDto;

@Data
public class AuthRequestDto {
    private String id;
	private String authNo;
}