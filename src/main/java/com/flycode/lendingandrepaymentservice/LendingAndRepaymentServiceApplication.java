package com.flycode.lendingandrepaymentservice;

import com.flycode.lendingandrepaymentservice.models.Role;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;

@SpringBootApplication
public class LendingAndRepaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LendingAndRepaymentServiceApplication.class, args);
	}

}
