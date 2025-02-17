package com.aivle.mini7.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hospital_log")
public class HospitalLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hospitalName;

    private String address;

    private String emergencyMedicalInstitutionType;

    private String phoneNumber1;

    private String phoneNumber3;

    private double latitude;

    private double longitude;

    private double distance;

    private int estimatedTime;

    private Integer actualTime;

    @ManyToOne
    @JoinColumn(name = "log_id")
    @JsonBackReference
    private Log log;
}

