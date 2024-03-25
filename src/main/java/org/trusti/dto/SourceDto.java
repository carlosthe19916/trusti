package org.trusti.dto;

import org.trusti.models.SourceType;

public record SourceDto(Long id, SourceType type, String url, GitDetailsDto gitDetails) {
    public SourceDto(Long id, SourceType type, String url, GitDetailsDto gitDetails) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.gitDetails = gitDetails;
    }

    public Long id() {
        return this.id;
    }

    public SourceType type() {
        return this.type;
    }

    public String url() {
        return this.url;
    }

    public GitDetailsDto gitDetails() {
        return this.gitDetails;
    }
}

