package com.code.justin.grabbledesign;

import org.junit.Test;
import static org.junit.Assert.*;
/**
 * Created by justin on 22/01/17.
 */

public class CreateAccActivity_PasswordValidatorTest {
    @Test
    public void passwordValidator_CorrectPasswordSimple_ReturnsTrue() {
        assertTrue(CreateAccActivity.isPasswordic("somepass&!@#$%^121"));
    }

    @Test
    public void passwordValidator_CorrectPassword_20Char_ReturnsTrue() {
        assertTrue(CreateAccActivity.isPasswordic("01234567890123456789"));
    }

    @Test
    public void passwordValidator_InvalidPassword_21Char_ReturnsFalse() {
        assertFalse(CreateAccActivity.isPasswordic("012345678901234567890"));
    }

    @Test
    public void passwordValidator_EmptyString_ReturnsFalse() {
        assertFalse(CreateAccActivity.isPasswordic(""));
    }

    @Test
    public void passwordValidator_NullEmail_ReturnsFalse() {
        assertFalse(CreateAccActivity.isPasswordic(null));
    }
}
