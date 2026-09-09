package com.tienda.ms_catalogo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    @GetMapping
    public String obtenerCatalogo() {
        return "¡Felicidades! Tu API, base de datos y seguridad con Azure están completamente integrados.";
    }
}