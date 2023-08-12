import axios from 'axios';
import {User} from "../types/user";

export default {
    fetchFilteredUsers,
    activateUsers,
    deactivateUsers,
    deleteUsers,
    fetchAllCredentials,
    fetchPermittedCredentials,
    fetchUserRoles,
    fetchUserInformation,
    setIssuerCredentials,
    fetchIssuableCredentials,
    setUserRoles
}

async function fetchFilteredUsers(permission: string, activated: string, searchType: string, search: string): Promise<User[]> {
    const params = new URLSearchParams([
        ['permission', permission], ['activated', activated], ['searchType', searchType], ['search', search]
    ]);

    const response = await axios.get("/api/admin/users/filter", { params });
    if (response.status !== 200) {
        throw new Error("Error while fetching Users");
    }
    return response.data as User[];
}

async function activateUsers(emails: string) {
    const credentials = new URLSearchParams();
    credentials.append('emails', emails);

    const response = await axios.post("/api/admin/users/activate", credentials);
    if (response.status !== 200) {
        throw new Error("Error while activating Users");
    }

    return response
}

async function deactivateUsers(emails: string) {
    const credentials = new URLSearchParams();
    credentials.append('emails', emails);

    const response = await axios.post("/api/admin/users/deactivate", credentials);
    if (response.status !== 200) {
        throw new Error("Error while deactivating Users");
    }

    return response
}

async function deleteUsers(emails: string) {
    const credentials = new URLSearchParams();
    credentials.append('emails', emails);

    const response = await axios.post("/api/admin/users/delete", credentials);
    if (response.status !== 200) {
        throw new Error("Error while removing Users");
    }

    return response
}

async function fetchAllCredentials(): Promise<Credential[]> {
    const response = await axios.get("/api/credentials");
    if (response.status !== 200) {
        throw new Error("Error while fetching all Credentials");
    }
    return response.data as Credential[];
}

async function fetchPermittedCredentials(email: string): Promise<Credential[]> {
    const response = await axios.get("/api/users/" + email + "/credentials");
    if (response.status !== 200) {
        throw new Error("Error while fetching Users");
    }
    return response.data as Credential[];
}

async function fetchUserRoles(email: string): Promise<String[]> {
    const response = await axios.get("/api/users/" + email + "/roles");
    if (response.status !== 200) {
        throw new Error("Error while fetching Users");
    }
    return response.data as String[];
}

async function fetchUserInformation(email: string): Promise<User> {
    const response = await axios.get("/api/users/email/" + email);
    if (response.status !== 200) {
        throw new Error("Error while fetching User");
    }
    return response.data as User;
}

async function setIssuerCredentials(email: string, credentials: string) {
    const params = new URLSearchParams([
        ['email', email], ['credentials', credentials]
    ]);

    const response = await axios.post("/api/admin/users/credentials/allowIssue", params);
    if (response.status !== 200) {
        throw new Error("Error while setting Credentials");
    }
    return response;
}

async function fetchIssuableCredentials(email: string): Promise<Credential[]> {
    const response = await axios.get("/api/admin/users/credentials/issuable/" + email);
    if (response.status !== 200) {
        throw new Error("Error while fetching issuable Credentials");
    }
    return response.data as Credential[];
}

async function setUserRoles(email: string, roles: string) {
    const params = new URLSearchParams([
        ['roles', roles]
    ]);

    const response = await axios.post("/api/admin/users/roles/" + email, params);
    if (response.status !== 200) {
        throw new Error("Error while setting Roles");
    }
    return response;
}
