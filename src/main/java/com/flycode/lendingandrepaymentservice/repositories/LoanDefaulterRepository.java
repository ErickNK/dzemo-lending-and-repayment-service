package com.flycode.lendingandrepaymentservice.repositories;

import com.flycode.lendingandrepaymentservice.models.LoanDefaulter;
import com.flycode.lendingandrepaymentservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanDefaulterRepository extends JpaRepository<LoanDefaulter, Long> {
    @Query(value = "SELECT * FROM loan_defaulters " +
            "WHERE user_id = :userId", nativeQuery = true)
    LoanDefaulter findByUserId(@Param("userId") Long userId);
}
