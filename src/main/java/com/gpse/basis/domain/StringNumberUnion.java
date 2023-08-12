package com.gpse.basis.domain;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.annotation.Nullable;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

import java.util.Optional;

/**
 * Class representing either a string or a number.
 */
@Embeddable
@JsonSerialize(using = StringNumberUnionSerializer.class)
public class StringNumberUnion {
    private Double number;
    private String string;

    public StringNumberUnion(Double value) {
        this.number = value;
    }

    public StringNumberUnion(Integer value) {
        this.number = value.doubleValue();
    }

    public StringNumberUnion(String value) {
        this.string = value;
    }

    public StringNumberUnion() {}

    public static StringNumberUnion fromJsonNode(JsonNode node) {
        if (node.isNumber()) {
            return new StringNumberUnion(node.asDouble());
        } else if (node.isTextual()) {
            return new StringNumberUnion(node.asText());
        } else {
            throw new IllegalArgumentException("Node must be either a number or a string.");
        }
    }

    @Nullable
    public Double getNumber() {
        return number;
    }

    public void setNumber(Double number) {
        this.number = number;
    }

    @Nullable
    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Transient
    @JsonView(Views.Internal.class)
    public Object getValue() {
        return Optional.ofNullable((Object) getNumber()).orElse(getString());
    }
}
