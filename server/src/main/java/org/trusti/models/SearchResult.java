package org.trusti.models;

import java.util.List;

public record SearchResult<T>(long count, List<T> list) {
}
