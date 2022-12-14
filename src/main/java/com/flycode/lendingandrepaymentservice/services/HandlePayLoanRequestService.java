package com.flycode.lendingandrepaymentservice.services;

import com.flycode.lendingandrepaymentservice.dtos.RepaymentRequest;
import com.flycode.lendingandrepaymentservice.dtos.Response;
import com.flycode.lendingandrepaymentservice.models.Loan;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.repositories.LoanRepository;
import com.flycode.lendingandrepaymentservice.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HandlePayLoanRequestService {
    @Autowired
    LoanRepository loanRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * Handle a /pay-loan api call. The function check multiple creteria updating clearing old loan if fully paid
     * or updating new debt of old loan if partially paid. A sms is then sent to customer upon action of loan update.
     *
     * @param repaymentRequest request data of api call.
     * @param principal logged in user.
     * @return Response with true value if loan updated or deleted successfully. Otherwise, Response with error message.
     */
    @Async
    public CompletableFuture<Response<Boolean>> execute(RepaymentRequest repaymentRequest, Principal principal) {
        try {
            User user = userRepository.findByUsername(principal.getName());
            Loan activeLoan = loanRepository.findByUserMsisdn(user.getMsisdn());
            if(activeLoan == null) {
                return CompletableFuture.completedFuture(Response.withBadRequestError("No active loan to repay"));
            }

            var paymentDiff = activeLoan.getDebt() - repaymentRequest.getAmount();
            if(paymentDiff < 1) { // fully paid
                loanRepository.delete(activeLoan);
            } else {
                activeLoan.setDebt(activeLoan.getDebt() - repaymentRequest.getAmount());
                loanRepository.save(activeLoan);
            }

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
}
