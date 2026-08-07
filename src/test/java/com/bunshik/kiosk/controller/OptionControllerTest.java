package com.bunshik.kiosk.controller;

import com.bunshik.admin.jwt.AdminJwtAuthenticationFilter;
import com.bunshik.common.config.SecurityConfig;
import com.bunshik.kiosk.dto.OptionResponseDto;
import com.bunshik.kiosk.service.OptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OptionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, AdminJwtAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class OptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OptionService optionService;

    @Test
    void getOptionsReturnsOptionList() throws Exception {
        OptionResponseDto option = new OptionResponseDto();
        option.setOptionId(1L);
        option.setOptionName("치즈");
        option.setOptionPrice(1000);
        option.setOptionIsAvailable(true);

        when(optionService.getOptions()).thenReturn(List.of(option));

        mockMvc.perform(get("/api/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].option_name").value("치즈"))
                .andExpect(jsonPath("$.data[0].option_price").value(1000));
    }

    @Test
    void getOptionsReturnsEmptyListWhenNoOptions() throws Exception {
        when(optionService.getOptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
