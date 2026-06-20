package com.zhilulinghang.backend.security;

import com.zhilulinghang.backend.mapper.UserMapper;
import com.zhilulinghang.backend.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public AuthInterceptor(JwtUtil jwtUtil, UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalStateException("请先登录");
        }
        AuthUser authUser = jwtUtil.parseToken(authorization.substring(7));
        User user = userMapper.findById(authUser.getId());
        if (user == null || Boolean.FALSE.equals(user.getEnabled())) {
            throw new IllegalStateException("账号已被禁用，请重新登录或联系管理员");
        }
        AuthContext.set(authUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
