package com.example.camunda;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class ApiTaskHandler implements JavaDelegate, ExternalTaskHandler {

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    log.info("------------- start of JavaDelegate -----------------");
    try {
      // Retrieve the process variables
      String url = (String) execution.getVariable("url"); // URL of the API
      String method = (String) execution.getVariable("method"); // HTTP method (GET, POST, PUT, DELETE)
      Map<String, String> headersMap = (Map<String, String>) execution.getVariable("headers"); // Headers map
      Map<String, String> paramsMap = (Map<String, String>) execution.getVariable("params"); // Query parameters
      Object requestBody = execution.getVariable("body"); // Body for POST, PUT

      ResponseEntity<Object> response = apiOperation(url, method, headersMap, paramsMap, requestBody);

      // Set the response body and headers as process variables
      execution.setVariable("apiResponseBody", response.getBody());
      execution.setVariable("apiResponseHeaders", response.getHeaders().toSingleValueMap());
      log.info("------------- end of JavaDelegate -----------------");
    } catch (RuntimeException e) {
      log.error(e.getMessage());
      throw e;
    }
  }

  @Override
  public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
    log.info("------------- start of ExternalTaskHandler -----------------");
    try {
      // Retrieve the process variables
      String url = externalTask.getVariable("url");
      String method = externalTask.getVariable("method");
      Map<String, String> headersMap = externalTask.getVariable("headers");
      Map<String, String> paramsMap = externalTask.getVariable("params");
      Object requestBody = externalTask.getVariable("body");

      ResponseEntity<Object> response = apiOperation(url, method, headersMap, paramsMap, requestBody);

      // Set the response body and headers as process variables
      Map<String, Object> runtimeVariables = new HashMap<>();
      runtimeVariables.put("apiResponseBody", response.getBody());
      runtimeVariables.put("apiResponseHeaders", response.getHeaders().toSingleValueMap());

      externalTaskService.complete(externalTask, runtimeVariables);
      log.info("------------- end of ExternalTaskHandler -----------------");
    } catch (RuntimeException e) {
      log.error(e.getMessage());
      throw e;
    }
  }

  public ResponseEntity<Object> apiOperation(String url, String method, Map<String, String> headersMap,
                                             Map<String, String> paramsMap, Object requestBody) {
    // Prepare headers
    HttpHeaders headers = new HttpHeaders();
    if (headersMap != null) {
      headersMap.forEach(headers::set);
    }

    // Prepare URI with query parameters
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url);
    if (paramsMap != null) {
      paramsMap.forEach(uriBuilder::queryParam);
    }
    URI uri = uriBuilder.build().toUri();

    // Prepare request entity
    HttpEntity<Object> entity = new HttpEntity<>(requestBody, headers);

    // Execute the API request based on the HTTP method
    ResponseEntity<Object> response = switch (method.toUpperCase()) {
      case "GET" -> restTemplate.exchange(uri, HttpMethod.GET, entity, Object.class);
      case "POST" -> restTemplate.exchange(uri, HttpMethod.POST, entity, Object.class);
      case "PUT" -> restTemplate.exchange(uri, HttpMethod.PUT, entity, Object.class);
      case "DELETE" -> restTemplate.exchange(uri, HttpMethod.DELETE, entity, Object.class);
      default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    };

    // Log the response
    log.info("API Response Body: {}", response.getBody());
    log.info("API Response Headers: {}", response.getHeaders());

//    log.info("--------Thread waiting----------");
//    Thread.sleep(5000);
    return response;
  }
}
