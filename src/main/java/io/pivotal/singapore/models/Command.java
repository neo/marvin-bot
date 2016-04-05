package io.pivotal.singapore.models;

import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "commands")
public class Command implements ICommand {

    @Id
    @SequenceGenerator(name = "pk_sequence", sequenceName = "commands_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_sequence")
    @Getter @Setter private long id;

    @Column(unique = true)
    @Getter @Setter private String name;
    @Getter @Setter private String endpoint;
    @Getter @Setter private String defaultResponseSuccess;
    @Getter @Setter private String defaultResponseFailure;

    @Getter @Setter private RequestMethod method;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "COMMAND_ID", referencedColumnName = "ID")
    @Getter @Setter List<SubCommand> subCommands = new ArrayList<>();

    public Command(String name, String endpoint) {
        this.name = name;
        this.endpoint = endpoint;
        this.method = RequestMethod.POST;
        this.subCommands = new ArrayList<>();
    }

    public Command() {} // to make JPA happy....

    public Optional<ICommand> findSubCommand(String name) {
        for(SubCommand subCommand: subCommands) {
            if(subCommand.getName().equals(name))
                return Optional.of(subCommand);
        }

        return Optional.empty();
    }

    public Arguments getArguments(){
        return new Arguments();
    }
}
