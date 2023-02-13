package com.example.MyProject.service;

import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.repo.AssetRepo;
import com.example.MyProject.repo.DeviceRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;

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

    public void deleteAssetByName(String name){
        AssetModel asset = assetRepo.findByName(name).orElseThrow(()-> new EntityNotFoundException("Asset not found"));
        List<DeviceModel> devices = asset.getDevice();
        devices.forEach(deviceModel -> deviceModel.setRelatedAsset(null));
        asset.setDevice(new ArrayList<>());
        deviceRepo.saveAll(devices);
        assetRepo.save(asset);
        assetRepo.delete(asset);
    }

    public void changeActivityState(String name){
        AssetModel asset = assetRepo.findByName(name).orElseThrow(()-> new EntityNotFoundException("Asset not found"));
        asset.setIsActive(!asset.getIsActive());
        assetRepo.save(asset);
    }

    /*public void deleteAssetByName(String name){
        assetRepo.delete(assetRepo.findByName(name).orElseThrow(()-> new EntityNotFoundException("Asset not found")));
    }*/

}
