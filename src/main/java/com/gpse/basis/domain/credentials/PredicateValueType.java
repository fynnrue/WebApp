package com.gpse.basis.domain.credentials;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PredicateValueType {
    DATE("Date"),
    NUMBER("Number"),
    TODAY("Today");

    private final String jsonValue;

    PredicateValueType(final String jsonValue) {
        this.jsonValue = jsonValue;
    }

    @JsonValue
    public String jsonValue() {
        return jsonValue;
    }
}
