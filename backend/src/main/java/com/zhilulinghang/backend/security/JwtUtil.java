package com.zhilulinghang.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Component
public class JwtUtil {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    @Value("${jwt.secret:zhilulinghang-secret}")
    private String secret;

    @Value("${jwt.expire-seconds:86400}")
    private long expireSeconds;

    public String generateToken(Long userId, String username, String role) {
        long exp = Instant.now().getEpochSecond() + expireSeconds;
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"userId\":" + userId + ",\"username\":\"" + escape(username) + "\",\"role\":\"" + role + "\",\"exp\":" + exp + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public AuthUser parseToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("无效 token");
        }
        String expected = sign(parts[0] + "." + parts[1]);
        if (!constantTimeEquals(expected, parts[2])) {
            throw new IllegalArgumentException("token 签名无效");
        }
        String payload = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        Long userId = Long.valueOf(extract(payload, "userId"));
        String username = extractString(payload, "username");
        String role = extractString(payload, "role");
        long exp = Long.parseLong(extract(payload, "exp"));
        if (Instant.now().getEpochSecond() > exp) {
            throw new IllegalArgumentException("token 已过期");
        }
        return new AuthUser(userId, username, role);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("无法生成 token 签名", e);
        }
    }

    private String encode(String value) {
        return URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractString(String json, String key) {
        return extract(json, key).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String extract(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("token 缺少字段: " + key);
        }
        start += marker.length();
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        }
        int end = json.indexOf(',', start);
        if (end < 0) {
            end = json.indexOf('}', start);
        }
        return json.substring(start, end);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigestUtil.constantTimeEquals(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
