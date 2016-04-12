package io.pivotal.singapore.marvin.utils;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.RemoteCommand;
import io.pivotal.singapore.marvin.commands.default_responses.DefaultResponses;
import io.pivotal.singapore.marvin.core.RemoteApiService;
import io.pivotal.singapore.marvin.core.RemoteApiServiceResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class a {
    public static DefaultResponseBuilder defaultResponse = new DefaultResponseBuilder();
    public static CommandBuilder command = new CommandBuilder();
    public static RemoteApiServiceBuilder remoteApiService = new RemoteApiServiceBuilder();
    public static RemoteApiServiceResponseBuilder remoteApiServiceResponse = new RemoteApiServiceResponseBuilder();
    public static RemoteCommandBuilder remoteCommand = new RemoteCommandBuilder();

    public static class DefaultResponseBuilder {
        private Map<String, String> responses = new HashMap<>();

        @SuppressWarnings("unchecked")
        DefaultResponseBuilder() {
        }

        DefaultResponseBuilder(Map<String, String> responses) {
            this.responses = responses;
        }

        public DefaultResponseBuilder w(String key, String value) {
            Map<String, String> resp = new HashMap<>();
            resp.putAll(responses);
            resp.put(key, value);

            return new DefaultResponseBuilder(resp);
        }

        public DefaultResponses build() {
            return DefaultResponses.from(responses);
        }
    }

    public static class CommandBuilder {
        private String name = "echo";
        private String endpoint = "http://example.com/api/echo";
        private DefaultResponses defaultResponses = new DefaultResponses();
        private RequestMethod method = RequestMethod.POST;

        CommandBuilder() {
        }

        CommandBuilder(String name, String endpoint, DefaultResponses defaultResponses, RequestMethod method) {
            this.name = name;
            this.endpoint = endpoint;
            this.defaultResponses = defaultResponses;
            this.method = method;
        }

        public CommandBuilder w(String name, String endpoint) {
            return new CommandBuilder(name, endpoint, defaultResponses, method);
        }

        public CommandBuilder w(DefaultResponses defaultResponses) {
            return new CommandBuilder(name, endpoint, defaultResponses, method);
        }

        public CommandBuilder w(RequestMethod method) {
            return new CommandBuilder(name, endpoint, defaultResponses, method);
        }

        public Command build() {
            Command cmd = new Command(name, endpoint);
            cmd.setDefaultResponses(defaultResponses);
            cmd.setMethod(method);

            return cmd;
        }
    }

    public static class RemoteApiServiceBuilder {
        private RestTemplate restTemplate = mock(RestTemplate.class);

        public RemoteApiServiceBuilder() {
        }

        public RemoteApiServiceBuilder(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        public RemoteApiServiceBuilder w(RestTemplate restTemplate) {
            return new RemoteApiServiceBuilder(restTemplate);
        }

        public RemoteApiService build() {
            return new RemoteApiService(restTemplate);
        }
    }

    public static class RemoteApiServiceResponseBuilder {
        private Boolean success = true;
        private Map<String, String> body = new HashMap<>();
        private DefaultResponses defaultResponses = new DefaultResponses();

        public RemoteApiServiceResponseBuilder() {
        }

        public RemoteApiServiceResponseBuilder(Boolean success, Map<String, String> body, DefaultResponses defaultResponses) {
            this.defaultResponses = defaultResponses;
            this.body = body;
            this.success = success;
        }

        public RemoteApiServiceResponseBuilder w(Boolean success) {
            return new RemoteApiServiceResponseBuilder(success, body, defaultResponses);
        }

        public RemoteApiServiceResponseBuilder w(Map<String, String> body) {
            return new RemoteApiServiceResponseBuilder(success, body, defaultResponses);
        }

        public RemoteApiServiceResponseBuilder w(String key, String value) {
            Map<String, String> newBody = new HashMap<>();
            newBody.putAll(body);
            newBody.put(key, value);

            return new RemoteApiServiceResponseBuilder(success, newBody, defaultResponses);
        }

        public RemoteApiServiceResponseBuilder w(DefaultResponses defaultResponses) {
            return new RemoteApiServiceResponseBuilder(success, body, defaultResponses);
        }

        public RemoteApiServiceResponse build() {
            return new RemoteApiServiceResponse(success, body, defaultResponses);
        }
    }

    public static class RemoteCommandBuilder {
        private RestTemplate restTemplate = new RestTemplate();
        private RequestMethod method = RequestMethod.POST;
        private String endpoint = "http://example.com/api/echo";
        private Map<String, String> params = new HashMap<>();

        public RemoteCommandBuilder() {

        }

        public RemoteCommandBuilder(RestTemplate restTemplate, RequestMethod method, String endpoint, Map<String, String> params) {
            this.restTemplate = restTemplate;
            this.method = method;
            this.endpoint = endpoint;
            this.params = params;
        }

        public RemoteCommandBuilder w(RestTemplate restTemplate) {
            return new RemoteCommandBuilder(restTemplate, method, endpoint, params);
        }

        public RemoteCommandBuilder w(RequestMethod method) {
            return new RemoteCommandBuilder(restTemplate, method, endpoint, params);
        }

        public RemoteCommandBuilder w(String endpoint) {
            return new RemoteCommandBuilder(restTemplate, method, endpoint, params);
        }

        public RemoteCommandBuilder w(String key, String value) {
            Map<String, String> newParams = new HashMap<>();
            newParams.putAll(params);
            newParams.put(key, value);

            return new RemoteCommandBuilder(restTemplate, method, endpoint, newParams);
        }

        public RemoteCommandBuilder w(Map<String, String> params) {
            Map<String, String> newParams = new HashMap<>();
            newParams.putAll(params);

            return new RemoteCommandBuilder(restTemplate, method, endpoint, newParams);
        }

        public RemoteCommand build() {
            return new RemoteCommand(restTemplate, method, endpoint, params);
        }
    }
}
