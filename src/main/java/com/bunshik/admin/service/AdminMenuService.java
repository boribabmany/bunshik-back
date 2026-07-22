package com.bunshik.admin.service;

import com.bunshik.admin.mappers.MenuMapper;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final MenuMapper menuMapper;

    public List<Menu> findAll() {
        return menuMapper.findAll();
    }

}