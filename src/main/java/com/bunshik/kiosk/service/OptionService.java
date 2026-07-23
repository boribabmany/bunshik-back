package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.OptionResponseDto;
import com.bunshik.kiosk.mapper.OptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OptionService {

    private final OptionMapper optionMapper;

    public List<OptionResponseDto> getOptions() {
        return optionMapper.findAll();
    }
}