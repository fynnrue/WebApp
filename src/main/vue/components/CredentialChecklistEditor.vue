<template>
  <div class="admin-navigation-bar">
    <AdminNavigationBar section="credentials"/>
  </div>

  <q-card class="card">
    <div v-if="credential != null" class="centerItems">
      <h4>{{ $t("issuer.editChecklistItems") }} {{ credential.name }}</h4>
      <div class="centerItems">
        <div v-for="i in fieldState.keys()" style="display: flex; flex-direction: row">
          <q-input :label="$t('issuer.checklistFieldLabel')" v-model="fieldState[i]" style="width: 300px"/>
          <q-btn icon="delete" @click="deleteItem(i)" style="margin: 10px;"/>
        </div>
        <q-btn flat color="primary" :label="$t('issuer.addChecklistField')" @click="addItem()"
               style="align-self: center"/>
      </div>
    </div>
  </q-card>

  <q-page-sticky position="bottom-right" :offset="[18, 18]">
    <q-btn fab icon="save" color="primary" @click="save()"/>
    <q-tooltip color="primary">{{ $t("issuer.save") }}</q-tooltip>
  </q-page-sticky>

  <q-page-sticky position="bottom-left" :offset="[18, 18]">
    <q-btn fab icon="close" color="accent" @click="cancel()"/>
    <q-tooltip color="primary">{{ $t("issuer.cancel") }}</q-tooltip>
  </q-page-sticky>
</template>

<script lang="ts">

export default {
  name: "CredentialsChecklistEditor",
}
</script>

<script setup lang="ts">
import {CredentialSchema} from "@/main/vue/types/credentials";
import {getCredentialById, postChecklist} from "@/main/vue/api/credentials";
import {onMounted, Ref, ref} from "vue";
import router from "@/main/vue/router";
import {useQuasar} from "quasar";
import {useI18n} from "vue-i18n";
import AdminNavigationBar from "@/main/vue/components/AdminNavigationBar.vue";

const props = defineProps<{
  credentialId: bigint
}>()

const q = useQuasar()
const {t} = useI18n()

function deleteItem(index: number) {
  fieldState.value.splice(index, 1)
}

function addItem() {
  if (fieldState.value.includes("")) return
  fieldState.value.push("")
}

async function save() {
  try {
    if (fieldState.value.length != 0) {
      if (fieldState.value.includes("")) {
        fieldState.value.pop()
      }
    }
    await postChecklist(props.credentialId, fieldState.value)
    router.back()
  } catch (e) {
    q.notify({
      type: 'negative',
      message: t("issuer.errorCouldNotSaveChecklist"),
    })
  }
}

function cancel() {
  router.back()
}

const credential = ref(null) as Ref<null | CredentialSchema>
const fieldState: Ref<string[]> = ref([])

onMounted(() => {
  getCredentialById(props.credentialId!).then((c) => {
    credential.value = c
    fieldState.value = c.checklist
  })
})

</script>

<style scoped>
.admin-navigation-bar {
  position: fixed;
  left: 0;
  width: 100%;
  z-index: 999;
}

.card {
  display: flex;
  padding: 20px;
  width: 70%;
  max-width: 700px;
  min-width: 400px;
  margin: 60px auto 20px;
  background-color: var(--background);
}

.centerItems {
  align-items: center;
  text-align: center;
  justify-content: center;
  display: flex;
  flex-direction: column;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
}
</style>