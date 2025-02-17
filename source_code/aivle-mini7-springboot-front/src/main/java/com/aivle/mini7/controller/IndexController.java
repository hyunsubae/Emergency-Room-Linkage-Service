package com.aivle.mini7.controller;

import com.aivle.mini7.client.api.FastApiClient;
import com.aivle.mini7.client.dto.EmergencyResponse;
import com.aivle.mini7.dto.LogDto;
import com.aivle.mini7.service.LogService;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequiredArgsConstructor
@Slf4j
public class IndexController {

    private final FastApiClient fastApiClient;
    private final LogService logService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/recommend_hospital")
    public ModelAndView recommend_hospital(@RequestParam("request") String request, @RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude) {
        EmergencyResponse response = fastApiClient.getEmergency(request, latitude, longitude);
        
        ModelAndView mv = new ModelAndView();
        
        if (response.getUrgencyLevel() <= 2) {
            mv.setViewName("recommend_hospital");
        } else {
            mv.setViewName("no_emergency");
        }
        
        mv.addObject("response", response);
        mv.addObject("hospitalList", response.getHospitals());
        mv.addObject("fireStationList", response.getFireStations());

        LogDto logDTO = new LogDto();
        logDTO.setDatetime(String.valueOf(new Date()));
        logDTO.setUrgencyLevel(response.getUrgencyLevel());
        logDTO.setRequest(request);
        logDTO.setLatitude(latitude);
        logDTO.setLongitude(longitude);
        logDTO.setHospitals(response.getHospitals());
        logDTO.setFireStations(response.getFireStations());

        logService.saveLog(logDTO);

        return mv;
    }
}


