package org.trusti.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record SortBy(String field, boolean asc) {

    public String getQuery() {
        return field + ":" + (asc() ? "asc" : "desc");
    }

    public static List<SortBy> getSorts(List<String> sortBy, String... validFieldNames) {
        if (sortBy == null) {
            return Collections.emptyList();
        }
        List<String> validFieldNamesList = validFieldNames != null ? Arrays.asList(validFieldNames) : Collections.emptyList();
        return sortBy.stream()
                .flatMap(f -> Stream.of(f.split(",")))
                .map(f -> {
                    String[] split = f.trim().split(":");
                    String fieldName = !split[0].isEmpty() ? split[0] : null;
                    boolean isAsc = split.length <= 1 || split[1].equalsIgnoreCase("asc");
                    return new SortBy(fieldName, isAsc);
                })
                .filter(f -> validFieldNamesList.contains(f.field()))
                .collect(Collectors.toList());
    }

}
