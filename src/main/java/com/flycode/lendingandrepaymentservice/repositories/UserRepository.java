package com.flycode.lendingandrepaymentservice.repositories;

import com.flycode.lendingandrepaymentservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
     User findByUsername(String username);
}
