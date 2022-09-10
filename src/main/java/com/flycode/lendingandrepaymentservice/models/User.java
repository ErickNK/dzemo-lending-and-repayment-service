package com.flycode.lendingandrepaymentservice.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Table(name = "users")
@Entity()
@EntityListeners({AuditingEntityListener.class})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    public User(Long id, String msisdn, String name, String username, String password, Long loanLimit) {
        this.id = id;
        this.msisdn = msisdn;
        this.name = name;
        this.username = username;
        this.password = password;
        this.loanLimit = loanLimit;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String msisdn;

    @Column()
    private String name;

    @Column(unique = true)
    private String username;

    @Column()
    private String password;

    @Column(name = "loan_limit")
    private Long loanLimit;

    @Column(name = "created_date", columnDefinition = "DATETIME", nullable = false)
    @CreatedDate
    private OffsetDateTime createdDate;

    @Column(name = "updated_date", columnDefinition = "DATETIME", nullable = false)
    @LastModifiedDate
    private OffsetDateTime updatedDate;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private LoanDefaulter loanDefaulter;

//    @ToString.Exclude
//    @EqualsAndHashCode.Exclude
//    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    @Transient
    private Loan loan;

}


