package org.trusti.models.jpa.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;

@Entity
@Table(name = "advisory")
public class AdvisoryEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "advisory_sequence")
    public Long id;

    @NotBlank
    @Column(name = "identifier")
    public String identifier;

    @NotBlank
    @Column(name = "title")
    public String title;

    @Column(name = "severity")
    public String severity;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "release_date")
    public Date releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task")
    public TaskEntity task;

}
