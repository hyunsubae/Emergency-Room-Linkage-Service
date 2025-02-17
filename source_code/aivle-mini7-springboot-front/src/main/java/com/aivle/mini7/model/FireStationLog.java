package com.aivle.mini7.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "firestation_log")
public class FireStationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number;

    private String name;

    private String address;

    private String headquarters;

    private String phoneNumber;

    private double latitude;

    private double longitude;

    private String type;

    private double distance;

    private int estimatedTime;

    private Integer actualTime;

    @ManyToOne
    @JoinColumn(name = "log_id")
    @JsonBackReference
    private Log log;
}