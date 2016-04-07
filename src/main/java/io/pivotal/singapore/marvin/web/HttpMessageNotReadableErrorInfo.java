package io.pivotal.singapore.marvin.web;

import java.util.HashMap;

public class HttpMessageNotReadableErrorInfo {
    public HashMap<String, Object> error = new HashMap<>();

    public HttpMessageNotReadableErrorInfo(String errorMessage) {
        error.put("message", errorMessage);
    }
}

