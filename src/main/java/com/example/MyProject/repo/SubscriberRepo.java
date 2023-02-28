package com.example.MyProject.repo;

import com.example.MyProject.model.SubscriberModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberRepo extends JpaRepository<SubscriberModel, Long> {
    Optional<SubscriberModel> findById(Long id);
    boolean existsById(Long id);
}
