package com.code.justin.grabbledesign;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by justin on 22/01/17.
 */

public class CreateAccActivity_NicknameValidatorTest {
    @Test
    public void nicknameValidator_CorrectNicknameSimple_ReturnsTrue() {
        assertTrue(CreateAccActivity.isNicknamic("SOMEnick123"));
    }

    @Test
    public void nicknameValidator_CorrectNickname_onlyUpperCase_ReturnsTrue() {
        assertTrue(CreateAccActivity.isNicknamic("SOMENICK"));
    }

    @Test
    public void nicknameValidator_CorrectNickname_onlyLowerCase_ReturnsTrue() {
        assertTrue(CreateAccActivity.isNicknamic("somenick"));
    }

    @Test
    public void nicknameValidator_CorrectNickname_onlyNumeric_ReturnsTrue() {
        assertTrue(CreateAccActivity.isNicknamic("0123456789"));
    }

    @Test
    public void nicknameValidator_CorrectNickname_20Char_ReturnsTrue() {
        assertTrue(CreateAccActivity.isNicknamic("some0123456789nickna"));
    }

    @Test
    public void nicknameValidator_CorrectNickname_21Char_ReturnsTrue() {
        assertFalse(CreateAccActivity.isNicknamic("some0123456789nicknam"));
    }
    @Test
    public void nicknameValidator_EmptyString_ReturnsFalse() {
        assertFalse(CreateAccActivity.isNicknamic(""));
    }

    @Test
    public void nicknameValidator_NullNick_ReturnsFalse() {
        assertFalse(CreateAccActivity.isNicknamic(null));
    }
}
