package io.pivotal.singapore.marvin.commands.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

import io.pivotal.singapore.marvin.commands.Command;
import io.pivotal.singapore.marvin.commands.CommandRepository;

@Component
@RepositoryEventHandler
public class CommandRepositoryEventHandler {

    @Autowired
    CommandRepository repository;

    @HandleBeforeCreate
    public void replaceIdIfCommandNameExists(Command command) {
        Optional<Long> existingId = repository.findOneByName(command.getName()).map(Command::getId);
        Long id = existingId.orElse(command.getId());
        command.setId(id);
    }
}
