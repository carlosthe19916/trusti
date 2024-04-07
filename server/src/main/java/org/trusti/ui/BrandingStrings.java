package org.trusti.ui;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Optional;

@RegisterForReflection
public record BrandingStrings(
        Application application,
        About about,
        Masthead masthead
) {
    public record Application(
            String title,
            Optional<String> name,
            Optional<String> description
    ) {
    }

    public record About(
            String displayName,
            Optional<String> imageSrc,
            Optional<String> documentationUrl
    ) {
    }

    public record Masthead(
            Optional<String> leftBrand,
            Optional<String> leftTitle,
            Optional<String> rightBrand
    ) {
    }
}
