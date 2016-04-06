package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.core.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Optional;

public class RemoteApiServiceResponse {
    private Boolean success;
    @Getter private Map<String, String> body;
    @Setter @Accessors(chain = true) private ICommand command;

    public RemoteApiServiceResponse(Boolean successful, Map<String, String> body) {
        this.success = successful;
        this.body = body;
    }

    public RemoteApiServiceResponse(Boolean successful, Map<String, String> body, ICommand command) {
        this.success = successful;
        this.body = body;
        this.command = command;
    }

    public Boolean isSuccessful() {
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
        String message = getBody().getOrDefault("message", getDefaultMessage());

        return interpolate(message);
    }

    private String getDefaultMessage() {
        String defaultMessage = getDefaultResponse();

        if (defaultMessage != null) {
            return defaultMessage;
        } else { // No default message provided by service, so return the body whatever they sent
            return getBody().toString();
        }
    }

    private String getDefaultResponse() {
        return isSuccessful() ? command.getDefaultResponseSuccess() : command.getDefaultResponseFailure();
    }

    private String interpolate(String message) {
        for (Map.Entry<String, String> entry : getBody().entrySet()) {
            String pattern = String.format("{%s}", entry.getKey());
            try {
                message = message.replace(pattern, entry.getValue());
            } catch (Exception e) {
                // do nothing
            }
        }

        return message;
    }
}
