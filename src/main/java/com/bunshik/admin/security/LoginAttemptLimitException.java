package com.bunshik.admin.security;

public class LoginAttemptLimitException extends RuntimeException {

    public LoginAttemptLimitException() {
        super("로그인에 5회 실패하여 10분간 로그인이 제한됩니다.");
    }
}
