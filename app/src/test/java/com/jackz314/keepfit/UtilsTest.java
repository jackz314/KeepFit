package com.jackz314.keepfit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.jackz314.keepfit.Utils.isValidEmail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UtilsTest {

    @Test
    public void getJWTExpirationDate() {
    }

    @Test
    public void isValidEmail_simple() {
        assertTrue(isValidEmail("1@2.3"));
        assertTrue(isValidEmail("abc@gmail.com"));
        assertTrue(isValidEmail("cool.beans.123@gmail.com"));
        assertFalse(isValidEmail("abc@gmailcom"));
        assertFalse(isValidEmail("abcgmail.com"));
        assertFalse(isValidEmail("@gmail.com"));
        assertFalse(isValidEmail("@.com"));
        assertFalse(isValidEmail("123gmail.com"));
    }

    @Test
    public void getMD5() {
    }

    @Test
    public void toTitleCase() {
    }

    @Test
    public void toHumanReadableFormat() {
    }

    @Test
    public void centimeterToFeet() {
    }
}