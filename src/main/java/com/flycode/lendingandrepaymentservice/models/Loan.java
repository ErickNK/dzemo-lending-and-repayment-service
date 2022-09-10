package com.flycode.lendingandrepaymentservice.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Table(name = "loans")
@Entity()
@EntityListeners({AuditingEntityListener.class})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Loan implements Serializable {

    public Loan(Long id, Long debt, LocalDate dueDate) {
        this.id = id;
        this.debt = debt;
        this.dueDate = dueDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column()
    private Long debt = 0L;

    @Column(name = "user_msisdn")
    private String userMsisdn;

    @Column(name = "due_date", columnDefinition = "DATE")
    private LocalDate dueDate;

    @Column(name = "created_date", columnDefinition = "DATETIME", nullable = false)
    @CreatedDate
    private OffsetDateTime createdDate;

    @Column(name = "updated_date", columnDefinition = "DATETIME", nullable = false)
    @LastModifiedDate
    private OffsetDateTime updatedDate;

//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_msisdn", referencedColumnName = "msisdn", columnDefinition = "VARCHAR", nullable = false)
//    private User user;

}
