import {defineStore} from "pinia";

/** Enum representing the different roles a user can have */
export enum Role {
    Admin,
    Issuer,
    Editor
}

export namespace Role {
    const map: Map<string, Role> = new Map([
        ["ROLE_ADMIN", Role.Admin],
        ["ROLE_ISSUER", Role.Issuer],
        ["ROLE_EDITOR", Role.Editor]
    ])
    export function valueOf(str: string) {
        return map.get(str);
    }
}

/** Interface declaring the values / getters a user object should define */
export interface User {
    readonly forename: string
    readonly surname: string
    readonly email: string

    readonly roles: Role[]
    readonly username: string   //added username
}


export interface UserState {
    /** Object holding information about the current user. `null` if user is not logged in  */
    currentUser: User | null
    admin: boolean;
}

/** Store which holds a {@link UserState} */
export const userStore = defineStore("user", {
    state: (): UserState => ({
        currentUser: null,
        admin: false
    }),
    actions: {
        setUser(user: User | null): void {
            this.currentUser = user;
        },
        isAdmin(): boolean {
            if (this.currentUser == null) {
                return false
            }
            return this.currentUser.roles.includes(Role.Admin)
        }
    }
})