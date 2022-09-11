package com.flycode.lendingandrepaymentservice.controllers;

import com.flycode.lendingandrepaymentservice.dtos.AssignRole;
import com.flycode.lendingandrepaymentservice.dtos.Response;
import com.flycode.lendingandrepaymentservice.models.Role;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.services.RefreshJWTTokenService;
import com.flycode.lendingandrepaymentservice.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequestMapping("/api/auth")
@RestController
public class AuthController {

    @Autowired
    RefreshJWTTokenService refreshJWTTokenService;

    @Autowired
    UserService userService;

    @Operation(summary = "Refresh existing login token.",
            security = {@SecurityRequirement(name = "bearer-key")})
    @GetMapping("/token/refresh")
    public ResponseEntity<Response<Map<String, String>>> refreshToken(HttpServletRequest request) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok().body(refreshJWTTokenService.execute(authorizationHeader));
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

    @Operation(summary = "Get all users registered", security = {@SecurityRequirement(name = "bearer-key")})
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @Operation(summary = "Register a new user", security = {@SecurityRequirement(name = "bearer-key")})
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(user));
    }

    @Operation(summary = "Register a new role", security = {@SecurityRequirement(name = "bearer-key")})
    @PostMapping("/role")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveRole(role));
    }

    @Operation(summary = "Assign user role", security = {@SecurityRequirement(name = "bearer-key")})
    @PostMapping("/user/role")
    public ResponseEntity<?> assignRoleToUser(@RequestBody AssignRole assignRole) {
        userService.addRoleToUser(assignRole.getUsername(), assignRole.getRoleName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Populate data base with test data.")
    @GetMapping("/populate-records")
    public ResponseEntity<?> populateRecords() {
        if (userService.getUser("john") != null) {
            return ResponseEntity.ok().build();
        }

        userService.saveRole(new Role(null, "ROLE_USER"));
        userService.saveRole(new Role(null, "ROLE_ADMIN"));

        userService.saveUser(new User(null, "25412345678", "John Doe", "john", "1234", 5000L));
        userService.saveUser(new User(null, "25400000000", "Admin", "admin", "1234", 0L));

        userService.addRoleToUser("john", "ROLE_USER");
        userService.addRoleToUser("admin", "ROLE_ADMIN");

        return ResponseEntity.ok().build();
    }
}
