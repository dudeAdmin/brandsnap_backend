package com.brandsnap.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CsrfController {

    /**
     * Endpoint to retrieve CSRF token for the frontend.
     * The token is automatically generated and stored in a cookie by Spring
     * Security.
     * This endpoint allows the frontend to trigger token generation on initial
     * load.
     */
    @GetMapping("/csrf-token")
    public CsrfToken csrfToken(CsrfToken token) {
        return token;
    }
}
