package com.wainyz.core.pojo.domain;

import java.security.Principal;

/**
 * @author Yanion_gwgzh
 */
public class CustomPrincipal implements Principal {
    private final String name;

    public CustomPrincipal(String name) {
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
}