package org.trusti.models;

public record Filters(String field, Operation operation, String value) {
    public enum Operation {
        Eq("="),
        Dif("!="),
        Like("~"),
        Gt(">"),
        Gte(">="),
        Lt("<"),
        Lte("<=");

        private final String value;

        Operation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
