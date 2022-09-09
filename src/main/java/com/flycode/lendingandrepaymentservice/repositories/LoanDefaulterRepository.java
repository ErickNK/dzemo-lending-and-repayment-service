package com.flycode.lendingandrepaymentservice.repositories;

import com.flycode.lendingandrepaymentservice.models.LoanDefaulter;
import com.flycode.lendingandrepaymentservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanDefaulterRepository extends JpaRepository<LoanDefaulter, Long> {
}
