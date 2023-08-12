import {AttributeRelation, PredValueType} from "@/main/vue/components/CredentialCard.vue";

/**
 * Represents a credential schema.
 */
export interface CredentialSchema {
    readonly id: bigint
    readonly name: string
    readonly origin: string
    readonly additional: string
    readonly credentialDid: string
    readonly fields: string[]
    readonly checklist: string[]
}

/**
 * Represents a group of credentials.
 */
export interface CredentialGroup {
    //readonly id: bigint
    //readonly name: bigint
    readonly credentials: CredentialSchema[]
    credentialString: string
    name: string
    //credentialIDs: string
    origin: string
    additional: string
}

/**
 * Represents a value which can either be a Credential or a CredentialGroup.
 */
export interface CredentialGroupUnion {
    readonly id: bigint
    readonly name: string
    readonly isGroup: boolean

    readonly attributes: string[]
}

/**
 * Represents a connection of credentials which are required to enter a room.
 */
export interface CredentialORConnection {
    credentials: CredentialGroupUnion[]
    predicateRequirements: PredicateRequirement[]
    attributeRequirement: AttributeRequirement

}

/**
 * Represents a value which can either be a string or a number.
 */
export interface StringNumberUnion {
    readonly string: string | null
    readonly number: number | null
}

/**
 * Represents an attribute requirement for a credential.
 */
export interface AttributeRequirement {
    readonly attributeName: string
    readonly values: StringNumberUnion[]
}



export interface PredicateRequirement {
    attributeName: string
    attributeValue: number | string
    relation: AttributeRelation
    valueType: PredValueType
}