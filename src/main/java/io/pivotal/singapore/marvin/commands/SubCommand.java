package io.pivotal.singapore.marvin.commands;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentListConverter;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.commands.arguments.serializers.ArgumentsDeserializerJson;
import io.pivotal.singapore.marvin.commands.arguments.serializers.ArgumentsSerializerJson;
import io.pivotal.singapore.marvin.commands.default_responses.DefaultResponses;
import io.pivotal.singapore.marvin.commands.default_responses.serializers.DefaultResponsesConverter;
import io.pivotal.singapore.marvin.commands.default_responses.serializers.DefaultResponsesDeserializerJson;
import io.pivotal.singapore.marvin.commands.default_responses.serializers.DefaultResponsesSerializerJson;
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
    // Putting this on an entity violates Single Responsiblity Principle
    // as well as puts HTTP code in my domain layer.
    // Gabe is working on a guide that will show a different
    // way to do this.
    @JsonDeserialize(converter = ArgumentsDeserializerJson.class)
    @JsonSerialize(converter = ArgumentsSerializerJson.class)
    @Getter @Setter private Arguments arguments = new Arguments();

    @Convert(converter = DefaultResponsesConverter.class)
    @JsonDeserialize(converter = DefaultResponsesDeserializerJson.class)
    @JsonSerialize(converter = DefaultResponsesSerializerJson.class)
    @Getter @Setter private DefaultResponses defaultResponses = new DefaultResponses();

    @Override
    public boolean requiresEndpoint() {
        return true;
    }

    @Override
    public boolean requiresMethod() {
        return true;
    }
}
