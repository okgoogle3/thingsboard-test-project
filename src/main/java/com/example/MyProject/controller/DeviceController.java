package com.example.MyProject.controller;

import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.repo.AssetRepo;
import com.example.MyProject.repo.DeviceRepo;
import com.example.MyProject.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @PostMapping
    public ResponseEntity<Void> createDeviceWithAsset() {
        Optional<AssetModel> optionalAsset = assetRepo.findByName("aboba");
        AssetModel asset;
        if(optionalAsset.isPresent()) {
            asset = optionalAsset.get();
            DeviceModel device = new DeviceModel("aboba", true, 30.0, 30.0);
            deviceRepo.save(device);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
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
