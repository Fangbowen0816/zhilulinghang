package com.zhilulinghang.backend.security;

public class AuthContext {
    private static final ThreadLocal<AuthUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        CURRENT_USER.set(user);
    }

    public static AuthUser get() {
        AuthUser user = CURRENT_USER.get();
        if (user == null) {
            throw new IllegalStateException("未登录");
        }
        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
