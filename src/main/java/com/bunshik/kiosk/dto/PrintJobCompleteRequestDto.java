package com.bunshik.kiosk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintJobCompleteRequestDto {

    @NotBlank(message = "출력 결과가 필요합니다.")
    private String result;
}