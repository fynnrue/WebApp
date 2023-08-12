package com.gpse.basis.domain.credentials;

import com.fasterxml.jackson.annotation.JsonView;
import com.gpse.basis.domain.Views;
import jakarta.persistence.Embeddable;

/**
 * Represents a relation between an attribute and a value.
 */
@JsonView(Views.Public.class)
@Embeddable
public class PredicateRequirement {
    private String attributeName;
    private AttributeRelation relation;

    private String attributeValue;

    private PredicateValueType valueType;

    public PredicateRequirement(
            final String attributeName,
            final AttributeRelation relation,
            final String value,
            final PredicateValueType valueType
    ) {
        this.attributeName = attributeName;
        this.relation = relation;
        this.attributeValue = value;
        this.valueType = valueType;
    }

    public PredicateRequirement() {}

    @JsonView(Views.Public.class)
    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @JsonView(Views.Public.class)
    public AttributeRelation getRelation() {
        return relation;
    }

    public void setRelation(AttributeRelation relation) {
        this.relation = relation;
    }

    @JsonView(Views.Public.class)
    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String value) {
        this.attributeValue = value;
    }

    @JsonView(Views.Public.class)
    public PredicateValueType getValueType() {
        return valueType;
    }

    public void setValueType(PredicateValueType valueType) {
        this.valueType = valueType;
    }
}
