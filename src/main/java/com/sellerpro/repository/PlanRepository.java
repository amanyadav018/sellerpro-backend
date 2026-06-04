package com.sellerpro.repository;

import com.sellerpro.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByName(String name);

    default Optional<Plan> findByName(Plan.PlanName planName) {
        return findByName(planName.name());
    }

    List<Plan> findByIsActiveTrue();

    // Alias for controllers that call findByActiveTrue
    default List<Plan> findByActiveTrue() {
        return findByIsActiveTrue();
    }

    boolean existsByName(String name);
}
