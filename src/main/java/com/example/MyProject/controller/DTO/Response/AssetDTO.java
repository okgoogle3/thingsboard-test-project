package com.example.MyProject.controller.DTO.Response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

import java.util.UUID;

@Data
public class AssetDTO {
    private String id;
    private String name;
    private Boolean isActive;
    private ObjectNode perimeter;
}
