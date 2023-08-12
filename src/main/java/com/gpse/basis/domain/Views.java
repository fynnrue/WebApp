package com.gpse.basis.domain;

/**
 * Used for JsonView()
 * Can be used for filtering the data that is sent to the client
 * Example Usage: @JsonView(Views.Public.class)
 * Example Usage: @JsonView(Views.Internal.class)
 */
public class Views {
    public static class Public {
    }

    public static class Internal extends Public {
    }
}
