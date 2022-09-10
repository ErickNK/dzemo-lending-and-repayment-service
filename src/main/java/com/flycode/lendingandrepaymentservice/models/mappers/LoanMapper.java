package com.flycode.lendingandrepaymentservice.models.mappers;


import com.flycode.lendingandrepaymentservice.models.Loan;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.OffsetTime;

public class LoanMapper implements RowMapper<Loan> {

    private Clock clock;

    public LoanMapper(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Loan mapRow(ResultSet rs, int rowNum) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getLong("id"));
        loan.setDebt(rs.getLong("debt"));
        loan.setUserMsisdn(rs.getString("user_msisdn"));
        loan.setDueDate(rs.getDate("due_date").toLocalDate());
//        loan.setCreatedDate(rs.getDate("created_date").toInstant().atZone(clock.getZone()).toOffsetDateTime());
//        loan.setUpdatedDate(rs.getDate("updated_date").toInstant().atZone(clock.getZone()).toOffsetDateTime());
        return loan;
    }
}
