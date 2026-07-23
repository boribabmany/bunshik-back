package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class MenuResponseDto {

    @JsonProperty("menu_id")
    private Long menuId;

    @JsonProperty("menu_name")
    private String menuName;

    private Integer price;
    private String category;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("is_available")
    private Boolean isAvailable;


}