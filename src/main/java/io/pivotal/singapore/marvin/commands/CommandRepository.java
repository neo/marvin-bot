package io.pivotal.singapore.marvin.commands;


// Don't need this because you are in the commands package.
// I am very intolerant with cruft in codebases.
// It's hard enough understanding code that needs to be there.
// I would have to stop and ask myself why and that breaks my
// flow.
//import io.pivotal.singapore.marvin.commands.Command;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommandRepository extends JpaRepository<Command, Long>{
    Optional<Command> findOneByName(String name);
}
