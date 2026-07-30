package com.bunshik.admin.security;

import com.bunshik.common.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.ObjectMapper;

import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RestSecurityErrorHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void authenticationFailureUsesUnifiedUnauthorizedResponse()
            throws Exception {
        RestAuthenticationEntryPoint entryPoint =
                new RestAuthenticationEntryPoint(objectMapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("invalid token")
        );

        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType())
                .isEqualTo("application/json;charset=UTF-8");
        assertErrorBodyWasWritten();
    }

    @Test
    void authorizationFailureUsesUnifiedForbiddenResponse()
            throws Exception {
        RestAccessDeniedHandler handler =
                new RestAccessDeniedHandler(objectMapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("forbidden")
        );

        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType())
                .isEqualTo("application/json;charset=UTF-8");
        assertErrorBodyWasWritten();
    }

    private void assertErrorBodyWasWritten() throws Exception {
        ArgumentCaptor<ApiResponse<?>> bodyCaptor =
                ArgumentCaptor.forClass(ApiResponse.class);

        verify(objectMapper)
                .writeValue(any(Writer.class), bodyCaptor.capture());

        ApiResponse<?> body = bodyCaptor.getValue();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getData()).isNull();
        assertThat(body.getMessage()).isNotBlank();
    }
}
