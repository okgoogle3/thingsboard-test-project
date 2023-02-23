package com.example.MyProject.controller;

import com.example.MyProject.controller.DTO.Request.AssetDTO;
import com.example.MyProject.controller.DTO.Request.TemperatureDTO;
import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.service.AssetService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@RestController
@RequestMapping("/asset")
public class AssetController {
    public final AssetService assetService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AssetModel>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AssetModel>> getAllActiveAssets() {
        return ResponseEntity.ok(assetService.getAllActiveAssets());
    }

    @GetMapping("/{name}")
    public ResponseEntity<AssetModel> getAssetByName(@PathVariable String name) {
        try{
            return ResponseEntity.ok(assetService.getAssetByName(name));
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{name}/devices")
    public ResponseEntity<List<DeviceModel>> getDevicesByAsset(@PathVariable String name) {
        try{
            return ResponseEntity.ok(assetService.getDevicesByAsset(name));
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{name}/devices/active")
    public ResponseEntity<List<DeviceModel>> getActiveDevicesByAsset(@PathVariable String name) {
        try{
            return ResponseEntity.ok(assetService.getActiveDevicesByAsset(name));
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    public ResponseEntity<Void> createAsset(@RequestBody AssetDTO asset) {
        try {
            String location = String.format("/asset/%s", assetService.createAsset(asset));
            return ResponseEntity.created(URI.create(location)).build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{name}/perimeter")
    public ResponseEntity<Void> setPerimeter(@RequestBody ObjectNode perimeter, @PathVariable String name) {
        try {
            assetService.setPerimeter(name, perimeter);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{name}/activity")
    public ResponseEntity<Void> changeActivityState(@PathVariable String name){
        try {
            assetService.changeActivityState(name);
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteAsset(@PathVariable String name){
        try {
            assetService.deleteAssetByName(name);
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{name}/temperature_threshold")
    public ResponseEntity<Void> changeTemperatureThreshold(@PathVariable String name, @RequestBody TemperatureDTO temperatureThreshold){
        try {
            assetService.changeTemperatureThreshold(name, temperatureThreshold.getTemperature());
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
