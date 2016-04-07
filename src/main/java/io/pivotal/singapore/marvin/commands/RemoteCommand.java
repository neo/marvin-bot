package io.pivotal.singapore.marvin.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RemoteCommand extends HystrixCommand<Map<String, String>> {

    private static final HystrixCommandGroupKey groupKey = HystrixCommandGroupKey.Factory.asKey("RemoteCommand");

    private Map params;
    private String endpoint;
    private RequestMethod method;
    private RestTemplate restTemplate;

    public RemoteCommand(RestTemplate restTemplate, RequestMethod method, String endpoint, Map params) {
        super(Setter.withGroupKey(groupKey).andCommandKey(HystrixCommandKey.Factory.asKey(endpoint)));
        this.params = params;
        this.endpoint = endpoint;
        this.method = method;
        this.restTemplate = restTemplate;
    }

    @Override
    protected Map<String, String> run() {
        switch (method) {
            case POST:
                return restTemplate.postForObject(endpoint, params, HashMap.class);
            case GET:
                return restTemplate.getForObject(buildUri(endpoint, params), HashMap.class);
            case PUT:
                return exchangeForObject(HttpMethod.PUT, endpoint, params);
            case DELETE:
                return exchangeForObject(HttpMethod.DELETE, endpoint, params);
            case PATCH:
                return exchangeForObject(HttpMethod.PATCH, endpoint, params);
            default:
                throw new IllegalArgumentException(
                        String.format("HTTP method '%s' not supported.", method)
                );
        }
    }

    @Override
    protected Map<String, String> getFallback() {
        Throwable exception = this.getFailedExecutionException();

        Map<String, String> body;
        if (exception instanceof HttpClientErrorException) {
            body = parseHttpError((HttpClientErrorException) exception);
        } else {
            body = new HashMap<>();
            body.put("message", "The service cannot be reached at this moment. You may wish to try again later.");
            body.put("message_type", "ephemeral");
        }

        return body;
    }

    private HashMap<String, String> exchangeForObject(HttpMethod method, String endpoint, Map params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

        return restTemplate.exchange(endpoint, method, entity, HashMap.class).getBody();
    }

    private String buildUri(String endpoint, Map<String, String> arguments) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endpoint);
        for (Map.Entry<String, String> arg : arguments.entrySet()) {
            builder = builder.queryParam(arg.getKey(), arg.getValue());
        }

        return builder.build().toUriString();
    }

    private Map<String, String> parseHttpError(HttpClientErrorException exc) {
        String contentType = "";
        try { // Is there a better way?
            contentType = exc.getResponseHeaders().get("Content-Type").get(0).toLowerCase();
        } catch (NullPointerException e) {
            // do nothing
        }
        HashMap<String, String> body;

        if (contentType.startsWith("application/json")) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(exc.getResponseBodyAsByteArray(), HashMap.class);
            } catch (IOException e) {
                // do nothing, return the default below
            }
        }

        body = new HashMap<>();
        body.put("errorBody", exc.getResponseBodyAsString());

        return body;
    }
}
