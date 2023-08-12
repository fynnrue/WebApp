package com.gpse.basis.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class StringNumberUnionSerializer extends JsonSerializer<StringNumberUnion> {

    @Override
    public void serialize(StringNumberUnion stringNumberUnion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeObject(stringNumberUnion.getValue());
    }
}