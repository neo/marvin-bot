package io.pivotal.singapore.marvin.commands.web;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RepositoryRestController
public class CommandController {

    @RequestMapping( value = "${spring.data.rest.basePath}commands", method = {
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.PATCH
    })
    public void handleDisallowedMethods() throws HttpMediaTypeNotSupportedException{
        throw new HttpMediaTypeNotSupportedException("Method not supported");
    }
}
