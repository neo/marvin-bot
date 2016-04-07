package io.pivotal.singapore.marvin.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@ControllerAdvice
public class ExceptionHandlingControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public @ResponseBody
    HttpMessageNotReadableErrorInfo handleJsonParseException(HttpServletRequest req, HttpMessageNotReadableException e) {
        return new HttpMessageNotReadableErrorInfo(e.getCause().getLocalizedMessage());
    }
}
