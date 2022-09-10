package com.flycode.lendingandrepaymentservice.controllers;

import com.flycode.lendingandrepaymentservice.dtos.AssignRole;
import com.flycode.lendingandrepaymentservice.dtos.Response;
import com.flycode.lendingandrepaymentservice.models.Role;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.services.RefreshJWTTokenService;
import com.flycode.lendingandrepaymentservice.services.UserService;
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

    @GetMapping("/token/refresh")
    public ResponseEntity<Response<Map<String, String>>> refreshToken(HttpServletRequest request) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok().body(refreshJWTTokenService.execute(authorizationHeader));
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(user));
    }

    @PostMapping("/role")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveRole(role));
    }

    @PostMapping("/user/role")
    public ResponseEntity<?> assignRoleToUser(@RequestBody AssignRole assignRole) {
        userService.addRoleToUser(assignRole.getUsername(), assignRole.getRoleName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/populate-records")
    public ResponseEntity<?> populateRecords() {
        userService.saveRole(new Role(null, "ROLE_USER"));
        userService.saveRole(new Role(null, "ROLE_ADMIN"));

        userService.saveUser(new User(null, "25412345678", "John Doe", "john", "1234", 5000L));
        userService.saveUser(new User(null, "25400000000", "Admin", "admin", "1234", 0L));

        userService.addRoleToUser("john", "ROLE_USER");
        userService.addRoleToUser("admin", "ROLE_ADMIN");

        return ResponseEntity.ok().build();
    }
}
