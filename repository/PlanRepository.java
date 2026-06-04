package com.sellerpro.repository;

import com.sellerpro.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByName(String name);

    List<Plan> findByActiveTrue();

    @Query("SELECT p FROM Plan p WHERE p.active = true ORDER BY p.price ASC")
    List<Plan> findAllActiveSorted();
}
