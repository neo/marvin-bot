package io.pivotal.singapore.marvin.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArgumentParsedResultList {
    final private List<ArgumentParsedResult> resultSet;

    public ArgumentParsedResultList() {
        resultSet = new ArrayList<>();
    }

    public boolean add(ArgumentParsedResult result) {
        return resultSet.add(result);
    }

    public ArgumentParsedResult getFirst() {
        return get(0);
    }

    public ArgumentParsedResult get(int index) {
        return resultSet.get(index);
    }

    public Map<String, String> getArgumentAndMatchResultMap() {
        return resultSet.stream()
            .collect(Collectors.toMap(ArgumentParsedResult::getArgumentName, ArgumentParsedResult::getMatchResult));
    }

    public boolean hasErrors() {
        return resultSet.stream().anyMatch(ArgumentParsedResult::isFailure);
    }
}
