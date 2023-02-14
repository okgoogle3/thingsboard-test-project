package com.example.MyProject.controller.DTO;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

@Data
public class AssetDTO {
    private String name;
    private ObjectNode perimeter;
}
