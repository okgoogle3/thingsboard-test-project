package com.example.MyProject.repo;

import com.example.MyProject.model.UserModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserModel, Long> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Optional<UserModel> findByUsername(String username);
    void deleteById(long id);
}
