package telecentro.common.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CommonResponseDto {
    private final String cd;
    private String msg;
    private Object data;
}