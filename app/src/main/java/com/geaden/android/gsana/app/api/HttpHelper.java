package com.geaden.android.gsana.app.api;

/**
 * HTTP helper class
 */
public class HttpHelper {
    /** Response codes **/
    public static class ResponseCode {
        public static final int OK = 200;
        public static final int UNAUTHORIZED = 401;
        public static final int ACCESS_DENIED = 403;
    }

    /** HTTP Request methods **/
    public static class Method {
        public static final String GET = "GET";
        public static final String PUT = "PUT";
        public static final String POST = "POST";
        public static final String DELETE = "DELETE";
    }
}
