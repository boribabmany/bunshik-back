package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SetMenuComponentsRequestDto {

    private List<Long> componentMenuIds = new ArrayList<>();
}
