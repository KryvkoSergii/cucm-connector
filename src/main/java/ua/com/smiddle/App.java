package ua.com.smiddle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author ksa on 20.12.16.
 * @project cucm-connector
 */
@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@EnableScheduling
@ComponentScan(basePackages = "ua.com.smiddle")
@PropertySource("classpath:application.properties")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }


}
