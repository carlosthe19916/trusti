
package org.trusti.dto;

import java.time.ZonedDateTime;

public record AdvisoryDto(
        Long id,
        String identifier,
        String title,
        String severity,
        ZonedDateTime releaseDate
) {
}
