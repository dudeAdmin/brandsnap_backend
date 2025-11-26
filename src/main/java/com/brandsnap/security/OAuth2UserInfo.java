package com.brandsnap.security;

public interface OAuth2UserInfo {
    String getProviderId();

    String getName();

    String getEmail();
}
