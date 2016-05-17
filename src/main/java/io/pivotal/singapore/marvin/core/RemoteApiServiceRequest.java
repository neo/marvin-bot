package io.pivotal.singapore.marvin.core;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentParsedResult;
import io.pivotal.singapore.marvin.slack.interactions.MakeRemoteApiCallRequest;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoteApiServiceRequest {
    private final List<ArgumentParsedResult> parsedResultList;
    private final MakeRemoteApiCallRequest makeRemoteApiCallRequest;

    private Clock clock;

    // made package local since only usage is test
    RemoteApiServiceRequest(MakeRemoteApiCallRequest makeRemoteApiCallRequest, List<ArgumentParsedResult> parsedResultList, Clock clock) {
        this.makeRemoteApiCallRequest = makeRemoteApiCallRequest;
        this.parsedResultList = parsedResultList;
        this.clock = clock;
    }

    public RemoteApiServiceRequest(MakeRemoteApiCallRequest makeRemoteApiCallRequest, List<ArgumentParsedResult> parsedResultList) {
        this.makeRemoteApiCallRequest = makeRemoteApiCallRequest;
        this.parsedResultList = parsedResultList;
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
        return parsedResultList.stream()
            .collect(Collectors.toMap(ArgumentParsedResult::getArgumentName, ArgumentParsedResult::getMatchResult));
    }
}
