package com.jackz314.keepfit;

import com.jackz314.keepfit.views.SearchActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ValidSearch {

    @Test
    public void isValidQuery() {
        assertTrue(SearchActivity.isValidQuery("a"));
        assertTrue(SearchActivity.isValidQuery("hernansj"));
        assertTrue(SearchActivity.isValidQuery("  test"));
        assertTrue(SearchActivity.isValidQuery("test   "));
        assertTrue(SearchActivity.isValidQuery("1234"));
        assertTrue(SearchActivity.isValidQuery("test123"));
        assertFalse(SearchActivity.isValidQuery("hernansj@usc.edu"));
        assertFalse(SearchActivity.isValidQuery(""));
        assertFalse(SearchActivity.isValidQuery("    "));
        assertFalse(SearchActivity.isValidQuery("."));
        assertFalse(SearchActivity.isValidQuery("test."));
    }

    @Test
    public void stripTest() {
        assertEquals("test", SearchActivity.stripQuery("  test"));
        assertEquals("test", SearchActivity.stripQuery("test    "));
        assertEquals("test", SearchActivity.stripQuery("  test  "));
        assertEquals("t e s t", SearchActivity.stripQuery("t e s t"));
        assertEquals("te st", SearchActivity.stripQuery(" te st "));
    }

}