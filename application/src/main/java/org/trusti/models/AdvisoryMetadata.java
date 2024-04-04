package org.trusti.models;

import java.util.Date;

public record AdvisoryMetadata(String identifier, String title, String severity, Date releaseDate) {
}
