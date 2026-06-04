package com.zhilulinghang.backend.controller;

import com.zhilulinghang.backend.dto.LoginRequest;
import com.zhilulinghang.backend.dto.LoginResponse;
import com.zhilulinghang.backend.mapper.UserMapper;
import com.zhilulinghang.backend.model.User;
import com.zhilulinghang.backend.security.AuthContext;
import com.zhilulinghang.backend.security.AuthUser;
import com.zhilulinghang.backend.security.JwtUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthController(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("请输入用户名和密码");
        }
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!isBlank(request.getRole()) && !normalizeRole(request.getRole()).equals(user.getRole())) {
            throw new IllegalArgumentException("所选角色与账号角色不匹配");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole().toLowerCase(Locale.ROOT));
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        AuthUser user = AuthContext.get();
        return Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole().toLowerCase(Locale.ROOT)
        );
    }

    private String normalizeRole(String role) {
        return role.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
