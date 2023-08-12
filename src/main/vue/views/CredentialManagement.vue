<script lang="ts">
import {onMounted, ref, watchEffect} from "vue"
import {QTable} from "quasar";
import {useI18n} from "vue-i18n"
import router from "@/main/vue/router";
import {getAllCredentials} from "@/main/vue/api/credentials";
import AdminNavigationBar from "@/main/vue/components/AdminNavigationBar.vue";

interface Credential {
  ID: number
  DID: string
  name: string
  origin: string
  AdditionalText: string
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

let columnPlan = ref<Column[]>([
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
  {name: 'DID', label: 'DID', align: 'left', field: 'DID', sortable: true, filter: true},
  {name: 'origin', label: 'Origin', align: 'left', field: 'origin', sortable: true, filter: true},
  {
    name: 'AdditionalText',
    label: 'Additional Text',
    align: 'left',
    field: 'AdditionalText',
    sortable: true,
    filter: true
  }
])

const rows = [
  {
    MappingID: 1,
    name: 'T-Member',
    origin: 'at Telekom',
    AdditionalText: 'A Telekom Member...'
  },
  {
    MappingID: 2,
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
    const fetchedRows = ref<Credential[]>([]);

    const selectedSearch = ref('')
    const search = ref('')

    function translateColumns() {
      // Translate table column names
      columnPlan.value = columnPlan.value.map(column => ({
        ...column,
        label: t(`admin_credential_management.columnNames.${column.name}`)
      }))
    }

    function editCheckList(id: string) {
      const Url = "/admin/edit/" + id
      router.push(Url)
    }

    function getFetchedCredentials() {
      getAllCredentials().then((response: any) => {
            fetchedRows.value = []
            for (let credential of response) {
              fetchedRows.value.push(
                  {
                    "ID": credential.id,
                    "DID": <string>credential.credentialDid,
                    "name": <string>credential.name,
                    "origin": <string>credential.origin,
                    "AdditionalText": <string>credential.additional
                  }
              )
            }
          }
      )
    }

      function editCredential(id: string) {
        const Url = "/admin/credentials/edit/" + id
          router.push(Url)
      }


    onMounted(() => {
      // fetch CredentialGroups and translate column names when site is loaded
      translateColumns();
      getFetchedCredentials();
    })


    watchEffect(() => {
      // translate column names when site is refreshed
      translateColumns()
    })

    return {
      rows, columnPlan,
      fetchedRows,
      translateColumns,
      editCheckList,
      pagination: ref({
        rowsPerPage: 0
      }),
        editCredential
    }

    }
}
</script>

<style scoped>

</style>

<template style=" max-width: 100%; overflow: hidden">
  <div class="admin-navigation-bar">
    <AdminNavigationBar section="credentials"/>
  </div>

  <div class="wrapper">
    <div class="center-screen">
      <div class="text-h4 header">{{ $t(`admin_credential_management.title`) }}</div>

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
            <q-tr :props="props" style="width: 100%">
              <q-th v-for="col in props.cols"
                    :key="col.name"
                    :props="props"
                    style="color: white; font-weight: bold; font-size: 15px">{{ col.label }}
              </q-th>
                <q-th>

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
                <q-btn size="sm" flat dense @click="editCredential(props.row.ID)" icon="edit">
                  <q-tooltip>{{ $t("admin_credential_management.columnNames.editCredential") }}</q-tooltip>
                </q-btn>
              </q-td>
              <q-td auto-width>
                <q-btn size="sm" flat dense @click="editCheckList(props.row.ID)" icon="checklist_rtl">
                  <q-tooltip>{{ $t("admin_credential_management.columnNames.checklist") }}</q-tooltip>
                </q-btn>
              </q-td>
            </q-tr>
            <q-tr/>
          </template>
        </q-table>
      </div>
    </div>
    <div class="button-content">
      <q-btn to="/admin/credentialgroups" color="primary" class="fab" round>
        <q-icon name="checklist" size="md"/>
        <q-tooltip>{{ $t("admin_credential_group_management.title") }}</q-tooltip>
      </q-btn>
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

.fab {
  position: fixed;
  height: 3.75rem;
  width: 3.75rem;
  bottom: 3.75rem;
  z-index: 1000;
  left: 2.5%;
}

</style>

<style lang="sass">
.my-sticky-virtscroll-table
  /* height or max-height is important */
  max-height: 60vh
  .q-table__top,
  thead tr:first-child th /* bg color is important for th; just specify one */
        background-color: $primary
  thead tr th
    position: sticky
    z-index: 1
  thead tr:last-child th
    top: 48px
  thead tr:first-child th
    top: 0
  tbody
    scroll-margin-top: 48px


</style>