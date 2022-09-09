package com.flycode.lendingandrepaymentservice.repositories;

import com.flycode.lendingandrepaymentservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
     User findByUsername(String username);

     @Query("SELECT u FROM User u " +
             "JOIN FETCH u.loan " +
             "WHERE u.username = :username")
     User findByUsernameWithLoan(String username);
}
