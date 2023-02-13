package com.example.MyProject.repo;

import com.example.MyProject.model.DeviceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceRepo extends JpaRepository<DeviceModel, Long> {
    Optional<DeviceModel> findById(String id);
    Optional<DeviceModel> findByName(String name);
}
