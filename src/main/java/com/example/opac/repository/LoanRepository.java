package com.example.opac.repository;

import com.example.opac.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(Long userId);
    List<Loan> findByStatusAndReminderSentFalseAndDueDateBetween(
            String status, LocalDateTime start, LocalDateTime end);
}
