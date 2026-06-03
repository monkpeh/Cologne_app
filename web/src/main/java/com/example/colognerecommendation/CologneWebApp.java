package com.example.colognerecommendation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point for the Cologne Advisor web application.
 *
 * <p>{@code @SpringBootApplication} is a convenience annotation that combines:
 * <ul>
 *   <li>{@code @Configuration} – marks this class as a source of Spring bean definitions</li>
 *   <li>{@code @EnableAutoConfiguration} – tells Spring Boot to auto-configure beans based on
 *       the libraries present on the classpath (e.g. Thymeleaf, Security, JPA)</li>
 *   <li>{@code @ComponentScan} – scans all sub-packages for {@code @Component},
 *       {@code @Service}, {@code @Controller}, etc.</li>
 * </ul>
 */
@SpringBootApplication
public class CologneWebApp {

    /**
     * Launches the embedded Tomcat server and starts the Spring application context.
     *
     * @param args optional command-line arguments passed through to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(CologneWebApp.class, args);
    }
}
