package br.com.wslima.javaspringbootrestapi.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping
    private ResponseEntity<String> getUsers(){
        return ResponseEntity.ok("Hello World");
    }

}
