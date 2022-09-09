package com.flycode.lendingandrepaymentservice.models.mappers;


import com.flycode.lendingandrepaymentservice.models.Loan;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.OffsetTime;

public class LoanMapper implements RowMapper<Loan> {
    private Clock clock;

    @Override
    public Loan mapRow(ResultSet rs, int rowNum) throws SQLException {
        Loan loan = new Loan();
        loan.setId(rs.getLong("id"));
        loan.setDebt(rs.getLong("debt"));
        loan.setDueDate(rs.getDate("due_date").toLocalDate().atTime(OffsetTime.MIN));

        return loan;
    }
}
