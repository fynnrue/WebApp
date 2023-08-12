import {defineStore} from "pinia";
import {CredentialGroupUnion} from "@/main/vue/types/credentials";
import {getAllCredentialGroupUnions} from "@/main/vue/api/credentials";

export interface CredentialStoreState {
    credentialGroupUnions: CredentialGroupUnion[] | null
}


export const useCredentialStore = defineStore("credentialStore", {
    state: (): CredentialStoreState => ({
        credentialGroupUnions: null
    }),
    actions: {
        async updateCredentials(): Promise<void> {
            this.credentialGroupUnions = await getAllCredentialGroupUnions()
        },
        async ensureInitialized(): Promise<void> {
            if (this.credentialGroupUnions == null) {
                await this.updateCredentials()
            }
        }
    }
})