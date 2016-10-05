package org.wsock.pub;

/**
 * Created by joco on 05.10.16.
 */
public class WsockConfig {
    private String allowedOrigins = null;

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
