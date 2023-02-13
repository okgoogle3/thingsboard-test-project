package com.example.MyProject.model;

import com.example.MyProject.model.CompositeKey.TelemetryId;
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
@IdClass(TelemetryId.class)
public class TelemetryModel {
    @Id
    private Long timestamp;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceModel device;
    private String key1;
    private Double value1;


    public TelemetryModel(String key1, Double value1, DeviceModel device) {
        this.timestamp = System.currentTimeMillis();
        this.key1 = key1;
        this.value1 = value1;
        this.device = device;
    }
}
