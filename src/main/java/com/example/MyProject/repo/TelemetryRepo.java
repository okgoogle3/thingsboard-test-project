package com.example.MyProject.repo;

import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.model.TelemetryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelemetryRepo extends JpaRepository<TelemetryModel, Long> {
    Optional<TelemetryModel> findByTimestamp(Long id);
    @Query(value = "SELECT a FROM TelemetryModel a WHERE a.timestamp>=:startTime AND a.timestamp<=:endTime AND a.device=:device AND a.type=:type")
    List<TelemetryModel> findAllByDeviceAndTimestampAndType(DeviceModel device, Long startTime, Long endTime, String type);
}
