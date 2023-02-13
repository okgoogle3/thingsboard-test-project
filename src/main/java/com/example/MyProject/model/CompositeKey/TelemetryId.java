package com.example.MyProject.model.CompositeKey;

import com.example.MyProject.model.DeviceModel;

import java.io.Serializable;
import java.util.Objects;

public class TelemetryId implements Serializable {
    private Long timestamp;
    private DeviceModel deviceModel;

    public TelemetryId(){

    }

    public TelemetryId(Long timestamp, DeviceModel deviceModel){
        this.timestamp = timestamp;
        this.deviceModel = deviceModel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelemetryId that = (TelemetryId) o;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(deviceModel, that.deviceModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, deviceModel);
    }
}
