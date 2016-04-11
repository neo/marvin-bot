package io.pivotal.singapore.marvin.commands;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.pivotal.singapore.marvin.commands.arguments.Arguments;
import io.pivotal.singapore.marvin.commands.arguments.serializers.ArgumentsDeserializerJson;
import io.pivotal.singapore.marvin.commands.arguments.serializers.ArgumentsSerializerJson;
import io.pivotal.singapore.marvin.commands.arguments.ArgumentListConverter;
import io.pivotal.singapore.marvin.commands.default_response.DefaultResponse;
import io.pivotal.singapore.marvin.commands.default_response.serializers.DefaultResponseConverter;
import io.pivotal.singapore.marvin.commands.default_response.serializers.DefaultResponseDeserializerJson;
import io.pivotal.singapore.marvin.commands.default_response.serializers.DefaultResponseSerializerJson;
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

    @Column(name = "default_responses")
    @Convert(converter = DefaultResponseConverter.class)
    @JsonDeserialize(converter = DefaultResponseDeserializerJson.class)
    @JsonSerialize(converter = DefaultResponseSerializerJson.class)
    @Getter @Setter private DefaultResponse defaultResponse = new DefaultResponse();

    @Override
    public boolean requiresEndpoint() {
        return true;
    }

    @Override
    public boolean requiresMethod() {
        return true;
    }
}
