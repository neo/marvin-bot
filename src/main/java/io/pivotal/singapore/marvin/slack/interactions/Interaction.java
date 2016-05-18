package io.pivotal.singapore.marvin.slack.interactions;

public interface Interaction {
    InteractionResult run(InteractionRequest interactionRequest);
}
