package com.flycode.lendingandrepaymentservice.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.flycode.lendingandrepaymentservice.dtos.Response;
import com.flycode.lendingandrepaymentservice.models.Role;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.springframework.http.HttpStatus.OK;

@Service
public class RefreshJWTTokenService {

    @Autowired
    Environment environment;

    @Autowired
    UserRepository userRepository;

    public Response<Map<String, String>> execute(String authorizationHeader) {
        String refresh_token = authorizationHeader.substring("Bearer ".length());

        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT decodedJWT = verifier.verify(refresh_token);

        String username = decodedJWT.getSubject();
        User user = userRepository.findByUsername(username);

        String access_token = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + environment.getRequiredProperty("security.jwt.access_token.expiry-time", Integer.class)))
                .withIssuer(environment.getRequiredProperty("security.jwt.issuer"))
                .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                .sign(algorithm);

        Response<Map<String, String>> responseData = new Response<>();
        responseData.setStatus(OK.value());

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        responseData.setData(tokens);

        return responseData;
    }
}
