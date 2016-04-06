package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.services.commands.RemoteCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RemoteApiService {

    @Autowired
    RestTemplate restTemplate;

    public RemoteApiServiceResponse call(ICommand command, Map params) {
        return call(command.getMethod(), command.getEndpoint(), params);
    }

    private RemoteApiServiceResponse call(RequestMethod method, String endpoint, Map params) {
        if(method == null) {
            throw new IllegalArgumentException("HTTP method was not defined by the command provider");
        }
        Map<String, String> response = new RemoteCommand(restTemplate, method, endpoint, params).execute();

        return new RemoteApiServiceResponse(response.get("_status") == "success", response);
    }
}
