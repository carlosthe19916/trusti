package org.trusti.models;

public record Page(int offset, int limit) {
    public static Page from(Integer offset, Integer limit) {
        if (offset == null || offset < 0) {
            offset = 0;
        }

        if (limit == null || limit > 1000) {
            limit = 1000;
        }
        if (limit < 0) {
            limit = 10;
        }

        return new Page(offset, limit);
    }
}
