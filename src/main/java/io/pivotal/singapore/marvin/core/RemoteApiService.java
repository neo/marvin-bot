package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.commands.ICommand;
import io.pivotal.singapore.marvin.commands.RemoteCommand;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@AllArgsConstructor
@Service
public class RemoteApiService {

    @Autowired
    RestTemplate restTemplate;

    public RemoteApiService() {
    }
    
    public RemoteApiServiceResponse call(ICommand command, Map params) {
        if(command.getMethod() == null) {
            throw new IllegalArgumentException("HTTP method was not defined by the command provider");
        }
        RemoteCommand remoteCommand = new RemoteCommand(restTemplate, command.getMethod(), command.getEndpoint(), params);
        Map<String, String> response = remoteCommand.execute();

        return new RemoteApiServiceResponse(remoteCommand.isSuccessfulExecution(), response, command.getDefaultResponseSuccess(), command.getDefaultResponseFailure());
    }
}
