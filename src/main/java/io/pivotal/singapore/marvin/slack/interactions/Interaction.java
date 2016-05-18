package io.pivotal.singapore.marvin.slack.interactions;

interface Interaction {
    InteractionResult run(InteractionRequest interactionRequest);
}
