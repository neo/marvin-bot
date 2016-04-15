package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.RemoteCommand;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@AllArgsConstructor
@Service
public class RemoteApiService {

//    @Autowired
    RestTemplate restTemplate;

    @Autowired
    public RemoteApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RemoteApiServiceResponse call(ICommand command, Map params) {
        if(command.getMethod() == null) {
            throw new IllegalArgumentException("HTTP method was not defined by the command provider");
        }
        RemoteCommand remoteCommand = new RemoteCommand(restTemplate, command, params);
        Map<String, String> response = remoteCommand.execute();

        // Why deprecate this and not just get rid of it?
        return new RemoteApiServiceResponse(
            remoteCommand.isSuccessfulExecution(),
            response,
            command
        );
    }
}
