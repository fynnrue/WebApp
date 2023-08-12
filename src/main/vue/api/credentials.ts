import axios from "axios";
import {CredentialGroup, CredentialGroupUnion, CredentialSchema} from "@/main/vue/types/credentials";

/**
 * Get all credentials the current user has.
 */
export async function getPermittedCredentials(): Promise<CredentialSchema[]> {
    const response = await axios.get("/api/credentials/permitted")
    if (response.status != 200) {
        throw new Error(`Invalid response from server. Status: ${response.status}`)
    }
    return response.data
}

/**
 * Get all issuable credentials the current user is allowed to issue.
 */
export async function getIssuableCredentials(): Promise<CredentialSchema[]> {
    const response = await axios.get("/api/credentials/issuable")
    if (response.status != 200) {
        throw new Error(`Invalid response from server. Status: ${response.status}`)
    }
    return response.data
}

/**
 * Get a single CredentialSchema from the backend.
 * @param id The id of the desired CredentialSchema
 */
export async function getCredentialById(id: bigint): Promise<CredentialSchema> {
    const response = await axios.get(`/api/credentials/${id}`)
    if (response.status != 200) {
        throw new Error(`Invalid response from server. Status: ${response.status}`)
    }
    return response.data
}

/**
 * Get all CredentialSchemas from the backend.
 */
export async function getAllCredentials(): Promise<CredentialSchema[]> {
    const response = await axios.get("/api/credentials")
    if (response.status != 200) {
        throw new Error("Could not fetch credentials")
    }
    return response.data
}

export async function getAllCredentialGroupUnions(): Promise<CredentialGroupUnion[]> {
    const response = await axios.get("/api/credentialGroupUnions")
    if (response.status != 200) {
        throw new Error("Could not fetch credential group unions")
    }
    return response.data
}

/**
 * Change the checklist of a credential.
 * @param id The id of the credential.
 * @param checklist The updated checklist.
 */
export async function postChecklist(id: bigint, checklist: string[]) {
    const response = await axios.post(`/api/credentials/${id}/checklist`, {
        items: checklist
    })
    if (response.status != 200) {
        throw new Error("Could not update checklist")
    }
}

export async function getAllCredentialGroups(): Promise<CredentialGroup[]> {
    const response = await axios.get("/api/admin/credentialgroups/all")
    if (response.status != 200) {
        throw new Error("Could not fetch credential groups")
    }
    return response.data
}

export async function getCredentialGroupByName(name: string): Promise<CredentialGroup> {
    const data = {params: {name: name}}
    const response = await axios.get(`/api/admin/credentialgroups/get`, data)
    if (response.status != 200) {
        throw new Error("Could not fetch credential group")
    }
    return response.data
}

export async function updateCredential(id: bigint, name: string, origin: string, additional:string) {
    const credential = new URLSearchParams();
    credential.append('name', name);
    credential.append('origin', origin);
    credential.append('additional', additional);
    const response = await axios.post(`/api/admin/credentials/edit/${id}/save`, credential);
    return response.data as string;
}