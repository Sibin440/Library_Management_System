package com.example.opac.repository;

import com.example.opac.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserIdAndIsReadFalse(Long userId);
}
