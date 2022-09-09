package com.flycode.lendingandrepaymentservice.services;

import com.flycode.lendingandrepaymentservice.dtos.LoanRequest;
import com.flycode.lendingandrepaymentservice.dtos.Response;
import com.flycode.lendingandrepaymentservice.models.Loan;
import com.flycode.lendingandrepaymentservice.models.LoanDefaulter;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.repositories.LoanDefaulterRepository;
import com.flycode.lendingandrepaymentservice.repositories.LoanRepository;
import com.flycode.lendingandrepaymentservice.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HandleLoanRequestService {

    @Autowired
    UserService userService;

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    LoanDefaulterRepository loanDefaulterRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    Environment environment;

    @Autowired
    Clock clock;

    @Async
    public CompletableFuture<Response<Boolean>> execute(LoanRequest loanRequest, Principal principal) {

        try {
            User user = userRepository.findByUsernameWithLoan(principal.getName());

            var loanRequestDueDate = OffsetDateTime.parse(loanRequest.getDueDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            var dayDiff = ChronoUnit.DAYS.between(loanRequestDueDate, OffsetDateTime.now(clock));
            if(dayDiff < 1) {
                return CompletableFuture.completedFuture(Response.withBadRequestError("Invalid due date"));
            }

            Loan activeLoan = user.getLoan();
            if(activeLoan != null) {
                return onActiveLoan(loanRequest, user, loanRequestDueDate);
            }

            Loan loan = new Loan();
            loan.setUser(user);
            loan.setDueDate(OffsetDateTime.parse(loanRequest.getDueDate(), DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            loan.setDebt(loanRequest.getAmount());
            loanRepository.save(loan);

            return CompletableFuture.completedFuture(Response.successResponse(Boolean.TRUE));
        } catch (Exception exception) {
            Response<Boolean> response = new Response<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    null,
                    exception.getMessage()
            );
            return CompletableFuture.completedFuture(response);
        }

    }

    private CompletableFuture<Response<Boolean>> onActiveLoan(LoanRequest loanRequest, User user, OffsetDateTime loanRequestDueDate) {
        var activeLoan = user.getLoan();

        // check for active loans passed due date
        var monthDiff = ChronoUnit.MONTHS.between(loanRequestDueDate, OffsetDateTime.now(clock));
        var defaultingTimeInMonths = environment.getRequiredProperty("validation.loans.defaulting-time-in-months", Integer.class);
        if (monthDiff >= defaultingTimeInMonths) { // defaulted
            LoanDefaulter loanDefaulter = new LoanDefaulter();
            loanDefaulter.setUser(user);
            loanDefaulter.setDebt(activeLoan.getDebt());
            loanDefaulterRepository.save(loanDefaulter);

            loanRepository.delete(activeLoan);

            return CompletableFuture.completedFuture(Response.withBadRequestError("Active loan is defaulted. User marked as defaulter"));
        }

        // check beyond loan limit
        var balance = user.getLoanLimit() - activeLoan.getDebt();
        if((balance - loanRequest.getAmount()) < 1) {
            return CompletableFuture.completedFuture(Response.withBadRequestError("Loan amount beyond user loan limit"));
        }

        activeLoan.setDebt(activeLoan.getDebt() + loanRequest.getAmount());
        activeLoan.setDueDate(loanRequestDueDate);
        loanRepository.save(activeLoan);

        return CompletableFuture.completedFuture(Response.successResponse(Boolean.TRUE));
    }

}
