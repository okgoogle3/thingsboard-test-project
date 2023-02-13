package com.example.MyProject.repo;

import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepo extends JpaRepository<AssetModel, Long> {
    Optional<AssetModel> findById(String id);
    Optional<AssetModel> findByName(String name);
    void deleteByName(String name);
}