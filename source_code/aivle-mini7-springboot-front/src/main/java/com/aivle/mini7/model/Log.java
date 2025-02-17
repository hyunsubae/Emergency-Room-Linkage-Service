package com.aivle.mini7.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "log")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String datetime;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int urgencyLevel;

    private String request;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private double latitude;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private double longitude;

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<HospitalLog> hospitalLogs;

    @OneToMany(mappedBy = "log", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FireStationLog> fireStationLogs;
}