package org.trusti.dto;

public record GitDetailsDto(String ref, String workingDirectory) {

    public String ref() {
        return this.ref;
    }

    public String workingDirectory() {
        return this.workingDirectory;
    }
}
