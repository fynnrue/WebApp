<script lang="ts">
import {onMounted, ref, watchEffect} from "vue"
import {QTable} from "quasar";
import {useI18n} from "vue-i18n"
import api from "@/main/vue/api";
import router from "@/main/vue/router";
import AdminNavigationBar from "@/main/vue/components/AdminNavigationBar.vue";

export interface Credential {
  credentialString: string
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

let columnPlan = ref<Column[]>([
  {
    name: 'credentialString',
    label: 'Credential-String',
    align: 'left',
    field: 'credentialString',
    sortable: true,
    filter: true
  },
  {name: 'name', label: 'Name', align: 'left', field: 'name', sortable: true, filter: true},
  {name: 'origin', label: 'Origin', align: 'left', field: 'origin', sortable: true, filter: true},
  {
    name: 'AdditionalText',
    label: 'Aditional Text',
    align: 'left',
    field: 'AdditionalText',
    sortable: true,
    filter: true
  }
])

const rows = [
  {
    CredentialIDs: "1, 2, 3",
    name: 'T-Member',
    origin: 'at Telekom',
    AdditionalText: 'A Telekom Member...'
  },
  {
    CredentialIDs: "2, 3, 5",
    name: 'U-Member',
    origin: 'at University',
    AdditionalText: 'A University Member...'
  }
]


export default {
  name: "CredentialManagement",
  components: {AdminNavigationBar, QTable},

  setup() {
    const {t} = useI18n()
    const filteredRows = ref(rows)

    const fetchedRows = ref<Credential[]>([])
    const selectedSearch = ref('')
    const search = ref('')



    function translateColumns() {
      // Translate table column names
      columnPlan.value = columnPlan.value.map(column => ({
        ...column,
        label: t(`admin_credential_group_management.columnNames.${column.name}`)
      }))
    }

    function getFetchedCredentialGroups() {
      api.credential_groups.getCredentialGroups().then((response: any) => {
            fetchedRows.value = []
            for (let credentialGroup of response) {
              fetchedRows.value.push(
                  {
                    "credentialString": <string>credentialGroup.credentialString,
                    "name": <string>credentialGroup.name,
                    "origin": <string>credentialGroup.origin,
                    "AdditionalText": <string>credentialGroup.additional
                  }
              )
            }
          }
      )
    }

    function editCredentialGroup(name: string) {
      const Url = "/admin/credentialgroups/edit/" + name
      router.push(Url)
    }

    onMounted(() => {
      // fetch CredentialGroups and translate column names when site is loaded
      getFetchedCredentialGroups()
      translateColumns();
    })


    watchEffect(() => {
      // translate column names when site is refreshed
      translateColumns()
    })

    return {
      rows, columnPlan,
      filteredRows,
      fetchedRows,
      translateColumns,
      editCredentialGroup,
      pagination: ref({
        rowsPerPage: 0
      })
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

  <div class="wrapper">
    <div class="center-screen">
      <div class="text-h4 header"> {{ $t(`admin_credential_group_management.title`) }}</div>

      <!-- Table -->
      <div>
        <q-table
            v-model:pagination="pagination"
            :columns="columnPlan"
            :rows="fetchedRows"
            :rows-per-page-options="[0]"
            :virtual-scroll-sticky-size-start="48"
            class="my-sticky-virtscroll-table"
            flat
            row-key="email"
            selection="multiple"
            square
            virtual-scroll
            style="background-color: var(--background);">
          <template v-slot:header="props">
            <q-tr :props="props">
              <q-th v-for="col in props.cols"
                    :key="col.name"
                    :props="props"
                    style="color: white; font-weight: bold; font-size: 15px">{{ col.label }}
              </q-th>
              <q-th/>
            </q-tr>
          </template>

          <template v-slot:body="props">
            <q-tr :props="props">
              <q-td
                  v-for="col in props.cols"
                  :key="col.name"
                  :props="props">
                {{ col.value }}
              </q-td>
              <q-td auto-width>
                <q-btn size="sm" flat dense @click="editCredentialGroup(props.row.name)" icon="edit">
                    <q-tooltip>{{ $t("admin_credential_group_management.edit.button") }}</q-tooltip>
                </q-btn>
              </q-td>
            </q-tr>
            <q-tr/>
          </template>
        </q-table>
      </div>
    </div>

    <q-btn to="/admin/credentials" color="primary" class="fab left-button" round>
      <q-icon name="format_list_numbered" size="md"/>
      <q-tooltip>{{ $t("admin_credential_group_management.credentials") }}</q-tooltip>
    </q-btn>
    <q-btn to="/admin/credentialgroups/add" color="primary" class="fab right-button" round>
      <q-icon name="add" size="md"/>
      <q-tooltip>{{ $t("admin_credential_group_management.buttons.add") }}</q-tooltip>
    </q-btn>
  </div>
</template>

<style scoped>
.admin-navigation-bar {
  position: fixed;
  left: 0;
  width: 100%;
  z-index: 999;
}

.wrapper {
  overflow-x: hidden;
  max-height: 95%;
  margin: 0 -20px -20px;
}

.center-screen {
  justify-content: center;
  align-items: center;
  text-align: center;
  margin-left: auto;
  margin-right: auto;
  width: 95%;
}

.header {
  text-align: left;
  margin: 4rem auto 1rem;
}

.left-button {
  left: 2.5%;
}

.right-button {
  right: 2.5%;
}

.fab {
  position: fixed;
  height: 3.75rem;
  width: 3.75rem;
  bottom: 3.75rem;
  z-index: 1000;
}
</style>
