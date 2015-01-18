package com.geaden.android.gsana.app.test.api;

import android.test.AndroidTestCase;

import com.geaden.android.gsana.app.api.AsanaApi2;

/**
 * Test Asana API
 */
public class TestAsanaApi extends AndroidTestCase {
    public void testBuildPath() {
        AsanaApi2 asanaApi = AsanaApi2.getInstance(this.getContext(), "");
        assertEquals("https://app.asana.com:443/api/1.0", asanaApi.baseApiUrl());
    }

}
