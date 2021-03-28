package com.jackz314.keepfit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.jackz314.keepfit.Utils.isValidEmail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class UtilsTest {

    @Test
    public void getJWTExpirationDate() {
    }

    @Test
    public void isValidEmail() {
        assertTrue(Utils.isValidEmail("1@2.3"));
        assertTrue(Utils.isValidEmail("abc@gmail.com"));
        assertFalse(Utils.isValidEmail("abc@gmailcom"));
        assertFalse(Utils.isValidEmail("abcgmail.com"));
        assertFalse(Utils.isValidEmail("@gmail.com"));
        assertFalse(Utils.isValidEmail("@.com"));
        assertFalse(Utils.isValidEmail(""));
        assertFalse(Utils.isValidEmail(" "));
        assertFalse(Utils.isValidEmail("123gmail.com"));
        assertFalse(Utils.isValidEmail("qegiuwerhgjq89"));
    }

    @Test
    public void getMD5() {
        assertEquals("", Utils.getMD5(null));
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Utils.getMD5(""));
        assertEquals("7215ee9c7d9dc229d2921a40e899ec5f", Utils.getMD5(" "));
        assertEquals("75170fc230cd88f32e475ff4087f81d9", Utils.getMD5("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
        assertEquals("34fda6e1ae53be65f5e9bf923d3a1095", Utils.getMD5("https://usc.zoom.us/j/1234567890?pwd=OaspGJUiAS392utgjf89weYYH89IJFHU&uname=Rick+Astley"));
        assertEquals("a18c43c8b63fa6800a53bb187b9ddd45", Utils.getMD5("7215ee9c7d9dc229d2921a40e899ec5f"));
        assertEquals("e9d9c6145d66e69d3f6bf973fe244b90", Utils.getMD5("a18c43c8b63fa6800a53bb187b9ddd45"));
        assertEquals("7f138a09169b250e9dcb378140907378", Utils.getMD5("MD5"));
    }

    @Test
    public void toTitleCase() {
        assertNull(Utils.toTitleCase(null));
        assertEquals("A", Utils.toTitleCase("a"));
        assertEquals("Abc", Utils.toTitleCase("abc"));
        assertEquals("Abc", Utils.toTitleCase("ABC"));
        assertEquals("Abcd", Utils.toTitleCase("aBCD"));
        assertEquals("Abc Def", Utils.toTitleCase("abc def"));
        assertEquals("Title Case", Utils.toTitleCase("title case"));
        assertEquals("Title Case, Now", Utils.toTitleCase("title case, now"));
        assertEquals("Evening Walk", Utils.toTitleCase("evening walk"));
        assertEquals("Evening Walk", Utils.toTitleCase("evening Walk"));
        assertEquals("Evening Walk", Utils.toTitleCase("Evening Walk"));
        assertEquals("Running", Utils.toTitleCase("running"));
        assertEquals("Running", Utils.toTitleCase("Running"));
    }

    @Test
    public void centimeterToFeet() {
        assertEquals("", Utils.centimeterToFeet(-100.5));
        assertEquals("", Utils.centimeterToFeet(-1));
        assertEquals("3' 4''", Utils.centimeterToFeet(100.5));
        assertEquals("3' 4''", Utils.centimeterToFeet(100));
        assertEquals("5' 11''", Utils.centimeterToFeet(180));
        assertEquals("6' 1''", Utils.centimeterToFeet(185));
        assertEquals("6'", Utils.centimeterToFeet(182.88));
        assertEquals("0'", Utils.centimeterToFeet(0));
        assertEquals("70455500' 3''", Utils.centimeterToFeet(Integer.MAX_VALUE));
    }

    @Test
    public void formatDurationString() {
        assertEquals("0:00", UtilsKt.formatDurationString(0L));
        assertEquals("0:00", UtilsKt.formatDurationString(null));
        assertEquals("0:00", UtilsKt.formatDurationString(-100L));
        assertEquals("1:40", UtilsKt.formatDurationString(100L));
        assertEquals("2777777777:46:40", UtilsKt.formatDurationString(10000000000000L));
        assertEquals("2562047788015215:30:07", UtilsKt.formatDurationString(Long.MAX_VALUE));
    }
}