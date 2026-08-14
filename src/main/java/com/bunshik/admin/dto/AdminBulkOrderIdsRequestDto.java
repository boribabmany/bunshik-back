package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminBulkOrderIdsRequestDto {
    private List<Integer> orderIds;
}
