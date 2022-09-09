package com.flycode.lendingandrepaymentservice.controllers;

import com.flycode.lendingandrepaymentservice.dtos.LoanRequest;
import com.flycode.lendingandrepaymentservice.dtos.RepaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequestMapping("/api/loans")
@RestController
@Slf4j
public class MainController {

    @PostMapping("/request-loan")
    public void getLoanRequest(
            @RequestBody LoanRequest loanRequest,
            Principal principal
    ) {
        //
        log.debug("something");
    }

    @PostMapping("/pay-loan")
    public void getLoanRequest(
            @RequestBody RepaymentRequest repaymentRequest,
            Principal principal
    ) {
        //
    }

}
