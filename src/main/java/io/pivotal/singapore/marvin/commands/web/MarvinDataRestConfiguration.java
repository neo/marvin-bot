package io.pivotal.singapore.marvin.commands.web;

import io.pivotal.singapore.marvin.commands.CommandValidator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class MarvinDataRestConfiguration extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
        v.addValidator("beforeCreate", new CommandValidator());
        v.addValidator("beforeSave", new CommandValidator());
    }
}
