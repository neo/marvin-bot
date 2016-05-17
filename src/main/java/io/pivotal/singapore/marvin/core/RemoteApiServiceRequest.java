package io.pivotal.singapore.marvin.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResultList;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCallRequest;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RemoteApiServiceRequest {
    private final ArgumentParsedResultList parsedResultSet;
    private final MakeRemoteApiCallRequest makeRemoteApiCallRequest;

    private Clock clock;

    // made package local since only usage is test
    RemoteApiServiceRequest() {
        this(null, null);
    }

    // made package local since only usage is test
    RemoteApiServiceRequest(MakeRemoteApiCallRequest makeRemoteApiCallRequest, ArgumentParsedResultList parsedResultSet, Clock clock) {
        this.makeRemoteApiCallRequest = makeRemoteApiCallRequest;
        this.parsedResultSet = parsedResultSet;
        this.clock = clock;
    }

    public RemoteApiServiceRequest(MakeRemoteApiCallRequest makeRemoteApiCallRequest, ArgumentParsedResultList parsedResultSet) {
        this.makeRemoteApiCallRequest = makeRemoteApiCallRequest;
        this.parsedResultSet = parsedResultSet;
        this.clock = Clock.systemUTC();
    }

    @JsonProperty
    public String getChannel() {
        return makeRemoteApiCallRequest.getChannelName();
    }

    @JsonProperty
    public String getCommand() {
        return makeRemoteApiCallRequest.getCommand();
    }

    @JsonProperty("received_at")
    public String getReceivedAt() {
        return ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    @JsonProperty
    public String getUsername() {
        return makeRemoteApiCallRequest.getUserName();
    }

    public Map<String, String> toMap() {
        return new ObjectMapper().convertValue(this, HashMap.class);
    }

    @JsonAnyGetter
    private Map<String, String> getParsedArguments() {
        return parsedResultSet.getArgumentAndMatchResultMap();
    }
}
