package org.wsock.internal.model;

/**
 * Created by joco on 03.10.16.
 */
public class RespErrorMessage {
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
