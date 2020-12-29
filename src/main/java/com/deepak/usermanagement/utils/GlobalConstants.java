package com.deepak.usermanagement.utils;

public class GlobalConstants {
    public static final long TOKEN_EXPIRATION_TIME = 432_000_000; // 5 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String COMPANY_NAME = "Deepak, LLC";
    public static final String AUTHORITIES = "Authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to login to access this page";
    public static final String ACCESS_DENIED = "You don't have permission to access this page";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
    public static final String[] PUBLIC_URLS = {"/user/login", "/user/register","/user/resetpassword/**",
                                                    "/user/image/**"};

}
