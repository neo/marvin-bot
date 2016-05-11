package io.pivotal.singapore.marvin.slack;

import com.google.common.collect.ImmutableMap;
import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.arguments.Argument;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParseException;
import io.pivotal.singapore.marvin.core.CommandParserService;
import io.pivotal.singapore.marvin.core.MessageType;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.core.RemoteApiServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
class SlackController {
    @Autowired
    CommandRepository commandRepository;

    @Autowired
    RemoteApiService remoteApiService;

    @Autowired
    CommandParserService commandParserService;

    @Value("${api.slack.token}")
    private String SLACK_TOKEN;

    private Clock clock = Clock.systemUTC();

    @RequestMapping(value="/", method= RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, String> index(@RequestParam Map<String, String> params) throws Exception {
        SlackRequest slackRequest = new SlackRequest(params, SLACK_TOKEN);

        if (slackRequest.isValid()) {
            if (slackRequest.getErrors().containsKey("recognizedToken")) {
                throw new UnrecognizedApiToken();
            }
            return defaultResponse();
        }

        // Parses into command, sub-command, args as token strings
        HashMap<String, String> parsedCommand = commandParserService.parse(slackRequest.getText());

        // Checks if Command exists
        Optional<Command> commandOptional = getCommand(parsedCommand.get("command"));
        if (!commandOptional.isPresent()) {
            return defaultResponse();
        }

        // Makes remote API calls
        RemoteApiServiceResponse response;
        Optional<ICommand> subCommandOptional = commandOptional.get().findSubCommand(parsedCommand.get("sub_command"));

        if( !subCommandOptional.isPresent() && !commandOptional.get().getSubCommands().isEmpty()) {
            return defaultResponse(String.format("This sub command doesn't exist for %s", parsedCommand.get("command")));
        }

        Map _params = remoteServiceParams(slackRequest);

        // FIXME: Only fallback to command if there are no subcommands
        ICommand cmd = subCommandOptional.orElse(commandOptional.get());

        try {
            Map args = cmd.getArguments().parse(parsedCommand.get("arguments"));
            _params.putAll(args);
        } catch (ArgumentParseException ex) {
            Argument argument = ((Argument) ex.getThrower());
            String text = String.format("`%s` is not found in your command.", argument.getName());

            return ImmutableMap.of("text", text, "response_type", getSlackResponseType(MessageType.user));
        }

        response = remoteApiService.call(cmd, _params);

        // Compiles final response to Slack
        return textResponse(response.getMessageType(), response.getMessage());
    }

    private HashMap<String, Object> remoteServiceParams(SlackRequest params) {
        HashMap<String, Object> serviceParams = new HashMap<>();
        serviceParams.put("username", String.format("%s@pivotal.io", params.getUserName()));
        serviceParams.put("channel", params.getChannelName());
        serviceParams.put("received_at", ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        serviceParams.put("command", params.getText());

        return serviceParams;
    }

    private Optional<Command> getCommand(String commandName) {
        return commandRepository.findOneByName(commandName);
    }

    HashMap<String, String> textResponse(Optional<MessageType> messageType, String text) {
        String responseType = getSlackResponseType(messageType.orElse(MessageType.user));

        HashMap<String, String> response = new HashMap<>();
        response.put("response_type", responseType);
        response.put("text", text);

        return response;
    }

    private String getSlackResponseType(MessageType messageType) {
        switch (messageType) {
            case user:
                return "ephemeral";
            case channel:
                return "in_channel";
            default:
                throw new IllegalArgumentException(
                    String.format("MessageType '%s' is not configured for Slack", messageType.toString())
                );
        }
    }

    private HashMap<String, String> defaultResponse() {
        return defaultResponse("This will all end in tears.");
    }

    private HashMap<String, String> defaultResponse(String message) {
        return textResponse(Optional.of(MessageType.user), message);
    }
}

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unrecognized token")
class UnrecognizedApiToken extends Exception {
}
