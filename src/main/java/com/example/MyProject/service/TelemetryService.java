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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
        List<ObjectNode> aggregatedTelemetry = new ArrayList<>();
        return switch (aggregationFunction) {
            case MIN -> minValue(startTs, endTs, telemetry, aggregationPeriod, aggregatedTelemetry);
            case MAX -> maxValue(startTs, endTs, telemetry, aggregationPeriod, aggregatedTelemetry);
            case SUM -> sumValue(startTs, endTs, telemetry, aggregationPeriod, aggregatedTelemetry);
            case AVG -> avgValue(startTs, endTs, telemetry, aggregationPeriod, aggregatedTelemetry);
        };
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
            switch (aggregationFunction){
                case MIN -> minValueInXls(startTs, endTs, telemetry, aggregationPeriod, sheet, rowCounter, device);
                case MAX -> maxValueInXls(startTs, endTs, telemetry, aggregationPeriod, sheet, rowCounter, device);
                case SUM -> sumValueInXls(startTs, endTs, telemetry, aggregationPeriod, sheet, rowCounter, device);
                case AVG -> avgValueInXls(startTs, endTs, telemetry, aggregationPeriod, sheet, rowCounter, device);
            }
        }
        FileOutputStream outputStream = new FileOutputStream(fileLocation);
        workbook.write(outputStream);
        outputStream.close();
        workbook.close();
    }

    private List<ObjectNode> minValue (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, List<ObjectNode> aggregatedTelemetry){
        while (startTs<endTs){
            Double minVal = null;
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if(new HashSet<>(telemetry).containsAll(currentTelemetry))
            {
                if (currentTelemetry.size()==0) {
                    startTs += period;
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
            startTs += period;
        }
        return aggregatedTelemetry;
    }

    private List<ObjectNode> maxValue (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, List<ObjectNode> aggregatedTelemetry){
        while (startTs<endTs){
            Double maxVal = null;
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if(new HashSet<>(telemetry).containsAll(currentTelemetry))
            {
                if (currentTelemetry.size()==0) {
                    startTs += period;
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
            startTs += period;
        }
        return aggregatedTelemetry;
    }
    private List<ObjectNode> sumValue (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, List<ObjectNode> aggregatedTelemetry){
        while (startTs<endTs){
            Double sum = 0.0;
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if(new HashSet<>(telemetry).containsAll(currentTelemetry))
            {
                if (currentTelemetry.size()==0) {
                    startTs += period;
                    continue;
                }
                else {
                    for(TelemetryModel tm : currentTelemetry) sum += tm.getValue();
                }
            }
            ObjectNode node = mapper.createObjectNode();
            node.put(startTs.toString(), sum);
            aggregatedTelemetry.add(mapper.valueToTree(node));
            startTs += period;
        }
        return aggregatedTelemetry;
    }
    private List<ObjectNode> avgValue (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, List<ObjectNode> aggregatedTelemetry){
        while (startTs<endTs){
            Double sum = 0.0;
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if(new HashSet<>(telemetry).containsAll(currentTelemetry))
            {
                if (currentTelemetry.size()==0) {
                    startTs += period;
                    continue;
                }
                else {
                    for(TelemetryModel tm : currentTelemetry) sum += tm.getValue();
                }
            }
            ObjectNode node = mapper.createObjectNode();
            node.put(startTs.toString(), sum/currentTelemetry.size());
            aggregatedTelemetry.add(mapper.valueToTree(node));
            startTs += period;
        }
        return aggregatedTelemetry;
    }

    private void minValueInXls (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, Sheet sheet, int rowCounter, DeviceModel dev){
        while (startTs<endTs){
            Double minVal = null;
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if(new HashSet<>(telemetry).containsAll(currentTelemetry))
            {
                if (currentTelemetry.size()==0) {
                    startTs += period;
                    continue;
                }
                else {
                    minVal = currentTelemetry.get(0).getValue();
                    for(TelemetryModel tm : currentTelemetry) minVal = (minVal<=tm.getValue()) ? minVal : tm.getValue();
                }
            }
            Row row = sheet.createRow(rowCounter);
            Cell cell = row.createCell(0);
            cell.setCellValue(dev.getName());
            cell = row.createCell(1);
            cell.setCellValue(startTs);
            cell = row.createCell(2);
            cell.setCellValue(minVal);
            rowCounter++;
            startTs += period;
        }
    }

    private void maxValueInXls (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, Sheet sheet, int rowCounter, DeviceModel dev){
        while (startTs<endTs){
            Double maxVal = null;
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if(new HashSet<>(telemetry).containsAll(currentTelemetry))
            {
                if (currentTelemetry.size()==0) {
                    startTs += period;
                    continue;
                }
                else {
                    maxVal = currentTelemetry.get(0).getValue();
                    for(TelemetryModel tm : currentTelemetry) maxVal = (maxVal>=tm.getValue()) ? maxVal : tm.getValue();
                }
            }
            Row row = sheet.createRow(rowCounter);
            Cell cell = row.createCell(0);
            cell.setCellValue(dev.getName());
            cell = row.createCell(1);
            cell.setCellValue(startTs);
            cell = row.createCell(2);
            cell.setCellValue(maxVal);
            rowCounter++;
            startTs += period;
        }
    }
    private void sumValueInXls (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, Sheet sheet, int rowCounter, DeviceModel dev){
        while (startTs<endTs){
            Double sum = 0.0;
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if(new HashSet<>(telemetry).containsAll(currentTelemetry))
            {
                if (currentTelemetry.size()==0) {
                    startTs += period;
                    continue;
                }
                else {
                    for(TelemetryModel tm : currentTelemetry) sum += tm.getValue();
                }
            }
            Row row = sheet.createRow(rowCounter);
            Cell cell = row.createCell(0);
            cell.setCellValue(dev.getName());
            cell = row.createCell(1);
            cell.setCellValue(startTs);
            cell = row.createCell(2);
            cell.setCellValue(sum);
            rowCounter++;
            startTs += period;
        }
    }
    private void avgValueInXls (Long startTs, Long endTs, List<TelemetryModel> telemetry, Long period, Sheet sheet, int rowCounter, DeviceModel dev){
        while (startTs<endTs){
            Double sum = 0.0;
            Long finalStartTs = startTs;
            List<TelemetryModel> currentTelemetry = telemetry.stream()
                    .filter(tm -> tm.getTimestamp() < endTs
                            && tm.getTimestamp() >= finalStartTs
                            && tm.getTimestamp() < finalStartTs + period
                    ).toList();
            if(new HashSet<>(telemetry).containsAll(currentTelemetry))
            {
                if (currentTelemetry.size()==0) {
                    startTs += period;
                    continue;
                }
                else {
                    for(TelemetryModel tm : currentTelemetry) sum += tm.getValue();
                }
            }
            Row row = sheet.createRow(rowCounter);
            Cell cell = row.createCell(0);
            cell.setCellValue(dev.getName());
            cell = row.createCell(1);
            cell.setCellValue(startTs);
            cell = row.createCell(2);
            cell.setCellValue(sum/currentTelemetry.size());
            rowCounter++;
            startTs += period;
        }
    }
}
