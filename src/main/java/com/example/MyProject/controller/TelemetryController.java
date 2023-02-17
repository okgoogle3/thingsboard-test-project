package com.example.MyProject.controller;

import com.example.MyProject.controller.DTO.Request.TelemetryAggregationDTO;
import com.example.MyProject.controller.DTO.Request.TelemetryHexDTO;
import com.example.MyProject.model.TelemetryModel;
import com.example.MyProject.service.TelemetryService;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@RestController
@RequestMapping("/telemetry")
public class TelemetryController {
    public final TelemetryService telemetryService;

    @GetMapping("/device")
    public ResponseEntity<List<ObjectNode>> getAggregatedTelemetryByDevice(@RequestBody TelemetryAggregationDTO tm){
        List<ObjectNode> telemetry = telemetryService
                .getTelemetryByDevice(tm.getDeviceName(), tm.getType(), tm.getStartTs(),
                        tm.getEndTs(), tm.getAggregationPeriod(), tm.getAggregationFunction());
        return ResponseEntity.ok(telemetry);
    }

    @GetMapping("/asset")
    public ResponseEntity<List<TelemetryModel>> getAggregatedTelemetryByAsset(@RequestBody TelemetryAggregationDTO tm){
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> createTelemetry(@RequestBody TelemetryHexDTO telemetry) {
        try {
            //String location = String.format("/telemetry/%s", telemetryService.createTelemetry(String.valueOf(telemetry)));
            //return ResponseEntity.created(URI.create(location)).build();
            telemetryService.createTelemetry(telemetry.getHex());
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
