package io.pivotal.singapore.marvin.slack;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IncomingSlackRequest {
    private Validator validator;

    @Getter @Setter @NotBlank private String token;
    @Getter @Setter @NotBlank private String text;
    @Getter @Setter private String teamId;
    @Getter @Setter private String teamDomain;
    @Getter @Setter private String channelId;
    @Getter @Setter private String channelName;
    @Getter @Setter private String userId;
    @Getter @Setter private String userName;
    @Getter @Setter private String command;
    @Getter @Setter private String responseUrl;

    private String slackToken;

    private Set<ConstraintViolation<IncomingSlackRequest>> constraintViolations = Collections.emptySet();

    public IncomingSlackRequest(Map<String, String> params, String slackToken) {
        this.token = params.getOrDefault("token", null);
        this.teamId = params.getOrDefault("team_id", null);
        this.teamDomain = params.getOrDefault("team_domain", null);
        this.channelId = params.getOrDefault("channel_id", null);
        this.channelName = params.getOrDefault("channel_name", null);
        this.userId = params.getOrDefault("user_id", null);
        this.userName = params.getOrDefault("user_name", null);
        this.command = params.getOrDefault("command", null);
        this.text = params.getOrDefault("text", null);
        this.responseUrl = params.getOrDefault("response_url", null);
        this.slackToken = slackToken;
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @AssertTrue
    private boolean isRecognizedToken() {
        return this.token != null && this.token.equals(this.slackToken);
    }

    public boolean isInvalid() {
        constraintViolations = this.validator.validate(this);
        return constraintViolations.size() > 0;
    }

    public boolean isValid() {
        return !isInvalid();
    }

    public boolean hasErrorFor(String field) {
        return getErrors().containsKey(field);
    }

    public Map<String, Object> getErrors() {
        List<String> fields = getErrorFields();
        List<Object> values = getErrorValues();

        assert (fields.size() == values.size());

        HashMap<String, Object> errors = new HashMap();
        for (int i = 0; i < fields.size(); i++) {
            errors.put(fields.get(i), values.get(i));
        }
        return errors;
    }

    private List<Object> getErrorValues() {
        return constraintViolations
            .stream()
            .map(ConstraintViolation::getInvalidValue)
            .collect(Collectors.toList());
    }

    private List<String> getErrorFields() {
        return constraintViolations
            .stream()
            .map(ConstraintViolation::getPropertyPath)
            .map(pathImpl -> ((PathImpl) pathImpl).getLeafNode().getName())
            .collect(Collectors.toList());
    }
}

