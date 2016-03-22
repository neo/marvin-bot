package io.pivotal.singapore.repositories.handlers;

import io.pivotal.singapore.models.Command;
import io.pivotal.singapore.repositories.CommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
