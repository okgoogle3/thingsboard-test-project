package com.example.MyProject.controller;

import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.repo.AssetRepo;
import com.example.MyProject.repo.DeviceRepo;
import com.example.MyProject.service.DeviceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@RestController
@RequestMapping("/device")
public class DeviceController {
    public final DeviceService deviceService;
    public final DeviceRepo deviceRepo;
    public final AssetRepo assetRepo;

    @GetMapping
    public ResponseEntity<List<DeviceModel>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DeviceModel>> getAllActiveDevices() {
        return ResponseEntity.ok(deviceService.getAllActiveDevices());
    }

    @PutMapping
    public ResponseEntity<Void> createDeviceWithAsset() {
        AssetModel asset = assetRepo.findByName("aboba").orElseThrow(() -> new EntityNotFoundException("Asset not found"));

        DeviceModel device = new DeviceModel("aboba", true, 30.0, 30.0, asset);
        deviceRepo.save(device);
        return ResponseEntity.ok().build();
    }

    /*@GetMapping("/{id}")
    public ResponseEntity<List<DeviceModel>> getTelemetryOfDevice(@PathVariable String id) {

    }

    @GetMapping("/{id}/telemetry")
    public ResponseEntity<List<DeviceModel>> getTelemetryOfDevice(@PathVariable String id) {
        return ResponseEntity.ok(deviceRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<Void> createArticle() {

    }*/

}
