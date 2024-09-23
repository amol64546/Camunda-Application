package com.example.camunda.controllers;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class ProcessExecutionController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;


    @PostMapping("/deploy")
    public ResponseEntity<String> deployProcess(@RequestBody String bpmnXml) {
        try {
            Deployment deployment = repositoryService.createDeployment()
              .addString("dynamic-process.bpmn", bpmnXml)
              .deploy();

            return ResponseEntity.ok("Process deployed with ID: " + deployment.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deploying process: " + e.getMessage());
        }
    }


    @PostMapping("/start/{processDefinitionId}")
    public ResponseEntity<String> startProcess(@PathVariable String processDefinitionId, @RequestBody Map<String, Object> variables) {
        try {
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, variables);

            return ResponseEntity.ok("Process instance started with ID: " + processInstance.getId());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error starting process: " + e.getMessage());
        }
    }

}
