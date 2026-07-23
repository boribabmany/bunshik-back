package com.bunshik.admin.mappers;

import com.bunshik.common.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminAuthMapper {

    AdminUser findByUsername(String username);

}