package com.aivle.mini7.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class DirectionController {
    
    @GetMapping("/directions")
    public ResponseEntity<String> getDirections(
            @RequestParam("startLat") double startLat,
            @RequestParam("startLng") double startLng,
            @RequestParam("endLat") double endLat,
            @RequestParam("endLng") double endLng) {
        
        String apiUrl = String.format(
            "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving?start=%f,%f&goal=%f,%f",
            startLng, startLat, endLng, endLat);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", "opyx24jxvh");
        headers.set("X-NCP-APIGW-API-KEY", "Kwvhoiue5AkAns5UPYJ3eNfVdzwFnQJg4Bj6D8q6");
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class
        );
        
        return response;
    }
} 