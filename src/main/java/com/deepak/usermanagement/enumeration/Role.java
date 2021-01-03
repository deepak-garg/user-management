package com.deepak.usermanagement.enumeration;

import static com.deepak.usermanagement.utils.Authority.ADMIN_AUTHORITIES;
import static com.deepak.usermanagement.utils.Authority.HR_AUTHORITIES;
import static com.deepak.usermanagement.utils.Authority.MANAGER_AUTHORITIES;
import static com.deepak.usermanagement.utils.Authority.SUPER_ADMIN_AUTHORITIES;
import static com.deepak.usermanagement.utils.Authority.USER_AUTHORITIES;

public enum Role {
    ROLE_USER(USER_AUTHORITIES),
    ROLE_HR(HR_AUTHORITIES),
    ROLE_MANAGER(MANAGER_AUTHORITIES),
    ROLE_ADMIN(ADMIN_AUTHORITIES),
    ROLE_SUPER_ADMIN(SUPER_ADMIN_AUTHORITIES);

    private String[] authorities;

    Role(String... authorities) {
        this.authorities = authorities;
    }

    public String[] getAuthorities() {
        return authorities;
    }
}
