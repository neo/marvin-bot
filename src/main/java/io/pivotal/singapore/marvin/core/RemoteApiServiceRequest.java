package io.pivotal.singapore.marvin.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.slack.SlackInteractionRequest;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RemoteApiServiceRequest {
    private final ArgumentParsedResultList parsedResultSet;
    private final SlackInteractionRequest slackInteractionRequest;

    private Clock clock;

    // made package local since only usage is test
    RemoteApiServiceRequest() {
        this(null, null);
    }

    // made package local since only usage is test
    RemoteApiServiceRequest(SlackInteractionRequest slackInteractionRequest, ArgumentParsedResultList parsedResultSet, Clock clock) {
        this.slackInteractionRequest = slackInteractionRequest;
        this.parsedResultSet = parsedResultSet;
        this.clock = clock;
    }

    public RemoteApiServiceRequest(SlackInteractionRequest slackInteractionRequest, ArgumentParsedResultList parsedResultSet) {
        this.slackInteractionRequest = slackInteractionRequest;
        this.parsedResultSet = parsedResultSet;
        this.clock = Clock.systemUTC();
    }

    Map<String, String> toMap() {
        return new ObjectMapper().convertValue(this, HashMap.class);
    }

    @JsonProperty
    String getChannel() {
        return slackInteractionRequest.getChannelName();
    }

    @JsonProperty
    String getCommand() {
        return slackInteractionRequest.getCommand();
    }

    @JsonProperty("received_at")
    String getReceivedAt() {
        return ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    @JsonProperty
    String getUsername() {
        return slackInteractionRequest.getUserName();
    }

    @JsonAnyGetter
    Map<String, String> getParsedArguments() {
        return parsedResultSet.getArgumentAndMatchResultMap();
    }
}
