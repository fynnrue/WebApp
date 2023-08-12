import axios from 'axios'
import {CredentialGroup} from "@/main/vue/types/credentials";

export default {
    getCredentialGroup,
    getCredentialGroups,
    addCredentialGroup,
    deleteCredentialGroup,
    updateCredentialGroup
}


async function getCredentialGroup(name: string):Promise <CredentialGroup> {
    const params = new URLSearchParams();
    params.append('name', name);
    const response = await axios.get("/api/admin/credentialgroups/get",
        {params: {
            name: params.get('name')
            }});

    return response.data as CredentialGroup;
}

async function getCredentialGroups(): Promise<CredentialGroup[]> {
    const response = await axios.get("/api/admin/credentialgroups/all");
    if (response.status !== 200) {
        throw new Error("Error while fetching Users");
    }
    return response.data as CredentialGroup[];
}

async function addCredentialGroup(CredentialIDs: string, name: string, origin: string, additionalText: string) {
    const credentialGroup = new URLSearchParams();
    credentialGroup.append('CredentialIDs', CredentialIDs);
    credentialGroup.append('name', name);
    credentialGroup.append('origin', origin);
    credentialGroup.append('additional', additionalText);
    const response = await axios.post('/api/credentialgroups/add', credentialGroup);
    return response.data as string;
}

async function updateCredentialGroup(CredentialIDs: string, name: string,
                                     origin: string, additionalText: string, oldGroup: string) {
    const credentialGroup = new URLSearchParams();
    credentialGroup.append('CredentialIDs', CredentialIDs);
    credentialGroup.append('name', name);
    credentialGroup.append('origin', origin);
    credentialGroup.append('additional', additionalText);
    credentialGroup.append('oldGroupName', oldGroup);
    const response = await axios.post('/api/admin/credentialgroups/update', credentialGroup);
    return response.data as string;
}

async function deleteCredentialGroup(group: string) {
    const credentialGroup = new URLSearchParams();
    credentialGroup.append('name', group);
    return axios.post('/api/admin/credentialgroups/delete', credentialGroup);
}

