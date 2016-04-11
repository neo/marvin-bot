package io.pivotal.singapore.marvin.commands;

import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.commands.default_response.DefaultResponse;
import org.springframework.web.bind.annotation.RequestMethod;

public interface ICommand {
    String getName();
    String getEndpoint();
    RequestMethod getMethod();

    String getDefaultResponseSuccess();
    String getDefaultResponseFailure();

    Arguments getArguments();

    DefaultResponse getDefaultResponse();

    boolean requiresEndpoint();

    boolean requiresMethod();
}
