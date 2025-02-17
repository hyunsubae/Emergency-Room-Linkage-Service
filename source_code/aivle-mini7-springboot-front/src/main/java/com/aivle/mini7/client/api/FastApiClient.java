package com.aivle.mini7.client.api;


import com.aivle.mini7.client.dto.EmergencyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// import java.util.List;

/**
 * FastApiClient
 * @app.get("/hospital/{request}/{latitude}/{longitude}") 를 호출한다.
 */
@FeignClient(name = "fastApiClient", url = "${hospital.api.host}")
public interface FastApiClient {

     @GetMapping("/emergency/analyze")
     public EmergencyResponse getEmergency(@RequestParam("text") String request, @RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude);

}
