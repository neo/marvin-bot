package io.pivotal.singapore.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.commands.arguments.serializers.ArgumentsDeserializerJson;
import io.pivotal.singapore.marvin.commands.arguments.serializers.ArgumentsSerializerJson;
import io.pivotal.singapore.repositories.converters.ArgumentListConverter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.persistence.*;

@Entity
@Data
@Table(name = "sub_commands")
public class SubCommand implements ICommand {
    @Id
    @SequenceGenerator(name = "pk_sequence", sequenceName = "commands_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_sequence")
    private long id;
    private String name;
    private String endpoint;
    @Getter @Setter private String defaultResponseSuccess;
    @Getter @Setter private String defaultResponseFailure;

    private RequestMethod method;

    @Convert(converter = ArgumentListConverter.class)
    @JsonDeserialize(converter = ArgumentsDeserializerJson.class)
    @JsonSerialize(converter = ArgumentsSerializerJson.class)
    @Getter @Setter private Arguments arguments = new Arguments();


    @Override
    public boolean requiresEndpoint() {
        return true;
    }

    @Override
    public boolean requiresMethod() {
        return true;
    }
}
