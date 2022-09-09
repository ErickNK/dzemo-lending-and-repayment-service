package com.flycode.lendingandrepaymentservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Table(name = "loans")
@Entity()
@EntityListeners({AuditingEntityListener.class})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column()
    private Long debt;

    @Column(name = "due_date", columnDefinition = "DATETIME")
    private OffsetDateTime dueDate;

    @Column(name = "created_date", columnDefinition = "DATETIME", nullable = false)
    @CreatedDate
    private OffsetDateTime createdDate;

    @Column(name = "updated_date", columnDefinition = "DATETIME", nullable = false)
    @LastModifiedDate
    private OffsetDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_msisdn", columnDefinition = "VARCHAR", nullable = false)
    private User user;
}
