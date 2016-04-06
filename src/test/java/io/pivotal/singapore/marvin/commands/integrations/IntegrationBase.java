package io.pivotal.singapore.marvin.commands.integrations;

import io.pivotal.singapore.MarvinApplication;
import io.pivotal.singapore.marvin.commands.CommandRepository;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MarvinApplication.class)
@WebAppConfiguration
@ActiveProfiles(profiles = "test")
@IntegrationTest("server.port:0")
public abstract class IntegrationBase {

    @Autowired protected CommandRepository commandRepository;

    @Value("${local.server.port}")
    protected int port;

    @Value("${api.slack.token}")
    protected String SLACK_TOKEN;

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;
}
