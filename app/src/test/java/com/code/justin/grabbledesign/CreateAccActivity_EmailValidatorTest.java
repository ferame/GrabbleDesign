package com.code.justin.grabbledesign;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class CreateAccActivity_EmailValidatorTest{

    @Test
    public void emailValidator_CorrectEmailSimple_ReturnsTrue() {
        assertTrue(CreateAccActivity.isEmailic("name@email.com"));
    }

    @Test
    public void emailValidator_CorrectEmailSubDomain_ReturnsTrue() {
        assertTrue(CreateAccActivity.isEmailic("name@email.co.uk"));
    }

    @Test
    public void emailValidator_InvalidEmailNoTld_ReturnsFalse() {
        assertFalse(CreateAccActivity.isEmailic("name@email"));
    }

    @Test
    public void emailValidator_InvalidEmailDoubleDot_ReturnsFalse() {
        assertFalse(CreateAccActivity.isEmailic("name@email..com"));
    }

    @Test
    public void emailValidator_InvalidEmailNoUsername_ReturnsFalse() {
        assertFalse(CreateAccActivity.isEmailic("@email.com"));
    }

    @Test
    public void emailValidator_EmptyString_ReturnsFalse() {
        assertFalse(CreateAccActivity.isEmailic(""));
    }

    @Test
    public void emailValidator_NullEmail_ReturnsFalse() {
        assertFalse(CreateAccActivity.isEmailic(null));
    }
}