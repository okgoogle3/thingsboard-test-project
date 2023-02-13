package com.example.MyProject.repo;

import com.example.MyProject.model.TelemetryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelemetryRepo extends JpaRepository<TelemetryModel, Long> {
    Optional<TelemetryModel> findByTimestamp(Long id);
}
