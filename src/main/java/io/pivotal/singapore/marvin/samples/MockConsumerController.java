package io.pivotal.singapore.marvin.samples;

import lombok.Synchronized;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MockConsumerController {
    private static Map<String, Integer> counters = new HashMap<>();

    @RequestMapping(value = "/counter", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> counter(@RequestParam String endpoint) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("counter", counters.getOrDefault(endpoint, 0));
        return response;
    }

    @RequestMapping(value = "/dummy", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String dummy() throws Exception {
        incrementCounter("dummy");

        throw new Exception("Dumb dumb!");
    }

    @RequestMapping(value = "/mocker", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> index(@RequestBody Map<String, Object> params) {
        incrementCounter("mocker");

        String location = (String) params.get("location");
        String response_message = "Its " + new Date();

        if (location != null) {
            if(location.equals("England")) {
                response_message = "It is Tea 'o' Clock";
            }
            else if(location.equals("Australia")) {
                response_message = "It's Beer 'o' Clock";
            }
            else if(location.equals("Russia")) {
                response_message = "It'z Vodka 'o' Clock";
            }
        }

        HashMap<String,String> response = new HashMap<String, String>();
        response.put("message", response_message);
        response.put("message_type", "channel");

        return response;
    }

    @Synchronized
    private void incrementCounter(String endpoint) {
        int counter = counters.getOrDefault(endpoint, 0);
        counter++;
        counters.put(endpoint, counter);
    }

}
