<script lang="ts">
import {defineComponent, onMounted, ref} from 'vue'
import UserCredentialCard from "@/main/vue/components/User Credential/UserCredentialCard.vue";
import {getAllCredentialGroups, getAllCredentials} from "@/main/vue/api/credentials";
import {CredentialGroup, CredentialSchema} from "@/main/vue/types/credentials";
import UserCredentialGroupCard from "@/main/vue/components/User Credential/UserCredentialGroupCard.vue";

export default defineComponent({
  name: "UserCredentialView",
  components: {UserCredentialGroupCard, UserCredentialCard},

  setup() {
    const credentials = ref([] as CredentialSchema[])
    const credentialGroups = ref([] as CredentialGroup[])
    const showGroups = ref(false)

    onMounted(() => {
      fetchCredentials()
      fetchGroups()
    })

    // functions below

    async function fetchCredentials() {
      credentials.value = await getAllCredentials()
      if (credentials.value.length <= 0) {
        credentials.value = [{name : "No Credentials yet", credentialDid : "", additional : "Currently there are no credentials", origin : "", id : BigInt(1), checklist : [] as string[], fields : [] as string[]}]
      }
    }

    async function fetchGroups() {
      credentialGroups.value = await getAllCredentialGroups()
      showGroups.value = credentialGroups.value.length > 0;
    }

    return {
      credentials,
      credentialGroups,
      showGroups,

    }
  }
})
</script>

<template>
  <div class="credentialContainer">
    <div class="text-h4 q-ma-xl">Credentials:</div>
    <div class="scrollContainer">
      <UserCredentialCard v-for="credential in credentials" :title="credential.name" :description="credential.additional" :origin="credential.origin"/>
    </div>
  </div>

  <div class="credentialContainer" v-if="showGroups">
    <div class="text-h4 q-ma-xl">Credential Groups:</div>
    <div class="scrollContainer">
      <UserCredentialGroupCard v-for="group in credentialGroups" :title="group.name" :origin="group.origin" :description="group.additional" :credentials="group.credentials"/>
    </div>
  </div>
</template>

<style scoped>
.credentialContainer {
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: start;
  height: 100%;
  margin-bottom: 2rem;
}

.scrollContainer {
  position: relative;
  display: grid;
  grid-template-rows: repeat(2, 1fr);
  grid-auto-flow: column;
  grid-template-columns: min-content;
  width: 100%;
  overflow-x: scroll;
  padding: 2rem;
  /*noinspection CssUnresolvedCustomProperty*/
  background-color: var(--background);
  border-radius: 1rem;
  grid-gap: 1rem;
  margin: -0.25rem;
}

::-webkit-scrollbar {
  width: 0.5rem;
  height: 0.5rem;
}
::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
::-webkit-scrollbar-thumb {
  background: var(--surface);
  border: 0 none transparent;
  border-radius: 50px;
}
::-webkit-scrollbar-track {
  background: transparent;
  border: 0 none transparent;
  border-radius: 0;
}
::-webkit-scrollbar-corner {
  background: transparent;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--q-primary);
}
::-webkit-scrollbar-thumb:active {
  background: var(--q-primary);
}

</style>