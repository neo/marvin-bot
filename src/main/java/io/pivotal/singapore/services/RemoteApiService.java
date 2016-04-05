package io.pivotal.singapore.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.singapore.models.ICommand;
import io.pivotal.singapore.utils.RemoteApiServiceResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class RemoteApiService {
    private RestTemplate restTemplate;

    public RemoteApiService() {
        this(new RestTemplate());
    }

    public RemoteApiService(RestTemplate _restTemplate) {
        restTemplate = _restTemplate;
        HttpClient httpClient = HttpClients.createDefault();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }

    public RemoteApiServiceResponse call(ICommand command, Map params) {
        return call(command.getMethod(), command.getEndpoint(), params)
            .setCommand(command);
    }

    public RemoteApiServiceResponse call(RequestMethod method, String endpoint, Map params) {
        if(method == null) {
            throw new IllegalArgumentException("HTTP method was not defined by the command provider");
        }
        try {
            switch (method) {
                case POST:
                    return new RemoteApiServiceResponse(true, restTemplate.postForObject(endpoint, params, HashMap.class));
                case GET:
                    return new RemoteApiServiceResponse(true, restTemplate.getForObject(buildUri(endpoint, params), HashMap.class));
                case PUT:
                    return new RemoteApiServiceResponse(true, exchangeForObject(HttpMethod.PUT, endpoint, params));
                case DELETE:
                    return new RemoteApiServiceResponse(true, exchangeForObject(HttpMethod.DELETE, endpoint, params));
                case PATCH:
                    return new RemoteApiServiceResponse(true, exchangeForObject(HttpMethod.PATCH, endpoint, params));
                default:
                    throw new IllegalArgumentException(
                        String.format("HTTP method '%s' not supported.", method)
                    );
            }
        } catch (HttpClientErrorException e) {
            return new RemoteApiServiceResponse(false, parseHttpError(e));
        }
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
