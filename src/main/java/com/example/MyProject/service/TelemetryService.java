package com.example.MyProject.service;

import com.example.MyProject.controller.DTO.AggregationFunction;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.model.TelemetryModel;
import com.example.MyProject.repo.DeviceRepo;
import com.example.MyProject.repo.TelemetryRepo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TelemetryService {
    public final TelemetryRepo telemetryRepo;
    private final DeviceRepo deviceRepo;
    private final ObjectMapper mapper = new ObjectMapper();
    public void createTelemetry(String hex) throws DecoderException, IOException {
        byte[] bytes = Hex.decodeHex(hex.substring(0, 20).toCharArray());
        String deviceName = new String(bytes, StandardCharsets.UTF_8);
        DeviceModel device = deviceRepo.findByName(deviceName)
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));
        bytes = Hex.decodeHex(hex.substring(20).toCharArray());
        ObjectReader reader = mapper.reader();
        JsonNode node =  reader.readTree(new ByteArrayInputStream(bytes));
        List<String> keys = new ArrayList<>();
        Iterator<String> iterator = node.fieldNames();
        iterator.forEachRemaining(keys::add);
        List<TelemetryModel> telemetry = new ArrayList<>();
        keys.forEach(key -> telemetry.add(new TelemetryModel(device, key, node.get(key).asDouble())));
        telemetryRepo.saveAll(telemetry);
    }

    public List<TelemetryModel> getTelemetryByDevice(
            String deviceName,
            String telemetryType,
            Long startTs,
            Long endTs,
            Long aggregationPeriod,
            AggregationFunction aggregationFunction)
    {
        DeviceModel device = deviceRepo.findByName(deviceName).orElseThrow(()->new EntityNotFoundException("Device not found"));
        List<TelemetryModel> telemetry = telemetryRepo.findAllByDeviceAndTimestampAndType(device, startTs, endTs, telemetryType);
        switch (aggregationFunction){
            case MIN:
            {

            }
            case MAX:
            {

            }
            case SUM:
            {

            }
            case AVG:
            {

            }
        }

        return telemetry;
    }
}
