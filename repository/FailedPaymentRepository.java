package com.sellerpro.repository;

import com.sellerpro.model.FailedPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedPaymentRepository extends JpaRepository<FailedPayment, Long> {

    Page<FailedPayment> findAllByOrderByAttemptedAtDesc(Pageable pageable);

    List<FailedPayment> findByStatus(String status);

    long countByStatus(String status);
}
