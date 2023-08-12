package com.gpse.basis.web.cmd;

import com.gpse.basis.domain.StringNumberUnion;
import com.gpse.basis.domain.credentials.AttributeRequirement;

import java.util.List;

public class AttributeRequirementCmd {
    private String attributeName;
    private List<StringNumberUnion> values;

    public AttributeRequirementCmd(final String attributeName, final List<StringNumberUnion> values) {
        this.attributeName = attributeName;
        this.values = values;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    public List<StringNumberUnion> getValues() {
        return values;
    }

    public void setValues(final List<StringNumberUnion> values) {
        this.values = values;
    }

    public AttributeRequirement toAttributeRequirement() {
        final var requirement = new AttributeRequirement();
        requirement.setAttributeName(getAttributeName());
        requirement.setValues(getValues());
        return requirement;
    }
}
