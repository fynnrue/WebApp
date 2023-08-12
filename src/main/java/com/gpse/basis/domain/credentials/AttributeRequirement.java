package com.gpse.basis.domain.credentials;

import com.gpse.basis.domain.StringNumberUnion;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;

import java.util.List;

@Embeddable
public class AttributeRequirement {
    private String attributeName;

    @ElementCollection
    private List<StringNumberUnion> values;

    public AttributeRequirement(
            final String attributeName,
            final List<StringNumberUnion> values
    ) {
        this.attributeName = attributeName;
        this.values = values;
    }

    public AttributeRequirement() {}

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public List<StringNumberUnion> getValues() {
        return values;
    }

    public void setValues(List<StringNumberUnion> values) {
        this.values = values;
    }
}
