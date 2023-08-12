<script lang="ts">
import {onMounted, Ref, ref} from 'vue'
import {useQuasar} from "quasar"
import {useI18n} from "vue-i18n"
import api from "../api"
import router from "@/main/vue/router";
import {useRoute} from "vue-router";
import {getAllCredentials} from "@/main/vue/api/credentials";
import UploadDialogue from "@/main/vue/components/UploadDialogue.vue";
import AdminNavigationBar from "@/main/vue/components/AdminNavigationBar.vue";
import {useCredentialStore} from "@/main/vue/stores/credentialStore";

export interface Credential {
  ID: number
  DID: string
  name: string
}

interface CredentialGroup {
  CredentialIDs: string
  name: string
  origin: string
  AdditionalText: string
}


export interface Column {
  align?: "left" | "right" | "center"
  field: keyof Credential | ((row: Credential) => any);
  label: string;
  name: string;
  required?: boolean;
  sortable?: boolean;
  sort?: (a: any, b: any, rowA: any, rowB: any) => number;
  sortOrder?: "ad" | "da";
  format?: (val: any, row: any) => any;
  style?: string | ((row: any) => string);
  classes?: string | ((row: any) => string);
  filter?: boolean;
}

let columnPlan: Column[] = [
  {
    name: 'ID',
    required: true,
    label: 'ID',
    align: 'left',
    field: row => row.ID,
    format: val => `${val}`,
    sortable: true
  },
  {name: 'name', label: 'Name', align: 'left', field: 'name', sortable: true, filter: true},
  {name: 'DID', label: 'DID', align: 'left', field: 'DID', sortable: true, filter: true}
]
const rows = [
  {
    ID: 1,
    name: 'DRK',
    DID: 'egal'
  },
  {
    ID: 2,
    name: 'DRK2',
    DID: 'test'
  }
]
export default {
  name: "CredentialGroupEdit",
  components: {AdminNavigationBar, UploadDialogue},

  setup() {
    const {t: $t} = useI18n()
    const name = ref('')
    const origin = ref('')
    const additional = ref('')
    const selected = ref([]) as Ref<Credential[]>
    const route = useRoute();
    const fetchedRows = ref<Credential[]>([]);
    const $q = useQuasar();

    const credentialStore = useCredentialStore()

    function getSelectedString() {
      return selected.value.length === 0 ? '' : `${selected.value.length} record${selected.value.length > 1 ? 's' : ''} selected of ${fetchedRows.value.length}`

    }

    function getFetchedCredentials() {
      return getAllCredentials().then((response: any) => {
            fetchedRows.value = []
            for (let credential of response) {
              fetchedRows.value.push(
                  {
                    "ID": credential.id,
                    "DID": <string>credential.credentialDid,
                    "name": <string>credential.name
                  }
              )
            }
          }
      )
    }

    function loadCurrent() {
      const groupName = String(route.params.name)
      api.credential_groups.getCredentialGroup(groupName).then((response: any) => {
        name.value = <string>response.name
        origin.value = <string>response.origin
        additional.value = <string>response.additional
        let id = response.credentialString.split(",")
        for (let row of fetchedRows.value) {
          for (let i of id) {
            if (row.ID == i) {
              selected.value.push(row);
            }
          }
        }
      })
    }

    function deleteGroup() {
      const groupName = String(route.params.name)
      api.credential_groups.deleteCredentialGroup(groupName).then((response: any) => {
      })
      $q.notify({
        message: $t('admin_credential_group_management.edit.delete'),
        color: 'primary'
      })
      router.push("/admin/credentialgroups")
      credentialStore.updateCredentials()
    }

    function updateGroup() {
      const oldName = String(route.params.name)
      api.credential_groups.updateCredentialGroup(getSelectedIds(), name.value,
          origin.value, additional.value, oldName).then((response: any) => {
        if (response == "success") {
          $q.notify({
            message: name.value.concat($t('admin_credential_group_management.edit.success')),
            color: 'primary'
          })
          router.push("/admin/credentialgroups")
        } else {
          if (response == "fail credentialIDs exist") {
            $q.notify({
              message: $t('admin_credential_group_management.edit.alreadyExists'),
              color: 'primary'
            })
          } else {
            $q.notify({
              message: $t('admin_credential_group_management.edit.error'),
              color: 'primary'
            })
          }
        }
        credentialStore.updateCredentials()
      })
    }

    function getSelectedIds() {
      const result = new Set()
      selected.value.forEach((x) => result.add(x.ID))
      const asArray = Array.from(result).sort()
      let asString = asArray.join(', ')
      return asString
    }

    function addCredentialGroup() {
      api.credential_groups.addCredentialGroup(getSelectedIds(), name.value,
          origin.value, additional.value)

      name.value = ""
      origin.value = "";
      additional.value = "";
      selected.value = [];
      credentialStore.updateCredentials()
    }


    onMounted(async () => {
      await getFetchedCredentials();
      loadCurrent();
    })

    return {
      name, origin, additional, rows, columnPlan,
      selected, fetchedRows,
      getSelectedString, getSelectedIds,
      addCredentialGroup, deleteGroup, updateGroup,
      confirm: ref(false),
      text: ref(''),
      ph: ref(''),
      dense: ref(false)
    }
  }
}
</script>

