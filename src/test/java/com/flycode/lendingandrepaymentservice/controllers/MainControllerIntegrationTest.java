package com.flycode.lendingandrepaymentservice.controllers;

import com.flycode.lendingandrepaymentservice.dtos.LoanRequest;
import com.flycode.lendingandrepaymentservice.dtos.RepaymentRequest;
import com.flycode.lendingandrepaymentservice.models.Loan;
import com.flycode.lendingandrepaymentservice.models.Role;
import com.flycode.lendingandrepaymentservice.models.User;
import com.flycode.lendingandrepaymentservice.repositories.LoanRepository;
import com.flycode.lendingandrepaymentservice.services.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest()
@TestPropertySource(
        locations = "classpath:application-integration-tests.properties",
        properties = {
                "spring.profiles.active=integration-tests",
                "spring.main.allow-bean-definition-overriding=true"
        })
@ActiveProfiles(profiles = {"integration-tests"})
@AutoConfigureMockMvc
@ImportAutoConfiguration(exclude = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MainControllerIntegrationTest {
    String authToken;

    @Autowired
    MockMvc mvc;

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    UserService userService;

    private final LocalDate LOCAL_DATE = LocalDate.of(2022, 9, 10);
    @MockBean
    Clock clock;
    private final Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Autowired
    private WebApplicationContext webApplicationContext;

    private HttpMessageConverter<Object> mappingJackson2HttpMessageConverter;

    @Autowired
    void setConverters(HttpMessageConverter<Object>[] converters) {
        this.mappingJackson2HttpMessageConverter = Arrays.stream(converters)
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);
        assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
    }

    protected String json(Object object) throws Exception {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(object, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }

    @DisplayName("Handles a loan request from registered customer")
    @Order(1)
    @Test
    void handlesLoanRequestSuccessfully() throws Exception {
        // Arrange
        var principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        populateRecords();
        LoanRequest loanRequest = new LoanRequest(1000L, "2022-09-11");

        // Act
        ResultActions resultActions = mvc.perform(
                post("/api/loans/request-loan")
                        .principal(principal)
                        .content(this.json(loanRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        resultActions.andExpect(status().isOk());
        Loan loan = loanRepository.findByUserMsisdn("25412345678");
        assert loan != null;
        assertThat(loan.getDebt()).isEqualTo(1000L);
    }

    @DisplayName("Handles a repayment request from registered customer")
    @Order(2)
    @Test
    void handlesRepaymentRequestSuccessfully() throws Exception {
        // Arrange
        var principal = mock(Principal.class);
        when(principal.getName()).thenReturn("john");
        populateRecords();
        RepaymentRequest repaymentRequest = new RepaymentRequest(500L);

        // Act
        ResultActions resultActions = mvc.perform(
                post("/api/loans/pay-loan")
                        .principal(principal)
                        .content(this.json(repaymentRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        resultActions.andExpect(status().isOk());
        Loan loan = loanRepository.findByUserMsisdn("25412345678");
        assert loan != null;
        assertThat(loan.getDebt()).isEqualTo(500L);
    }

    public void populateRecords() {
        if (userService.getUser("john") != null) {
            return;
        }

        userService.saveRole(new Role(null, "ROLE_USER"));
        userService.saveRole(new Role(null, "ROLE_ADMIN"));

        userService.saveUser(new User(null, "25412345678", "John Doe", "john", "1234", 5000L));
        userService.saveUser(new User(null, "25400000000", "Admin", "admin", "1234", 0L));

        userService.addRoleToUser("john", "ROLE_USER");
        userService.addRoleToUser("admin", "ROLE_ADMIN");
    }
}