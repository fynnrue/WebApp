<script lang="ts">
import {onMounted, Ref, ref} from 'vue'
import {useQuasar} from "quasar"
import {useI18n} from "vue-i18n"
import api from "../api"
import router from "@/main/vue/router";
import {getAllCredentials} from "@/main/vue/api/credentials";
import AdminNavigationBar from "@/main/vue/components/AdminNavigationBar.vue";
import {useCredentialStore} from "@/main/vue/stores/credentialStore";

interface Credential {
    ID: number
    DID: string
    name: string
}


interface Column {
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
        name: 'desc',
        required: true,
        label: 'CredentialIDs',
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
        name: 'DRK'
    },
    {
        ID: 2,
        name: 'DRK2'
    }
]

export default {
  name: "CredentialGroupAdd",
  components: {AdminNavigationBar},

    setup() {
        const {t: $t} = useI18n()
        const name = ref('')
        const origin = ref('')
        const additional = ref('')
        const selected = ref([]) as Ref<Credential[]>
        const credentialString = "Credential IDs: "
        const $q = useQuasar()
        const fetchedRows = ref<Credential[]>([]);

        const credentialStore = useCredentialStore()

        function getSelectedString() {
            return selected.value.length === 0 ? '' : `${selected.value.length} record${selected.value.length > 1 ? 's' : ''} selected of ${rows.length}`

        }

    function getSelectedIds() {
      const result = new Set()
      selected.value.forEach((x) => result.add(x.ID))
      const asArray = Array.from(result).sort()
      let asString = asArray.join(', ')
      return asString
    }

    async function addCredentialGroup() {
      if (name.value == "") {
        $q.notify({
          message: $t('admin_credential_group_management.add.emptyName'),
          color: 'negative'
        })
      } else if (getSelectedIds() == "") {
        $q.notify({
          message: $t('admin_credential_group_management.add.emptyId'),
          color: 'negative'
        })
      } else {
        api.credential_groups.addCredentialGroup(getSelectedIds(), name.value,
            origin.value, additional.value
        ).then((response: any) => {
          credentialStore.updateCredentials()
          if (response == "success") {
            $q.notify({
              message: name.value.concat($t('admin_credential_group_management.add.success')),
              color: 'primary'
            })
            router.push("/admin/credentialgroups")
          } else {
            if (response == "fail name exists") {
              $q.notify({
                message: $t('admin_credential_group_management.add.nameExists'),
                color: 'negative'
              })
            } else if (response == "fail credentialIDs exist") {
              $q.notify({
                message: $t('admin_credential_group_management.add.idExists'),
                color: 'primary'
              })
            } else {
              $q.notify({
                message: $t('admin_credential_group_management.add.error'),
                color: 'primary'
              })
            }
          }
        })
      }
    }

    function getFetchedCredentials() {
      getAllCredentials().then((response: any) => {
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

    function getCredentialString() {
      return credentialString;
    }

    onMounted(() => {
      getFetchedCredentials();
    })


    return {
      name, origin, additional, rows, columnPlan, selected, credentialString, fetchedRows,
      getSelectedString, getSelectedIds, addCredentialGroup,
      text: ref(''),
      ph: ref(''),
      dense: ref(false)
    }
  }
}
</script>

<style scoped>

</style>

<template style="max-width: 100%; overflow-y: hidden">
  <div class="admin-navigation-bar">
    <AdminNavigationBar section="credentials"/>
  </div>

  <div class="outer-container">
    <div class="inner-container">
      <q-card style="background-color: var(--background)">
        <q-card-section class="add-components left-half">
          <div class="q-gutter-y-md column">

            <q-input filled v-model="text"
                     class="input"
                     color="white"
                     :placeholder="credentialString.concat(getSelectedIds())"
                     :dense="dense"
                     disable style="font-size: 40px; height: 110px; color:white"/>

            <q-input filled v-model="name"
                     class="input"
                     label="Name:"
                     :dense="dense"
                     style="font-size: 30px; height: 110px;"/>


            <q-input filled v-model="origin"
                     class="input"
                     :label="$t('admin_credential_management.columnNames.origin')"
                     :dense="dense"
                     style="font-size: 30px; height: 110px;"/>

            <q-input
                v-model="additional"
                class="input"
                filled
                clearable
                type="textarea"
                color="primary"
                :label="$t('credential_groups.additional_text')"
                style="font-size:30px"
            />


          </div>
        </q-card-section>
      </q-card>
      <div class="right-half">
        <q-table
            flat bordered
            :rows="fetchedRows"
            :rows-per-page-options="[0]"
            :virtual-scroll-sticky-size-start="48"
            class="my-sticky-virtscroll-table"
            :columns="columnPlan"
            row-key="ID"
            :selected-rows-label="getSelectedString"
            selection="multiple"
            v-model:selected="selected"
            virtual-scroll
            style="width: 100%; background-color: var(--background);"
        />
      </div>
    </div>
    <div class="button-components">
      <q-btn to="/admin/credentialgroups" no-caps color="accent"
             :label="$t('credential_groups.back')"
             class="add-button"
             style="height:40px"/>

      <q-btn no-caps color="primary"
             :label="$t('credential_groups.add_credential_group')"
             class="add-button"
             @click="addCredentialGroup"/>
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
}

.inner-container {
  grid-row: 1;
  display: grid;
  grid-template-columns: 1fr 2% 1fr;
  margin-top: 60px;
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

@media (max-width: 875px) {
  .inner-container {
    grid-template-columns: 1fr;
  }

  .right-half {
    grid-column: 1;
    grid-row: 2;
  }
}
</style>
