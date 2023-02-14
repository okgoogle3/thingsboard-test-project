package com.example.MyProject.service;

import com.example.MyProject.controller.DTO.Request.AssetDTO;
import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.repo.AssetRepo;
import com.example.MyProject.repo.DeviceRepo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AssetService {
    public final AssetRepo assetRepo;
    public final DeviceRepo deviceRepo;

    public List<AssetModel> getAllAssets(){
        return assetRepo.findAll();
    }

    public List<AssetModel> getAllActiveAssets(){
        List<AssetModel> assets = assetRepo.findAll();
        return assets.stream().filter(AssetModel::getIsActive).toList();
    }

    public AssetModel getAssetByName(String name){
        return assetRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
    }

    public void deleteAssetByName(String name){
        AssetModel asset = assetRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        List<DeviceModel> devices = asset.getDevice();
        devices.forEach(deviceModel -> deviceModel.setRelatedAsset(null));
        asset.setDevice(new ArrayList<>());
        deviceRepo.saveAll(devices);
        assetRepo.save(asset);
        assetRepo.delete(asset);
    }

    public void changeActivityState(String name){
        AssetModel asset = assetRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        asset.setIsActive(!asset.getIsActive());
        assetRepo.save(asset);
    }

    public String createAsset(AssetDTO requestedAsset) {
        String perimeter = requestedAsset.getPerimeter().toString();
        AssetModel asset = new AssetModel(requestedAsset.getName(), perimeter);
        assetRepo.save(asset);
        return asset.getName();
    }

    public ObjectNode testMethod() throws IOException {
        String jsonString = assetRepo.findByName("aboba").orElseThrow(() -> new EntityNotFoundException("Asset not found")).getPerimeter();

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser(jsonString);
        return mapper.readTree(parser);
    }

    public void setPerimeter(String name, ObjectNode perimeter){
        AssetModel asset = assetRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        asset.setPerimeter(perimeter.toString());
        assetRepo.save(asset);
    }

    public List<DeviceModel> getDevicesByAsset(String name){
        AssetModel asset = assetRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        return asset.getDevice();
    }

    public List<DeviceModel> getActiveDevicesByAsset(String name){
        AssetModel asset = assetRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        return asset.getDevice().stream().filter(DeviceModel::getIsActive).collect(Collectors.toList());
    }
}
