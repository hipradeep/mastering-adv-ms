package com.hipradeep.userservice;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Component
public class YamlValidator implements ApplicationRunner {

    @Value("${custom.message}")
    private  String message;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("application.yml");

            if (inputStream != null) {
                Map<String, Object> obj = yaml.load(inputStream);
                System.out.println("YAML parsed successfully: " + obj);
            } else {
                System.out.println("YAML file not found!");
            }
        } catch (Exception e) {
            System.out.println("YAML syntax error: " + e.getMessage());
        }
    }

    public void print() {
        System.out.println("Message from Config Server: " + message);
    }
}