package com.example.MyProject.service;

import com.example.MyProject.bot.TgBot;
import com.example.MyProject.controller.DTO.AggregationFunction;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.model.TelemetryModel;
import com.example.MyProject.repo.DeviceRepo;
import com.example.MyProject.repo.TelemetryRepo;
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
import java.util.*;

@RequiredArgsConstructor
@Service
public class TelemetryService {
    public final TelemetryRepo telemetryRepo;
    private final DeviceRepo deviceRepo;
    private final ObjectMapper mapper = new ObjectMapper();
    public final TgBot bot;

    public List<TelemetryModel> getTelemetryByDevice(String name){
        DeviceModel device = deviceRepo.findByName(name).orElseThrow(() -> new EntityNotFoundException("Device not found"));
        return device.getTelemetry();
    }

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
        for(TelemetryModel tm : telemetry) {
            if(tm.getType().equals("temperature")&& tm.getDevice().getTemperatureThreshold()<tm.getValue())
                bot.sendTemperatureMessage(tm.getDevice().getName(), tm.getValue());
        }
        telemetryRepo.saveAll(telemetry);
    }

    public List<ObjectNode> getAggregatedTelemetryByDevice(
            String deviceName,
            String telemetryType,
            Long startTs,
            Long endTs,
            Long aggregationPeriod,
            AggregationFunction aggregationFunction)
    {
        DeviceModel device = deviceRepo.findByName(deviceName).orElseThrow(()->new EntityNotFoundException("Device not found"));
        List<TelemetryModel> telemetry = telemetryRepo.findAllByDeviceAndTimestampAndType(device, startTs, endTs, telemetryType);
        List<ObjectNode> aggregatedTelemetry = new ArrayList<>();
        switch (aggregationFunction){
            case MIN:
            {
                while (startTs<endTs){
                    Double minVal = null;
                    Long finalStartTs = startTs;
                    List<TelemetryModel> currentTelemetry = telemetry.stream().filter
                            (tm -> tm.getTimestamp() < endTs && tm.getTimestamp() >= finalStartTs && tm.getTimestamp() < finalStartTs + aggregationPeriod).toList();
                    if(new HashSet<>(telemetry).containsAll(currentTelemetry))
                    {
                        if (currentTelemetry.size()==0) {
                            startTs += aggregationPeriod;
                            continue;
                        }
                        else {
                            minVal = currentTelemetry.get(0).getValue();
                            for(TelemetryModel tm : currentTelemetry) minVal = (minVal<=tm.getValue()) ? minVal : tm.getValue();
                        }
                    }
                    ObjectNode node = mapper.createObjectNode();
                    node.put(startTs.toString(), minVal);
                    aggregatedTelemetry.add(mapper.valueToTree(node));
                    startTs += aggregationPeriod;
                }
            }
            case MAX:
            {
                while (startTs<endTs){
                    Double maxVal = null;
                    Long finalStartTs = startTs;
                    List<TelemetryModel> currentTelemetry = telemetry.stream().filter(tm -> tm.getTimestamp() < endTs && tm.getTimestamp() >= finalStartTs && tm.getTimestamp() < finalStartTs + aggregationPeriod).toList();
                    if(new HashSet<>(telemetry).containsAll(currentTelemetry))
                    {
                        if (currentTelemetry.size()==0) {
                            startTs += aggregationPeriod;
                            continue;
                        }
                        else {
                            maxVal = currentTelemetry.get(0).getValue();
                            for(TelemetryModel tm : currentTelemetry) maxVal = (maxVal>=tm.getValue()) ? maxVal : tm.getValue();
                        }
                    }
                    ObjectNode node = mapper.createObjectNode();
                    node.put(startTs.toString(), maxVal);
                    aggregatedTelemetry.add(mapper.valueToTree(node));
                    startTs += aggregationPeriod;
                }
            }
            case SUM:
            {
                while (startTs<endTs){
                    Double sum = 0.0;
                    Long finalStartTs = startTs;
                    List<TelemetryModel> currentTelemetry = telemetry.stream().filter(tm -> tm.getTimestamp() < endTs && tm.getTimestamp() >= finalStartTs && tm.getTimestamp() < finalStartTs + aggregationPeriod).toList();
                    if(new HashSet<>(telemetry).containsAll(currentTelemetry))
                    {
                        if (currentTelemetry.size()==0) {
                            startTs += aggregationPeriod;
                            continue;
                        }
                        else {
                            for(TelemetryModel tm : currentTelemetry) sum += tm.getValue();
                        }
                    }
                    ObjectNode node = mapper.createObjectNode();
                    node.put(startTs.toString(), sum);
                    aggregatedTelemetry.add(mapper.valueToTree(node));
                    startTs += aggregationPeriod;
                }
            }
            case AVG:
            {
                while (startTs<endTs){
                    Double sum = 0.0;
                    Long finalStartTs = startTs;
                    List<TelemetryModel> currentTelemetry = telemetry.stream().filter(tm -> tm.getTimestamp() < endTs && tm.getTimestamp() >= finalStartTs && tm.getTimestamp() < finalStartTs + aggregationPeriod).toList();
                    if(new HashSet<>(telemetry).containsAll(currentTelemetry))
                    {
                        if (currentTelemetry.size()==0) {
                            startTs += aggregationPeriod;
                            continue;
                        }
                        else {
                            for(TelemetryModel tm : currentTelemetry) sum += tm.getValue();
                        }
                    }
                    ObjectNode node = mapper.createObjectNode();
                    node.put(startTs.toString(), sum/currentTelemetry.size());
                    aggregatedTelemetry.add(mapper.valueToTree(node));
                    startTs += aggregationPeriod;
                }
            }
        }
        return aggregatedTelemetry;
    }
}
