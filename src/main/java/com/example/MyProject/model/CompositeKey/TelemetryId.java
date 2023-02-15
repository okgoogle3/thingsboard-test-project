package com.example.MyProject.model.CompositeKey;

import com.example.MyProject.model.DeviceModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
public class TelemetryId implements Serializable {
    private Long timestamp;
    private DeviceModel device;
    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelemetryId that = (TelemetryId) o;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(device, that.device) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, device, type);
    }
}
