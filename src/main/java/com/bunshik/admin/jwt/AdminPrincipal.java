package com.bunshik.admin.jwt;

public record AdminPrincipal(
        Integer adminId,
        String username
) {
}