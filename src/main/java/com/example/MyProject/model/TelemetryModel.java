package com.example.MyProject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "telemetry")
public class TelemetryModel {
    @Id
    private Long timestamp;
    private String key1;
    private Double value1;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceModel device;

    public TelemetryModel(String key1, Double value1, DeviceModel device) {
        this.timestamp = System.currentTimeMillis();
        this.key1 = key1;
        this.value1 = value1;
        this.device = device;
    }
}
