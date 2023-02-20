package com.example.MyProject.service;

import com.example.MyProject.bot.TgBot;
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
import java.util.List;

@RequiredArgsConstructor
@Service
public class DeviceService {

    public final DeviceRepo deviceRepo;
    public final AssetRepo assetRepo;
    public final TgBot bot;

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

    public void relateDeviceToAsset(String deviceName, String assetName) throws IOException {
        DeviceModel device = deviceRepo.findByName(deviceName).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        AssetModel asset = assetRepo.findByName(assetName).orElseThrow(() -> new EntityNotFoundException("Asset not found"));
        device.setRelatedAsset(asset);
        device.setIsInAssetPerimeter(checkIfDeviceInAssetPerimeter(asset.getPerimeter(), device.getLatitude(), device.getLongitude()));
        deviceRepo.save(device);
    }

    public void removeRelationOnDevice(String deviceName){
        DeviceModel device = deviceRepo.findByName(deviceName).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        device.setRelatedAsset(null);
        deviceRepo.save(device);
    }

    public void deleteDeviceByName(String name){
        DeviceModel device = deviceRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        device.setRelatedAsset(null);
        deviceRepo.save(device);
    }

    public void deleteDevices(){
        List<DeviceModel> devices = deviceRepo.findAll();
        devices.forEach(deviceModel -> deviceModel.setRelatedAsset(null));
        deviceRepo.saveAll(devices);
        deviceRepo.deleteAll();
    }

    public void relocate(String name, double latitude, double longitude) throws IOException {
        DeviceModel device = deviceRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        device.setLatitude(latitude);
        device.setLongitude(longitude);
        if(device.getRelatedAsset() != null){
            boolean isDeviceInPerimeter = checkIfDeviceInAssetPerimeter(device.getRelatedAsset().getPerimeter(), latitude, longitude);
            if (!isDeviceInPerimeter && device.getIsInAssetPerimeter())
                bot.sendLeavingPerimeterMessage(device.getName());
            device.setIsInAssetPerimeter(isDeviceInPerimeter);
        }
        deviceRepo.save(device);
    }

    public boolean checkIfDeviceInAssetPerimeter (String stringPerimeter, double x, double y) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser(stringPerimeter);
        ObjectNode perimeter = mapper.readTree(parser);

        double xA = perimeter.get("xA").asDouble();
        double yA = perimeter.get("yA").asDouble();
        double xB = perimeter.get("xB").asDouble();
        double yB = perimeter.get("yB").asDouble();
        double xC = perimeter.get("xC").asDouble();
        double yC = perimeter.get("yC").asDouble();
        double xD = perimeter.get("xD").asDouble();
        double yD = perimeter.get("yD").asDouble();

        if (checkMaxMin(x, xA, xB, xC, xD) || checkMaxMin(y, yA, yB, yC, yD)) return false;

        double cross1 = (x - xA) * (yB - yA) - (y - yA) * (xB - xA);
        double cross2 = (x - xB) * (yC - yB) - (y - yB) * (xC - xB);
        double cross3 = (x - xC) * (yD - yC) - (y - yC) * (xD - xC);
        double cross4 = (x - xD) * (yA - yD) - (y - yD) * (xA - xD);

        return cross1 >= 0 && cross2 >= 0 && cross3 >= 0 && cross4 >= 0;
    }
    private boolean checkMaxMin(double point, double rect1, double rect2, double rect3, double rect4) {
        double min = Math.min(Math.min(rect1, rect2), Math.min(rect3, rect4));
        if (point < min) {
            return true;
        }
        double max = Math.max(Math.max(rect1, rect2), Math.max(rect3, rect4));
        return point > max;
    }
}
