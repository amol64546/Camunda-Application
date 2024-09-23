package com.example.camunda.delegates;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.camunda.spin.Spin.JSON;

@Service
@Slf4j
public class ApiCallDelegate implements JavaDelegate {


  @Override
  public void execute(DelegateExecution execution) {
    log.info("----------ApiCallDelegate----------");
    String apiUrl = (String) execution.getVariable("apiUrl");
    RestTemplate restTemplate = new RestTemplate();
    String response = restTemplate.getForObject(apiUrl, String.class);

    SpinJsonNode jsonNode = JSON(response);

    log.info("API Response: {}", jsonNode);
    execution.setVariable("apiResponse", jsonNode);
  }
}
