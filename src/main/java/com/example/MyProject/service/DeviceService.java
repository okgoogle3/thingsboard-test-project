package com.example.MyProject.service;

import com.example.MyProject.controller.DTO.Request.AssetDTO;
import com.example.MyProject.controller.DTO.Request.DeviceDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class DeviceService {

    public final DeviceRepo deviceRepo;
    public final AssetRepo assetRepo;

    public List<DeviceModel> getAllDevices(){
        return deviceRepo.findAll();
    }

    public List<DeviceModel> getAllActiveDevices(){
        List<DeviceModel> devices = deviceRepo.findAll();
        return devices.stream()
                .filter(DeviceModel::getIsActive)
                .toList();
    }

    public void changeActivityState(String name){
        DeviceModel device = deviceRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        device.setIsActive(!device.getIsActive());
        deviceRepo.save(device);
    }

    public String createDevice(DeviceDTO requestedDevice) {
        DeviceModel device = new DeviceModel(requestedDevice.getName(), requestedDevice.getLatitude(), requestedDevice.getLongitude());
        deviceRepo.save(device);
        return device.getName();
    }

    public void relateDeviceToAsset(String deviceName, String assetName){
        DeviceModel device = deviceRepo.findByName(deviceName).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        AssetModel asset = assetRepo.findByName(assetName).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        device.setRelatedAsset(asset);
        deviceRepo.save(device);
    }

    public void deleteDeviceByName(String name){
        DeviceModel device = deviceRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        device.setRelatedAsset(null);
        deviceRepo.save(device);
        /*AssetModel asset = assetRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        List<DeviceModel> devices = asset.getDevice();
        devices.forEach(deviceModel -> deviceModel.setRelatedAsset(null));
        asset.setDevice(new ArrayList<>());
        deviceRepo.saveAll(devices);
        assetRepo.save(asset);
        assetRepo.delete(asset);*/
    }

    public void deleteDevices(){

        List<DeviceModel> devices = deviceRepo.findAll();
        devices.forEach(deviceModel -> deviceModel.setRelatedAsset(null));
        deviceRepo.saveAll(devices);
        deviceRepo.deleteAll();
    }

    public boolean check (String stringPerimeter, double x, double y) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser(stringPerimeter);
        ObjectNode perimeter = mapper.readTree(parser);

        double xA = perimeter.get("xA").doubleValue();
        double yA = perimeter.get("yA").doubleValue();
        double xB = perimeter.get("xA").doubleValue();
        double yB = perimeter.get("yA").doubleValue();
        double xC = perimeter.get("xA").doubleValue();
        double yC = perimeter.get("yA").doubleValue();
        double xD = perimeter.get("xA").doubleValue();
        double yD = perimeter.get("yA").doubleValue();
        return checkPoint(xA, yA, xB, yB, x, y) && checkPoint(xB, yB, xC, yC, x, y)
                && checkPoint(xC, yC, xD, yD, x, y) && checkPoint(xD, yD, xA, yA, x, y);
    }
    private boolean checkPoint(double xA, double yA, double xB, double yB,double x, double y) {
        double ax = xA - x;
        double ay = yA - y;
        double bx = xB - x;
        double by = yB - y;
        boolean s = (ax * by - ay * bx) > 0;
        if (ay < 0 ^ by < 0)
        {
            if (by < 0)
                return s;
            return !s;
        }
        return true;
    }

}
