package io.pivotal.singapore.marvin.commands;

import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.commands.default_responses.DefaultResponses;
import org.springframework.web.bind.annotation.RequestMethod;

// Idomatic Java would call this Command

public interface ICommand {
    String getName();
    String getEndpoint();
    RequestMethod getMethod();

    String getDefaultResponseSuccess();
    String getDefaultResponseFailure();

    Arguments getArguments();

    DefaultResponses getDefaultResponses();

    boolean requiresEndpoint();

    boolean requiresMethod();
}
