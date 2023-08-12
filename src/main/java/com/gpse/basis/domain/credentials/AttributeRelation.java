package com.gpse.basis.domain.credentials;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This enum represents the relation between an two values.
 */
public enum AttributeRelation {
    EQUAL("="),
    LOWER("<"),
    GREATER(">"),
    GREATER_EQUAL(">="),
    LOWER_EQUAL("<=");

    /**
     * Returns the enum value from the given symbol.
     * @param symbol The symbol to get the enum value from.
     * @return The enum value.
     */
    public static AttributeRelation fromSymbol(String symbol) {
        return switch (symbol) {
            case ">" -> GREATER;
            case "<" -> LOWER;
            case "=" -> EQUAL;
            case ">=" -> GREATER_EQUAL;
            case "<=" -> LOWER_EQUAL;
            default -> null;
        };
    }

    /**
     * The symbol of the relation.
     */
    public final String symbol;

    AttributeRelation(final String symbol) {
        this.symbol = symbol;
    }

    @JsonValue
    public String jsonValue() {
        return symbol;
    }
}
