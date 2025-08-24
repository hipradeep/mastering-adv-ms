package com.hipradeep.userservice;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Component
public class YamlValidator implements ApplicationRunner {

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
}