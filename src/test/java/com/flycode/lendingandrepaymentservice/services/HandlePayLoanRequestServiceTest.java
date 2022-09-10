package com.flycode.lendingandrepaymentservice.services;

import com.flycode.lendingandrepaymentservice.dtos.RepaymentRequest;
import com.flycode.lendingandrepaymentservice.models.Loan;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.repositories.LoanRepository;
import com.flycode.lendingandrepaymentservice.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HandlePayLoanRequestServiceTest {

    @InjectMocks
    HandlePayLoanRequestService inTesting;

    @Mock
    LoanRepository loanRepositoryMock;

    @Mock
    UserRepository userRepositoryMock;

    private final LocalDate LOCAL_DATE = LocalDate.of(2022, 9, 10);
    @Mock
    private Clock clock;
    private final Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    private void mockClock() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    @DisplayName("Deletes the active loan when repayment is in full.")
    @Test
    void deletesActiveLoanOnFullPayment() throws ExecutionException, InterruptedException {
        // Arrange
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");

        RepaymentRequest repaymentRequest = new RepaymentRequest(1000L);
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 2000L)
        );
        when(loanRepositoryMock.findByUserMsisdn(anyString())).thenReturn(
                new Loan(1L, 1000L, LocalDate.of(2022, 9, 11))
        );

        // Act
        var response = inTesting.execute(repaymentRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(loanRepositoryMock).delete(any(Loan.class));
    }

    @DisplayName("Updates the active loan when repayment is partial.")
    @Test
    void updatesActiveLoanOnPartialPayment() throws ExecutionException, InterruptedException {
        // Arrange
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");

        RepaymentRequest repaymentRequest = new RepaymentRequest(500L);
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 2000L)
        );
        var activeLoan = new Loan(1L, 1000L, LocalDate.of(2022, 9, 11));
        when(loanRepositoryMock.findByUserMsisdn(anyString())).thenReturn(activeLoan);

        // Act
        var response = inTesting.execute(repaymentRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(activeLoan.getDebt()).isEqualTo(500L);
        verify(loanRepositoryMock).save(any(Loan.class));
        verify(loanRepositoryMock, times(0)).delete(any(Loan.class));
    }

    @DisplayName("Responds with error on no active loan.")
    @Test
    void errorOnNoActiveLoan() throws ExecutionException, InterruptedException {
        // Arrange
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");

        RepaymentRequest repaymentRequest = new RepaymentRequest(500L);
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 2000L)
        );
        when(loanRepositoryMock.findByUserMsisdn(anyString())).thenReturn(null);

        // Act
        var response = inTesting.execute(repaymentRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("No active loan to repay");
    }

}