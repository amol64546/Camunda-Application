package com.example.camunda;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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
@RequiredArgsConstructor
public class ProcessExecutionController {

  private final RuntimeService runtimeService;

  private final RepositoryService repositoryService;

  private final HistoryService historyService;



  @PostMapping("/deploy")
  public ResponseEntity<String> deployProcess(@RequestBody String bpmnXml) {
    Deployment deployment = repositoryService.createDeployment()
      .addString("dynamic-process.bpmn", bpmnXml)
      .deploy();

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
      .deploymentId(deployment.getId())
      .singleResult();

    return ResponseEntity.ok("Process Definition ID: " + processDefinition.getId());
  }

  @PostMapping("/start/{processDefinitionId}")
  public ResponseEntity<Map<String, Object>> startProcess(@PathVariable String processDefinitionId,
                                          @RequestBody Map<String, Object> variables) {
    log.info("------------ /start/{}", processDefinitionId);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, variables);
    return ResponseEntity.ok(getHistoricVariables(processInstance.getId()));
  }

  public Map<String, Object> getHistoricVariables(String processInstanceId) {
    List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
      .processInstanceId(processInstanceId)
      .list();

    Map<String, Object> variableMap = new HashMap<>();
    for (HistoricVariableInstance variable : variables) {
      variableMap.put(variable.getName(), variable.getValue());
    }

    return variableMap;
  }

}
