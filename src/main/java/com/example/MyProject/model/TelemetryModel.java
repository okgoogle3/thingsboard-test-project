package com.example.MyProject.model;

import com.example.MyProject.model.CompositeKey.TelemetryId;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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
    @JsonBackReference(value = "device-telemetry-relation")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceModel device;
    @Id
    private String type;
    private Double value;


    public TelemetryModel(DeviceModel device, String type, Double value) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.value = value;
        this.device = device;
    }
}
