package com.example.MyProject.service;

import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.repo.DeviceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class DeviceService {

    public final DeviceRepo deviceRepo;

    public List<DeviceModel> getAllDevices(){
        return deviceRepo.findAll();
    }

    public List<DeviceModel> getAllActiveDevices(){
        List<DeviceModel> devices = deviceRepo.findAll();
        return devices.stream()
                .filter(DeviceModel::getIsActive)
                .toList();
    }

    public List<DeviceModel> getAllActiveDevicesByAsset(String id){
        List<DeviceModel> devices = deviceRepo.findAll();
        return devices.stream()
                .filter(DeviceModel::getIsActive)
                .filter(deviceModel -> Objects.equals(deviceModel.getRelatedAsset().getId(), id))
                .toList();
    }

}
