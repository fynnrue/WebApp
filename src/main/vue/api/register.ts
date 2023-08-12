import axios from 'axios';

export async function confirmRegistration(email: string, token: string): Promise<boolean> {
    const credentials = new URLSearchParams();
    credentials.append('email', email);
    credentials.append('token', token);

     const valid = await axios.post('/api/user/registration/confirm', credentials);
     return valid.data;
}

export default {
    /**
     * Register a new user. The user is not activated by default!
     *
     * Needs some work.
     *
     * @param forename
     * @param surname
     * @param email
     * @param password
     */
    registerUser(forename: string, surname: string, email: string, password: string) {
        const credentials = new URLSearchParams();
        credentials.append('forename', forename);
        credentials.append('surname', surname);
        credentials.append('email', email);
        credentials.append('password', password);
        return axios.post('/api/user/registration', credentials);
    }
}