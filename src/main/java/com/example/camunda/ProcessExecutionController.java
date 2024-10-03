package com.example.camunda;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
@Slf4j
public class ProcessExecutionController {

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  private HistoryService historyService;



  @PostMapping("/deploy")
  public ResponseEntity<String> deployProcess(@RequestBody String bpmnXml) {
    Deployment deployment = repositoryService.createDeployment()
      .addString("dynamic-process.bpmn", bpmnXml)
      .deploy();
    return ResponseEntity.ok("Process deployed with ID: " + deployment.getId());
  }

  @PostMapping("/start/{processDefinitionId}")
  public ResponseEntity<Map<String, Object>> startProcess(@PathVariable String processDefinitionId,
                                          @RequestBody Map<String, Object> variables) {
    log.info("------------ /start/{}", processDefinitionId);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, variables);
    // Return the response to the user
    return ResponseEntity.ok(getHistoricVariables(processInstance.getId()));
  }

  public Map<String, Object> getHistoricVariables(String processInstanceId) {
    // Fetch all historic variables for the given process instance
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
      .processInstanceId(processInstanceId)
      .list();

    // Convert the list of variables into a map for easier access
    Map<String, Object> variableMap = new HashMap<>();
    for (HistoricVariableInstance variable : variables) {
      variableMap.put(variable.getName(), variable.getValue());
    }

    return variableMap;
  }

}
