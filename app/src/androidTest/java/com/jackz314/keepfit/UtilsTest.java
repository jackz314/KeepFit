package com.jackz314.keepfit;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jackz314.keepfit.Utils.isValidEmail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class UtilsTest {

    @Test
    public void getJWTExpirationDate() {
    }

    @Test
    public void isValidEmail_simple() {
        assertTrue(isValidEmail("1@2.3"));
        assertTrue(isValidEmail("abc@gmail.com"));
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