package com.newwork.backend;

import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @SpringBootApplication is a convenience annotation that adds:
// @Configuration: Tags the class as a source of bean definitions.
// @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings.
// @ComponentScan: Tells Spring to look for other components, configurations, and services in this package.
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class BackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackendApplication.class, args);
  }

}


// Simple controller to verify the app is running and reachable
// RestController combines @Controller and @ResponseBody, it's preferred for RESTful web services.
@RestController
class HealthController {

  @GetMapping("/api/health")
  public Map<String, String> healthCheck() {
    return Map.of("status", "UP", "backend", "Spring Boot Java 21");
  }
}