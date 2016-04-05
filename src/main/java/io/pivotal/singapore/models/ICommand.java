package io.pivotal.singapore.models;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import org.springframework.web.bind.annotation.RequestMethod;

public interface ICommand {
    String getName();
    String getEndpoint();
    RequestMethod getMethod();

    String getDefaultResponseSuccess();
    String getDefaultResponseFailure();

    Arguments getArguments();

    boolean requiresEndpoint();

    boolean requiresMethod();
}
