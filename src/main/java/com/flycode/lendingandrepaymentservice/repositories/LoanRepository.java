package com.flycode.lendingandrepaymentservice.repositories;

import com.flycode.lendingandrepaymentservice.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query(value = "SELECT * FROM loans " +
            "WHERE user_msisdn = ?1", nativeQuery = true)
    Loan findByUserMsisdn(String userMsisdn);
}
