package com.bunshik.admin.mappers;

import com.bunshik.common.entity.Option;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminOptionMapper {

    List<Option> findAll();

    Option findById(@Param("optionId") Long optionId);

    int insert(Option option);

    int update(Option option);

    int delete(@Param("optionId") Long optionId);
}