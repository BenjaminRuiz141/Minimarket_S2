package com.minimarket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class HolaMundoController {

    @GetMapping("/public/hola")
    public String holaMundo() {
        log.info("Acceso al endpoint de prueba: /public/hola");
        return "¡Hola Mundo!";
    }
}
