package com.example.camunda;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ExternalTaskClientRunner implements CommandLineRunner {

    private final ApiTaskHandler apiTaskHandler;

    public ExternalTaskClientRunner(ApiTaskHandler apiTaskHandler) {
        this.apiTaskHandler = apiTaskHandler;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create the external task client
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")  // Camunda REST API base URL
                .build();

        // Subscribe to the topic and assign the handler
        client.subscribe("apiTaskTopic")  // Replace with your BPMN topic
                .lockDuration(1000)  // Lock duration in milliseconds
                .handler(apiTaskHandler)
                .open();
    }
}
