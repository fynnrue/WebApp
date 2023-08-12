import axios from 'axios';
import {Role, User} from '../stores/userStore'

/**
 * Authenticate a user with the backend.
 * The authorization token is being handled in the loginStore and saved for axios calls.
 *
 * @param email
 * @param password
 * @returns a promise containing the authorization header and the user object
 */
const authApi = async function login(email: string, password: string): Promise<{ authorization: string, user: User }> {
        try {
            const credentials = new URLSearchParams();
            credentials.append('username', email);
            credentials.append('password', password);
            const response = await axios.post('/api/authenticate', credentials);

            const data = response.data
            const user: User = {
                forename: data["forename"],
                surname: data["surname"],
                email: data["username"],
                roles: data["roles"].map(Role.valueOf),
                username: data["username"]  //added the username
            }

            return {
                user,
                authorization: response.headers.authorization
            }

        } catch (error) {
            throw error;
        }
    }

    export default authApi;

/**
 * Fetches our User object from the backend.
 */
export async function getUserInfo(): Promise<User> {
    const response = await axios.get("/api/profile")
    if (response.status != 200) {
        throw new Error("Could not fetch user info")
    }
    const data = response.data
    return {
        forename: data["forename"],
        surname: data["surname"],
        email: data["username"],
        roles: data["roles"].map(Role.valueOf),
        username: data["username"]  //added the username
    }
}

export async function requestPasswordReset(email: string): Promise<void> {
    const response = await axios.post('/api/user/requestResetPasswordPerMail', null , {
        params: {
            email: email
        }
    })
}

export async function resetPasswordWithToken(email: string, token: string, password: string): Promise<boolean> {
    const response = await axios.post('/api/user/resetPassword', null , {
        params: {
            email: email,
            token: token,
            password: password
        }
    })

    return response.status == 200;
}


