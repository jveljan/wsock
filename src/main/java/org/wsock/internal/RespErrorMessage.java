package org.wsock.internal;

/**
 * Created by joco on 03.10.16.
 */
class RespErrorMessage {
    private String message;
    private String error;
    public RespErrorMessage(Exception resp) {
        this.message = resp.getMessage();
        this.error = resp.getClass().getName();
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}
