package com.example.MyProject.controller;

import com.example.MyProject.controller.DTO.AssetDTO;
import com.example.MyProject.model.AssetModel;
import com.example.MyProject.service.AssetService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<AssetModel>> getAllAssets() {
        return ResponseEntity.ok(assetService.getAllAssets());
    }

    @GetMapping("/active")
    public ResponseEntity<List<AssetModel>> getAllActiveAssets() {
        return ResponseEntity.ok(assetService.getAllActiveAssets());
    }

    @GetMapping("/{name}")
    public ResponseEntity<AssetModel> getAssetByName(@PathVariable String name) {
        return ResponseEntity.ok(assetService.getAssetByName(name));
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

    @PostMapping("/{name}/activity")
    public ResponseEntity<Void> changeActivityState(@PathVariable String name){
        try {
            assetService.changeActivityState(name);
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteAsset(@PathVariable String name){
        try {
            assetService.deleteAssetByName(name);
            return ResponseEntity.ok().build();
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }
}
