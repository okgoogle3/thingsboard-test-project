package com.example.MyProject.repo;

import com.example.MyProject.model.ERole;
import com.example.MyProject.model.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepo extends JpaRepository<RoleModel, Long> {
    Optional<RoleModel> findByRole(ERole role);
}