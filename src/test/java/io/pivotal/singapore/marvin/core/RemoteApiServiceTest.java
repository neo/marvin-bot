package io.pivotal.singapore.marvin.core;

import io.pivotal.singapore.marvin.commands.Command;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RemoteApiServiceTest {
    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    RemoteApiService remoteApiService = new RemoteApiService();

    @Test
    public void testRemoteCommandCollaboration() {
        String endpoint = "http://www.example.com";

        Command command = new Command("test", endpoint);

        HashMap<String, String> params;
        params = new HashMap<>();
        params.put("foo", "bar");

        HashMap<String, String> response = new HashMap<>();
        response.put("status", "yay! it works!");
        when(restTemplate.postForObject(endpoint, params, HashMap.class)).thenReturn(response);

        RemoteApiServiceResponse remoteApiServiceResponse = remoteApiService.call(command, params);
        assertThat(remoteApiServiceResponse.isSuccessful(), is(true));
    }
}
