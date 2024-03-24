package org.trusti.dto;

public record GitDetailsDto(String ref, String workingDirectory) {
    public GitDetailsDto(String ref, String workingDirectory) {
        this.ref = ref;
        this.workingDirectory = workingDirectory;
    }

    public String ref() {
        return this.ref;
    }

    public String workingDirectory() {
        return this.workingDirectory;
    }
}
