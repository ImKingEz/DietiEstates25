package com.dietiestates25backend.api.controller;

import com.dietiestates25backend.api.dto.ImmobileDTO;
import com.dietiestates25backend.business.entity.Immobile;
import com.dietiestates25backend.business.service.ImmobileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/immobili")
public class ImmobileController {

    @Autowired
    private ImmobileService immobileService;

    @PostMapping("/create")
    public ResponseEntity<Immobile> createImmobile(@RequestBody ImmobileDTO immobileDTO) {
        Immobile savedImmobile = immobileService.saveImmobile(immobileDTO);
        return new ResponseEntity<>(savedImmobile, HttpStatus.CREATED);
    }
}