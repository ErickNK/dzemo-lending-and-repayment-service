package com.flycode.lendingandrepaymentservice.services;

import com.flycode.lendingandrepaymentservice.dtos.LoanRequest;
import com.flycode.lendingandrepaymentservice.models.Loan;
import com.flycode.lendingandrepaymentservice.models.LoanDefaulter;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.repositories.LoanDefaulterRepository;
import com.flycode.lendingandrepaymentservice.repositories.LoanRepository;
import com.flycode.lendingandrepaymentservice.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-integration-tests.properties")
class HandleLoanRequestServiceTest {

    @InjectMocks
    HandleLoanRequestService inTesting;

    @Mock
    LoanRepository loanRepositoryMock;

    @Mock
    LoanDefaulterRepository loanDefaulterRepositoryMock;

    @Mock
    UserRepository userRepositoryMock;

    @Autowired
    Environment environment;

    private final LocalDate LOCAL_DATE = LocalDate.of(2022, 9, 10);
    @Mock
    private Clock clock;
    private final Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @BeforeEach
    void initMocks() {
        ReflectionTestUtils.setField(inTesting, "environment", environment);
    }

    private void mockClock() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    @DisplayName("Assigns new loan within limit when none existed before")
    @Test
    void assigns_new_loan_within_user_limit_if_none_existed_before() throws ExecutionException, InterruptedException {
        // Arrange
        mockClock();
        LoanRequest loanRequest = new LoanRequest(1000L, "2022-09-11");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 2000L)
        );
        when(loanDefaulterRepositoryMock.findByUserId(anyLong())).thenReturn(null);
        when(loanRepositoryMock.findByUserMsisdn(anyString())).thenReturn(null);

        // Act
        var response = inTesting.execute(loanRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(loanRepositoryMock).save(any(Loan.class));
    }

    @DisplayName("Does not assign loan when beyond customer limit when no loan existed before")
    @Test
    void denies_new_loan_within_user_limit_if_none_existed_before() throws ExecutionException, InterruptedException {
        // Arrange
        mockClock();
        LoanRequest loanRequest = new LoanRequest(2000L, "2022-09-11");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 1000L)
        );
        when(loanDefaulterRepositoryMock.findByUserId(anyLong())).thenReturn(null);
        when(loanRepositoryMock.findByUserMsisdn(anyString())).thenReturn(null);

        // Act
        var response = inTesting.execute(loanRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("Loan amount beyond user loan limit");
    }

    @DisplayName("Does not assign loan if due date is invalid")
    @Test
    void invalidDueDateTest() throws ExecutionException, InterruptedException {
        // Arrange
        mockClock();
        LoanRequest loanRequest = new LoanRequest(1000L, "2022-09-09");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 1000L)
        );
        when(loanDefaulterRepositoryMock.findByUserId(anyLong())).thenReturn(null);

        // Act
        var response = inTesting.execute(loanRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("Invalid due date");
    }

    @DisplayName("Does not assign loan if customer is already a defaulter")
    @Test
    void defaulterCheckTest() throws ExecutionException, InterruptedException {
        // Arrange
        LoanRequest loanRequest = new LoanRequest(1000L, "2022-09-11");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 1000L)
        );
        when(loanDefaulterRepositoryMock.findByUserId(anyLong())).thenReturn(
                new LoanDefaulter(1L, 1000L)
        );

        // Act
        var response = inTesting.execute(loanRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("Previous loans defaulted. User has to repay previous loans.");
    }

    @DisplayName("Does not assign loan if active loan is past due date")
    @Test
    void activeLoanPastDueDateTest() throws ExecutionException, InterruptedException {
        // Arrange
        mockClock();
        LoanRequest loanRequest = new LoanRequest(1000L, "2022-09-11");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 1000L)
        );
        when(loanDefaulterRepositoryMock.findByUserId(anyLong())).thenReturn(null);
        when(loanRepositoryMock.findByUserMsisdn(anyString())).thenReturn(
                new Loan(1L, 1000L, LocalDate.of(2022, 11, 11))
        );

        // Act
        var response = inTesting.execute(loanRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("Active loan is defaulted. User marked as defaulter");
    }

    @DisplayName("Does not assign loan if balance from active loan is not enough for loan requested")
    @Test
    void activeLoanBalanceNotEnough() throws ExecutionException, InterruptedException {
        // Arrange
        mockClock();
        LoanRequest loanRequest = new LoanRequest(1000L, "2022-09-13");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 1000L)
        );
        when(loanDefaulterRepositoryMock.findByUserId(anyLong())).thenReturn(null);
        when(loanRepositoryMock.findByUserMsisdn(anyString())).thenReturn(
                new Loan(1L, 1000L, LocalDate.of(2022, 9, 11))
        );

        // Act
        var response = inTesting.execute(loanRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getErrorMessage()).contains("Loan amount beyond user loan limit");
    }

    @DisplayName("Assigns loan by adding to active loan and updating due date")
    @Test
    void addLoanToActiveLoanTest() throws ExecutionException, InterruptedException {
        // Arrange
        mockClock();
        LoanRequest loanRequest = new LoanRequest(1000L, "2022-09-13");
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        when(userRepositoryMock.findByUsername(anyString())).thenReturn(
                new User(1L, "test-number", "john", "john", null, 2000L)
        );
        when(loanDefaulterRepositoryMock.findByUserId(anyLong())).thenReturn(null);
        when(loanRepositoryMock.findByUserMsisdn(anyString())).thenReturn(
                new Loan(1L, 1000L, LocalDate.of(2022, 9, 11))
        );

        // Act
        var response = inTesting.execute(loanRequest, principal).get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(loanRepositoryMock).save(any(Loan.class));
    }
}