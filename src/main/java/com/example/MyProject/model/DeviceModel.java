package com.example.MyProject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "device")
public class DeviceModel {
    @Id
    private String id;
    @Column(name="name", unique=true)
    private String name;
    private Boolean isActive;
    private Boolean isInAssetPerimeter;
    private Double latitude;
    private Double temperatureThreshold;
    private Double longitude;

    @Nullable
    @JsonBackReference(value = "asset-device-relation")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", referencedColumnName = "id")
    private AssetModel relatedAsset;

    @JsonManagedReference(value = "device-telemetry-relation")
    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    private Set<TelemetryModel> telemetry;

    public DeviceModel(String name, Double x, Double y){
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isActive = false;
        this.isInAssetPerimeter = false;
        this.latitude = x;
        this.longitude = y;
    }
}
