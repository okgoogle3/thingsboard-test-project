package com.example.MyProject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "asset")
public class AssetModel {
    @Id
    private String id;
    @Column(unique=true)
    private String name;
    private Boolean isActive;
    private String perimeter;

    @Nullable
    @JsonBackReference(value = "user-asset-relation")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserModel relatedUser;
    @JsonManagedReference(value = "asset-device-relation")
    @OneToMany(mappedBy = "relatedAsset", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<DeviceModel> device = new ArrayList<>();

    public AssetModel(String name, String perimeter) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.perimeter = perimeter;
        this.isActive = false;
    }

    public AssetModel(String name, Boolean isActive, List<DeviceModel> device) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isActive = isActive;
        this.device = device;
    }
}
