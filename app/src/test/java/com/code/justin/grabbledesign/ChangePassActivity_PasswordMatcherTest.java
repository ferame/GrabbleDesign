package com.code.justin.grabbledesign;
import org.junit.Test;

import static org.junit.Assert.*;
/**
 * Created by justin on 22/01/17.
 */

public class ChangePassActivity_PasswordMatcherTest {
    @Test
    public void changePassActivityValidator_CorrectNicknameSimple_ReturnsTrue() {
        assertTrue(ChangePassActivity.passwordsMatch("pass", "pass"));
    }

    @Test
    public void changePassActivityValidator_OneNull_ReturnsFalse() {
        assertFalse(ChangePassActivity.passwordsMatch(null, "pass"));
    }

    @Test
    public void changePassActivityValidator_TwoNulls_ReturnsFalse() {
        assertFalse(ChangePassActivity.passwordsMatch(null, null));
    }
}
