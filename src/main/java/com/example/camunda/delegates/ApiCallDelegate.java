package com.example.camunda.delegates;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

import static org.camunda.spin.Spin.JSON;

@Service
@Slf4j
public class ApiCallDelegate implements JavaDelegate {
  
  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    // Retrieve the process variables
    String apiUrl = (String) execution.getVariable("apiUrl"); // URL of the API
    String method = (String) execution.getVariable("method"); // HTTP method (GET, POST, PUT, DELETE)
    Map<String, String> headersMap = (Map<String, String>) execution.getVariable("headers"); // Headers map
    Map<String, String> paramsMap = (Map<String, String>) execution.getVariable("params"); // Query parameters
    Object requestBody = execution.getVariable("body"); // Body for POST, PUT

    // Prepare headers
    HttpHeaders headers = new HttpHeaders();
    if (headersMap != null) {
      headersMap.forEach(headers::set);
    }

    // Prepare URI with query parameters
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(apiUrl);
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

    // Set the response body and headers as process variables
    execution.setVariable("apiResponseBody", response.getBody());
    execution.setVariable("apiResponseHeaders", response.getHeaders().toSingleValueMap());
  }

}
