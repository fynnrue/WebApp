<script lang="ts">
import {defineComponent, onMounted, ref} from 'vue'
import {useI18n} from "vue-i18n";
import UserCredentialCard from "@/main/vue/components/User Credential/UserCredentialCard.vue";
import {CredentialSchema} from "@/main/vue/types/credentials";
import {getCredentialGroupByName} from "@/main/vue/api/credentials";

export default defineComponent({
  name: "UserCredentialGroupCard",
  components: {UserCredentialCard},
  props: {
    title: String,
    origin: String,
    description: String,
    credentials: Array as () => CredentialSchema[],
  },
  setup (props) {
    const {t} = useI18n()
    const isExpanded = ref(false)
    const groupName = ref("")
    const groupOrigin = ref("")
    const groupDescription = ref("")

    onMounted(() => {
      fetchGroupData()
    })

    function expandCard() {
      isExpanded.value = !isExpanded.value
    }

    async function fetchGroupData() {
      const group = await getCredentialGroupByName(props.title!)
      groupName.value = group.name
      groupOrigin.value = group.origin
      groupDescription.value = group.additional
    }

    return {
      t,
      isExpanded,
      expandCard,
      groupName,
      groupOrigin,
      groupDescription
    }
  }
})
</script>

<template>
  <div id="groupCard" @click="expandCard">
    <div id="top">
      <div id="title">{{title || t('userCredentialCard.error')}}</div>
      <div id="origin">{{origin || t('userCredentialCard.noOrigin')}}</div>
    </div>
    <div id="bottom">
      <div id="description">{{description || t('userCredentialCard.noDescription')}}</div>
    </div>
  </div>

  <q-dialog class="q-dialog" v-model="isExpanded" :full-width="true">
    <div id="bigCard" @click.self="expandCard">
      <UserCredentialCard :title="groupName" :description="groupDescription" :origin="groupOrigin"/>
      <div class="scrollContainer">
        <UserCredentialCard v-for="credential in credentials" :title="credential.name" :description="credential.additional" :origin="credential.origin"/>
      </div>
    </div>
  </q-dialog>
</template>

<style scoped>
#groupCard {
  position: relative;
  /*noinspection CssUnresolvedCustomProperty*/
  background-color: var(--background);
  min-height: 23vh;
  width: fit-content;
  min-width: calc(1.618 * 23vh);
  border-radius: 0.25rem;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.4);
  transition: scale 0.2s cubic-bezier(.48,-0.82,.51,1.77), box-shadow 0.2s cubic-bezier(.48,-0.82,.51,1.77);
  cursor: pointer;
}

#groupCard:hover {
  scale: 1.05;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
}

#top{
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: start;
  min-height: 30%;
  background-color: var(--surface);
  border-top-left-radius: inherit;
  border-top-right-radius: inherit;
  padding: 0.25rem 1rem 0.5rem;
}

#title {
  position: relative;
  font-size: x-large;
  font-weight: bold;
  text-align: center;
  width: fit-content;
}

#description {
  position: relative;
  font-size: medium;
  text-align: left;
  padding: 1rem;
}

#origin {
  position: relative;
  font-size: medium;
  text-align: center;
  width: fit-content;
}

#bigCard {
  position: relative;
  height: 85vh;
  width: 100%;
  border-radius: 1rem;
  padding: 1rem;
  display: grid;
  grid-template-columns: 1fr;
  grid-auto-rows: auto;
  justify-items: center;
  align-items: start;
  gap: 1rem;
  background-color: transparent;
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