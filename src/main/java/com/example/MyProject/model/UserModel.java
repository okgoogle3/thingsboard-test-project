package com.example.MyProject.model;

import com.example.MyProject.model.CompositeKey.TelemetryId;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;

    //@ManyToMany(fetch = FetchType.EAGER)
    //private Set<RoleModel> roles;

    @JsonManagedReference(value = "user-asset-relation")
    @OneToMany(mappedBy = "relatedUser", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<AssetModel> asset = new ArrayList<>();
}