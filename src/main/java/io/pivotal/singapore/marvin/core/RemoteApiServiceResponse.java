package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.default_responses.DefaultResponses;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Optional;

public class RemoteApiServiceResponse {
    @Getter @Setter private DefaultResponses defaultResponses;
    @Getter private Map<String, String> body;
    private Boolean success;

    // To be removed when the defaultResponses are removed from the command/subcommand
    @Deprecated
    public RemoteApiServiceResponse(Boolean successful, Map<String, String> body, String defaultResponseSuccess, String defaultResponseFailure) {
        this.success = successful;
        this.body = body;
        this.defaultResponses = new DefaultResponses();
        defaultResponses.putMessage("success", defaultResponseSuccess);
        defaultResponses.putMessage("failure", defaultResponseFailure);
    }

    // To be removed when the defaultResponses are removed from the command/subcommand
    @Deprecated
    RemoteApiServiceResponse(Boolean successful, Map<String, String> body, ICommand command) {
        this.success = successful;
        this.body = body;
        this.defaultResponses = command.getDefaultResponses();

        if (command.getDefaultResponseSuccess() != null) {
            this.defaultResponses.putMessage("success", command.getDefaultResponseSuccess());
        }
        if (command.getDefaultResponseFailure() != null) {
            this.defaultResponses.putMessage("failure", command.getDefaultResponseFailure());
        }
    }

    public RemoteApiServiceResponse(Boolean success, Map<String, String> body, DefaultResponses defaultResponses) {
        this.body = body;
        this.success = success;
        this.defaultResponses = defaultResponses;
    }

    Boolean isSuccessful() {
        return success;
    }

    public Optional<MessageType> getMessageType() {
        try {
            String messageTypeField = getBody().getOrDefault("messageType", getBody().get("message_type"));

            return Optional.of(MessageType.valueOf(messageTypeField));
        } catch (NullPointerException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String getMessage() {
        if (getBody() == null) {
            return getDefaultMessage();
        } else {
            String message = getBody().getOrDefault("message", getDefaultMessage());

            return interpolate(message);
        }
    }

    private String getDefaultMessage() {
        Optional<String> defaultMessage = getDefaultResponse();

        if (defaultMessage.isPresent()) {
            return defaultMessage.get();
        } else { // No default message provided by service, so return the body whatever they sent
            Map<String, String> body = getBody();

            return body == null ? "" : String.valueOf(body);
        }
    }

    private Optional<String> getDefaultResponse() {
        return isSuccessful() ? getDefaultResponseSuccess() : getDefaultResponseFailure();
    }

    private Optional<String> getDefaultResponseSuccess() {
        return defaultResponses.getMessage("success");
    }

    private Optional<String> getDefaultResponseFailure() {
        return defaultResponses.getMessage("failure");
    }

    private String interpolate(String message) {
        for (Map.Entry<String, String> entry : getBody().entrySet()) {
            String pattern = String.format("{%s}", entry.getKey());
            try {
                message = message.replace(pattern, String.valueOf(entry.getValue()));
            } catch (Exception e) {
                // do nothing
            }
        }

        return message;
    }
}
