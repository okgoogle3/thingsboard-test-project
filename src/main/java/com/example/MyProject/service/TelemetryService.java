package com.example.MyProject.service;

import com.example.MyProject.bot.TgBot;
import com.example.MyProject.controller.DTO.AggregationFunction;
import com.example.MyProject.model.AssetModel;
import com.example.MyProject.model.DeviceModel;
import com.example.MyProject.model.TelemetryModel;
import com.example.MyProject.repo.AssetRepo;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@Service
public class TelemetryService {
    public final TelemetryRepo telemetryRepo;
    private final DeviceRepo deviceRepo;
    private final AssetRepo assetRepo;
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
            if(tm.getType().equals("temperature") && tm.getDevice().getTemperatureThreshold() != null && tm.getDevice().getTemperatureThreshold()<tm.getValue())
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
        return aggregationValue(startTs, endTs, telemetry, aggregationPeriod, aggregationFunction);
    }

    public void getAggregatedTelemetryByAssetInXls(
            String assetName,
            String telemetryType,
            Long startTs,
            Long endTs,
            Long aggregationPeriod,
            AggregationFunction aggregationFunction) throws IOException {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        String fileLocation = path.substring(0, path.length() - 1) + assetName + ".xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(assetName);
        Row header = sheet.createRow(0);
        Cell headerCell = header.createCell(0);
        headerCell.setCellValue("Device");
        headerCell = header.createCell(1);
        headerCell.setCellValue("Time");
        headerCell = header.createCell(2);
        headerCell.setCellValue("Value");
        AssetModel assetModel = assetRepo.findByName(assetName).orElseThrow(()-> new EntityNotFoundException("Asset not found"));
        List<DeviceModel> devices = assetModel.getDevice();
        int rowCounter = 1;
        for(DeviceModel device : devices){
            List<TelemetryModel> telemetry = telemetryRepo.findAllByDeviceAndTimestampAndType(device, startTs, endTs, telemetryType);
            aggregationValue(startTs, endTs, telemetry, aggregationPeriod, sheet, rowCounter,  device, aggregationFunction);
        }
        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();
    }

    private void aggregationValue (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, Sheet sheet, int rowCounter, DeviceModel dev, AggregationFunction aggregationFunction){
        while (startTs<endTs){
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if (currentTelemetry.size()==0) {
                startTs += period;
                continue;
            }
            Double val = calculatedValue(currentTelemetry, aggregationFunction);
            Row row = sheet.createRow(rowCounter);
            setCellValue(row, dev.getName(), startTs, val);
            rowCounter++;
            startTs += period;
        }
    }

    private List<ObjectNode> aggregationValue (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, AggregationFunction aggregationFunction){
        List<ObjectNode> aggregatedTelemetry = new ArrayList<>();
        while (startTs<endTs){
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if (currentTelemetry.size()==0) {
                startTs += period;
                continue;
            }
            Double val = calculatedValue(currentTelemetry, aggregationFunction);
            ObjectNode node = mapper.createObjectNode();
            node.put(startTs.toString(), val);
            aggregatedTelemetry.add(mapper.valueToTree(node));
            startTs += period;
        }
        return aggregatedTelemetry;
    }
    private void setCellValue(Row row, String deviceName, Long startTime, Double value){
        Cell cell = row.createCell(0);
        cell.setCellValue(deviceName);
        cell = row.createCell(1);
        cell.setCellValue((new Date(startTime)).toString());
        cell = row.createCell(2);
        cell.setCellValue(value);
    }

    private Double calculatedValue(List<TelemetryModel> telemetry, AggregationFunction func){
        Double val=0.0;
        switch (func) {
            case MIN -> {
                val = telemetry.get(0).getValue();
                for(TelemetryModel tm : telemetry) val = (val <= tm.getValue()) ? val : tm.getValue();
                return val;
            }
            case MAX -> {
                val = telemetry.get(0).getValue();
                for(TelemetryModel tm : telemetry) val = (val >= tm.getValue()) ? val : tm.getValue();
                return val;
            }
            case SUM -> {
                for(TelemetryModel tm : telemetry) val += tm.getValue();
                return val;
            }
            case AVG -> {
                for(TelemetryModel tm : telemetry) val += tm.getValue();
                return val/telemetry.size();
            }
        }
        return val;
    }
}
