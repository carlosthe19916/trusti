
package org.trusti.dto;

import java.util.Date;

public record AdvisoryDto(
        Long id,
        String identifier,
        String title,
        String severity,
        Date releaseDate
) {
}
