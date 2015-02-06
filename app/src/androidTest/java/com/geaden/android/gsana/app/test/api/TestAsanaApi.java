package com.geaden.android.gsana.app.test.api;

import android.test.AndroidTestCase;

import com.geaden.android.gsana.app.api.AsanaApiBridge;

/**
 * Test Asana API
 */
public class TestAsanaApi extends AndroidTestCase {
    public void testBuildPath() {
        assertEquals("https://app.asana.com:443/api/1.0", AsanaApiBridge.baseApiUrl());
    }

}
