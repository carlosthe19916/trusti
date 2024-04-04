package org.trusti.models.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GitDetailsEntity {

    @Column(name = "git_ref")
    public String ref;

    @Column(name = "git_working_directory")
    public String workingDirectory;

}
