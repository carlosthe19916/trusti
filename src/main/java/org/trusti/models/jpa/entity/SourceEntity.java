package org.trusti.models.jpa.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.trusti.models.SourceType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "source")
public class SourceEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "source_sequence")
    public Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    public SourceType type;

    @NotBlank
    @Column(name = "url")
    public String url;

    @Embedded
    public GitDetailsEntity gitDetails;

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL}, orphanRemoval = true, mappedBy = "source")
    public List<TaskEntity> tasks = new ArrayList<>();
}
