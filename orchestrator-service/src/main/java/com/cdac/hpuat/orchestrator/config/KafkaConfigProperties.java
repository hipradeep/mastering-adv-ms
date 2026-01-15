package com.cdac.hpuat.orchestrator.config;

public class KafkaConfigProperties {

    // Topics
    public static final String TOPIC_INVENTORY_COMMANDS = "inventory.commands";
    public static final String TOPIC_ISSUE_COMMANDS = "issue.commands";
    public static final String TOPIC_ORCHESTRATOR_REPLIES = "orchestrator.replies";

    // Group IDs
    public static final String GROUP_ID_ORCHESTRATOR = "orchestrator-group";
}
