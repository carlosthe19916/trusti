package org.trusti.dto;

import org.trusti.models.SourceType;

public record SourceDto(Long id, SourceType type, String url, String taskImage, GitDetailsDto gitDetails) {
    public SourceDto(Long id, SourceType type, String url, String taskImage, GitDetailsDto gitDetails) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.taskImage = taskImage;
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

    public String taskImage() {
        return this.taskImage;
    }

    public GitDetailsDto gitDetails() {
        return this.gitDetails;
    }
}

