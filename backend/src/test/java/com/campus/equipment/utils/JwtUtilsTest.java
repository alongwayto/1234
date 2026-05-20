package com.campus.equipment.utils;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilsTest {

    @Test
    void generateAndValidateToken() {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret",
                "campus-equipment-management-test-secret-key-2026");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 60000L);

        String token = jwtUtils.generateToken("admin", 1L);

        assertTrue(jwtUtils.validateToken(token));
        assertEquals("admin", jwtUtils.getUsernameFromToken(token));
        assertEquals(1L, jwtUtils.getUserIdFromToken(token));
    }

    @Test
    void rejectInvalidToken() {
        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret",
                "campus-equipment-management-test-secret-key-2026");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 60000L);

        assertFalse(jwtUtils.validateToken("not-a-jwt-token"));
    }
}
