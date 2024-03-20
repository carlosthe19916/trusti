package org.trusti.models.jpa.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;
import java.time.ZonedDateTime;

@Entity
@Table(name = "advisory", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"identifier"})
})
public class AdvisoryEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "organization_sequence")
    public Long id;

    @NotBlank
    @Column(name = "identifier")
    public String identifier;

    @NotBlank
    @Column(name = "title")
    public String title;

    @NotBlank
    @Column(name = "severity")
    public String severity;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "release_date")
    public Date releaseDate;

}