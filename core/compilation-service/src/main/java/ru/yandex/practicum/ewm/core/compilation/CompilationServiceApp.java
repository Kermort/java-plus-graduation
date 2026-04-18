package ru.yandex.practicum.ewm.core.compilation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.yandex.practicum")
public class CompilationServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CompilationServiceApp.class, args);
    }
}
