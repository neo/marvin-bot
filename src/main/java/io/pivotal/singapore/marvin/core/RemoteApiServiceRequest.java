package io.pivotal.singapore.marvin.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.slack.interactions.InteractionRequest;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RemoteApiServiceRequest {
    private final ArgumentParsedResultList parsedResultSet;
    private final InteractionRequest interactionRequest;

    private Clock clock;

    // made package local since only usage is test
    RemoteApiServiceRequest() {
        this(null, null);
    }

    // made package local since only usage is test
    RemoteApiServiceRequest(InteractionRequest interactionRequest, ArgumentParsedResultList parsedResultSet, Clock clock) {
        this.interactionRequest = interactionRequest;
        this.parsedResultSet = parsedResultSet;
        this.clock = clock;
    }

    public RemoteApiServiceRequest(InteractionRequest interactionRequest, ArgumentParsedResultList parsedResultSet) {
        this.interactionRequest = interactionRequest;
        this.parsedResultSet = parsedResultSet;
        this.clock = Clock.systemUTC();
    }

    @SuppressWarnings("unchecked")
    Map<String, String> toMap() {
        return new ObjectMapper().convertValue(this, HashMap.class);
    }

    @JsonProperty
    String getChannel() {
        return interactionRequest.getChannelName();
    }

    @JsonProperty
    String getCommand() {
        return interactionRequest.getCommand();
    }

    @JsonProperty("received_at")
    String getReceivedAt() {
        return ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    @JsonProperty
    String getUsername() {
        return interactionRequest.getUserName();
    }

    @JsonAnyGetter
    Map<String, String> getParsedArguments() {
        return parsedResultSet.getArgumentAndMatchResultMap();
    }
}
