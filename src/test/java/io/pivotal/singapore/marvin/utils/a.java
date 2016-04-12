package io.pivotal.singapore.marvin.utils;

import io.pivotal.singapore.marvin.commands.RemoteCommand;
import io.pivotal.singapore.marvin.commands.default_responses.DefaultResponses;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

public class a {
    public static DefaultResponseBuilder defaultResponse = new DefaultResponseBuilder();
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
