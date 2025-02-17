package com.aivle.mini7.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FireStationResponse {
    @JsonProperty("번호")
    private int number;
    
    @JsonProperty("소방서 및 안전센터명")
    private String name;
    
    @JsonProperty("주소")
    private String address;
    
    @JsonProperty("상위 본부명")
    private String headquarters;
    
    @JsonProperty("전화번호")
    private String phoneNumber;
    
    @JsonProperty("X좌표")
    private double latitude;
    
    @JsonProperty("Y좌표")
    private double longitude;
    
    @JsonProperty("유형")
    private String type;
    
    @JsonProperty("도로 거리")
    private double distance;
    
    @JsonProperty("소요시간")
    private int estimatedTime;
} 