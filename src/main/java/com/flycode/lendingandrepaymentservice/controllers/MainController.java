package com.flycode.lendingandrepaymentservice.controllers;

import com.flycode.lendingandrepaymentservice.batch.DataDumpingJob;
import com.flycode.lendingandrepaymentservice.dtos.LoanRequest;
import com.flycode.lendingandrepaymentservice.dtos.RepaymentRequest;
import com.flycode.lendingandrepaymentservice.dtos.Response;
import com.flycode.lendingandrepaymentservice.services.HandleLoanRequestService;
import com.flycode.lendingandrepaymentservice.services.HandlePayLoanRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.concurrent.CompletableFuture;

@RequestMapping("/api/loans")
@RestController
@Slf4j
public class MainController {

    @Autowired
    HandleLoanRequestService handleLoanRequestService;

    @Autowired
    HandlePayLoanRequestService handlePayLoanRequestService;

    @Autowired
    DataDumpingJob dataDumpingJob;

    @PostMapping("/request-loan")
    public CompletableFuture<Response<Boolean>> getLoanRequest(
            @RequestBody LoanRequest loanRequest,
            Principal principal
    ) {
        return handleLoanRequestService.execute(loanRequest, principal);
    }

    @PostMapping("/pay-loan")
    public CompletableFuture<Response<Boolean>> getLoanRequest(
            @RequestBody RepaymentRequest repaymentRequest,
            Principal principal
    ) {
        return handlePayLoanRequestService.execute(repaymentRequest, principal);
    }

    @GetMapping("/data-dump")
    public CompletableFuture<Response<Boolean>> executeDataDump(
            Principal principal
    ) {
        return dataDumpingJob.execute(principal);
    }

}
