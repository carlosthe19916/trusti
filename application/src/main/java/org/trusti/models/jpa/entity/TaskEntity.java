package org.trusti.models.jpa.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.trusti.models.TaskState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "task")
public class TaskEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "task_sequence")
    public Long id;

    @NotBlank
    @Column(name = "name")
    public String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    public TaskState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source")
    public SourceEntity source;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    public Date createTime;

    @Column(name = "started")
    public Date started;

    @Column(name = "terminated")
    public Date terminated;

    @Column(name = "job")
    public String job;

    @Column(name = "error")
    public String error;

    @Column(name = "image")
    public String image;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "task")
    public List<AdvisoryEntity> advisories = new ArrayList<>();
}
