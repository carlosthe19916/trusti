package org.trusti.ui;

import java.util.Optional;

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
