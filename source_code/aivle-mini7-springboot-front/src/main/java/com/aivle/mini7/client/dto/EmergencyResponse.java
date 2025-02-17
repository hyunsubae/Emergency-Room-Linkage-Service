package com.aivle.mini7.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmergencyResponse {
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("urgency_level")
    private int urgencyLevel;
    
    @JsonProperty("hospitals")
    private List<HospitalResponse> hospitals;
    
    @JsonProperty("fire_stations")
    private List<FireStationResponse> fireStations;
} 