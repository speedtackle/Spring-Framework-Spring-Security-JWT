package dio.security.jwt.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    @GetMapping
    public String welcome(){
        return "Bem vindo ao meu Spring Boot API";
    }
    @GetMapping("/users")
    public String users(){
        return "Bem vindo ao meu Spring Boot API, user";
    }
    @GetMapping("/managers")
    public String managers(){
        return "Bem vindo ao meu Spring Boot, manager";
    }
}
