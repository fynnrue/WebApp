import {defineStore} from 'pinia'
import {Ref, ref} from 'vue'
import axios from "axios";
import api from "../api";
import {User, userStore} from "../stores/userStore";
import {getUserInfo} from "@/main/vue/api/auth";

export const useLoginStore = defineStore('loginStore', () => {
    const authenticated: Ref<boolean | null> = ref(null)
    const userSt = userStore()


    function authenticate(token: string | null) {
        if (token === null) {
            logout()
            return
        }

        authenticated.value = true
        localStorage.setItem("token", token);
        axios.defaults.headers['Authorization'] = token

    }

    interface credentials {
        email: string,
        password: string
    }

    function requestToken(credentials: credentials) {
        return new Promise<void>((resolve, reject) => {
            api.authApi(credentials.email, credentials.password).then(({user, authorization}) => {
                authenticate(authorization)
                storeUserInfo(user)
                resolve()
            }).catch((error) => {
                authenticate(null)

                if (error.response && error.response.status === 401) {
                    reject(new Error("activation"))
                } else {
                    reject("authentication")
                }
            })
        })
    }

    function storeUserInfo(user: User) {
        userSt.setUser(user)
    }

    async function loadPreviousSession(): Promise<User | null> {
        if (localStorage.getItem("token") === null) {
            authenticated.value = false;
            return null
        }
        try {
            const user = await getUserInfo();
            userSt.setUser(user);
            authenticated.value = true;
            return user;
        } catch (error) {
            logout();
            location.reload();
            throw new Error("Failed to get user info");
        }
    }

    function logout() {
        authenticated.value = false;
        // If there is no token, it does not do anything
        localStorage.removeItem("token")
        userSt.setUser(null)
        axios.defaults.headers['Authorization'] = null;
    }

    return {authenticated, authenticate, requestToken, logout, loadPreviousSession}
})

axios.defaults.headers['Authorization'] = localStorage.getItem('token')

