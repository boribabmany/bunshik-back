package com.bunshik.admin.mappers;

import com.bunshik.common.entity.Menu;
import com.bunshik.admin.dto.SetMenuComponentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMenuMapper {

    List<Menu> findAll();

    Menu findById(Long menuId);

    List<SetMenuComponentDto> findSetComponents(Long menuId);

    int deleteSetComponents(Long menuId);

    int insertSetComponent(
            @Param("setMenuId") Long setMenuId,
            @Param("componentMenuId") Long componentMenuId,
            @Param("selectGroup") String selectGroup,
            @Param("groupMaxSelect") Integer groupMaxSelect,
            @Param("extraPrice") Integer extraPrice
    );

    int insert(Menu menu);

    int update(Menu menu);

    // 논리 삭제
    int stopSelling(Long menuId);

    // 판매 재개
    int resumeSelling(Long menuId);
}