<style scoped>

</style>

<template style="max-width: 100%; overflow: hidden">
  <div class="admin-navigation-bar">
    <AdminNavigationBar section="credentials"/>
  </div>

  <div class="outer-container">
    <div class="inner-container">
      <q-card style="background-color: var(--background)">
        <q-card-section class="add-components left-half">
          <div class="q-pa-md">
            <div class="q-gutter-y-md">

              <q-input filled v-model="text"
                       class="input"
                       color="white"
                       :placeholder="($t('admin_credential_group_management.edit.credentials')).concat(getSelectedIds())"
                       :dense="dense"
                       disable style="font-size: 40px; height: 110px; color:white; "/>

              <q-input filled v-model="name"
                       class="input"
                       label="Name:"
                       :dense="dense"
                       style="font-size: 30px; height: 110px;"/>

              <q-input filled v-model="origin"
                       class="input"
                       :label="$t('admin_credential_group_management.columnNames.origin')"
                       :dense="dense"
                       style="font-size: 30px; height: 110px;"/>

              <q-input
                  v-model="additional"
                  class="input"
                  filled
                  clearable
                  type="textarea"
                  color="primary"
                  :label="$t('admin_credential_group_management.edit.AdditionalText')"
                  style="font-size:30px; height:auto"
              />


            </div>
          </div>
        </q-card-section>
      </q-card>
      <div class="right-half">
        <q-table
            flat bordered
            :rows="fetchedRows"
            :rows-per-page-options="[0]"
            :virtual-scroll-sticky-size-start="48"
            class="my-sticky-virtscroll-table coloredTable"
            :columns="columnPlan"
            row-key="ID"
            :selected-rows-label="getSelectedString"
            selection="multiple"
            v-model:selected="selected"
            style="width: 100%; background-color: var(--background);"

        />
      </div>
      <q-dialog v-model="confirm" persistent>
        <q-card style="width: 700px">
          <q-card-section class="row items-center">
            <span class="q-ml-sm">{{ $t('admin_credential_group_management.confirmDialog.message') }}</span>
          </q-card-section>

          <q-card-actions align="right">
            <q-btn no-caps color="accent"
                   :label="$t('admin_credential_group_management.buttons.back')"
                   v-close-popup/>
            <q-btn no-caps color="accent"
                   :label="$t('admin_credential_group_management.buttons.delete')"
                   @click="deleteGroup"
                   v-close-popup/>
          </q-card-actions>
        </q-card>
      </q-dialog>
    </div>
    <div class="button-components">
      <q-btn to="/admin/credentialgroups" no-caps color="accent"
             :label="$t('admin_credential_group_management.buttons.back')"
             class="add-button"
             style="height:40px"/>

      <q-btn no-caps color="accent"
             :label="$t('admin_credential_group_management.buttons.delete')"
             class="add-button"
             @click="confirm = true"/>

      <q-btn no-caps color="primary"
             :label="$t('admin_credential_group_management.buttons.save')"
             class="add-button"
             @click="updateGroup"/>
    </div>
  </div>
</template>


<style scoped>
.admin-navigation-bar {
  position: fixed;
  left: 0;
  width: 100%;
  z-index: 999;
}

.outer-container {
  display: flex;
  flex-direction: column;
  height: 95vh;
  overflow-x: hidden;
  margin-top: 50px;
}

.inner-container {
  grid-row: 1;
  display: grid;
  grid-template-columns: 1fr 2% 1fr;
  margin-top: 10px;
}

.left-half {
  grid-column: 1;
  width: 100%;
}

.right-half {
  grid-column: 3;
  width: 100%;
  display: flex;
  justify-content: right;
}

.input {
  width: 100%;
}

.add-components {
  width: 100%;
  float: left;
}

.button-components {
  grid-row: 2;
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  margin-bottom: 20px;
}

.add-button {
  width: 80px;
}

@media (max-width: 875px) {
  .inner-container {
    grid-template-columns: 1fr;
    row-gap: 10px;
  }

  .right-half {
    grid-column: 1;
    grid-row: 2;
  }
}

@media (max-width: 320px) {
  .button-components {
    flex-direction: column;
  }

  .add-button {
    width: 100%;
    margin-top: 10px;
  }
}
</style>
