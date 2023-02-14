package com.example.MyProject.controller;

import com.example.MyProject.controller.DTO.Request.AssetDTO;
import com.example.MyProject.controller.DTO.Request.DeviceDTO;
import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.repo.AssetRepo;
import com.example.MyProject.repo.DeviceRepo;
import com.example.MyProject.service.DeviceService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @PostMapping("/{name}/activity")
    public ResponseEntity<Void> changeActivityState(@PathVariable String name){
        try {
            deviceService.changeActivityState(name);
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String name){
        try {
            deviceService.deleteDeviceByName(name);
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<Void> createDevice(@RequestBody DeviceDTO device) {
        try {
            String location = String.format("/device/%s", deviceService.createDevice(device));
            return ResponseEntity.created(URI.create(location)).build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{name}/relate")
    public ResponseEntity<Void> relateDeviceToAsset (@PathVariable String name, @RequestBody ObjectNode assetNameNode){
        try {
            deviceService.relateDeviceToAsset(name, assetNameNode.get("name").asText());
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{name}/check")
    public ResponseEntity<Boolean> checkIfDeviceInAssetPerimeter(@PathVariable String name) {
        try {
            AssetModel asset = assetRepo.findByName("aboba").orElseThrow(() -> new EntityNotFoundException("Asset not found"));
            DeviceModel device = deviceRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
            String perimeter = asset.getPerimeter();
            return ResponseEntity.ok(deviceService.checkIfDeviceInAssetPerimeter(perimeter, device.getLatitude(), device.getLongitude()));
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (IOException e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{name}/remove_relation")
    public ResponseEntity<Void> removeRelationOnDevice(@PathVariable String name) {
        try {
            deviceService.removeRelationOnDevice(name);
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteDevices(){
        try {
            deviceService.deleteDevices();
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }


    /*@GetMapping("/{id}")
    public ResponseEntity<List<DeviceModel>> getTelemetryOfDevice(@PathVariable String id) {

    }

    @GetMapping("/{id}/telemetry")
    public ResponseEntity<List<TelemetryModel>> getTelemetryOfDevice(@PathVariable String id) {
        return ResponseEntity.ok(deviceRepo.findAll());
    }

    @PostMapping
    public ResponseEntity<Void> createArticle() {

    }*/

}
