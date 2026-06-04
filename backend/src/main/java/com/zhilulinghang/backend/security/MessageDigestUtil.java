package com.zhilulinghang.backend.security;

import java.security.MessageDigest;

public class MessageDigestUtil {
    private MessageDigestUtil() {
    }

    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }
}
