package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.OptionResponseDto;
import com.bunshik.kiosk.mapper.OptionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptionServiceTest {

    @Mock
    private OptionMapper optionMapper;

    @InjectMocks
    private OptionService optionService;

    @Test
    void getOptionsReturnsMapperResult() {
        OptionResponseDto option = new OptionResponseDto();
        option.setOptionId(1L);
        option.setOptionName("치즈");
        List<OptionResponseDto> options = List.of(option);

        when(optionMapper.findAll()).thenReturn(options);

        assertThat(optionService.getOptions()).isSameAs(options);
    }

    @Test
    void getOptionsReturnsEmptyListWhenNoOptions() {
        when(optionMapper.findAll()).thenReturn(List.of());

        assertThat(optionService.getOptions()).isEmpty();
    }
}
