<script lang="ts">
import {onMounted, ref, watch, watchEffect, computed, provide, getCurrentInstance} from "vue"
import {QTable, useQuasar, QDialog} from "quasar";
import {useI18n} from "vue-i18n"
import api from "../api"
import TitleBarButton from "@/main/vue/components/TitleBarButton.vue"
import ModifyUsersDialog from "@/main/vue/components/ModifyUsersDialog.vue";
import AdminNavigationBar from "@/main/vue/components/AdminNavigationBar.vue";
import {useRouter} from "vue-router";
import {userStore} from "@/main/vue/stores/userStore";

interface User {
    forename: string
    surname: string
    email: string
    permissions: string
    activated: string
    selected: boolean
}

interface Column {
  align?: "left" | "right" | "center";
  field: keyof User | ((row: User) => any);
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

export default {
  name: "UserManagement",
  components: {AdminNavigationBar, TitleBarButton, ModifyUsersDialog, QTable, QDialog},

  setup() {
    provide('parent', getCurrentInstance())

    const $q = useQuasar()
    const {t} = useI18n()
    const router = useRouter()
    const store = userStore()

    const loading = $q.loading
    let numLoading = 0;

    const selectedPermission = ref('')
    const selectedActivation = ref('')
    const selectedSearch = ref('')
    const search = ref('')
    const fetchedRows = ref<User[]>([])
    const selectedRows = ref<User[]>([])

    const showModifyDialog = ref(false)
    const modifyId = ref(0)

    // Columns of user information table
    const columnPlan = ref<Column[]>([
      {name: "forename", label: 'Vorname', align: 'left', field: 'forename', sortable: true, filter: true},
      {name: "surname", label: 'Nachname', align: 'left', field: 'surname', sortable: true, filter: true},
      {name: "email", label: 'E-Mail', align: 'left', field: 'email', sortable: true, required: true, filter: true},
      {name: "permissions", label: 'Permissions', align: 'left', field: 'permissions', sortable: true},
      {name: "activated", label: 'Activated', align: 'left', field: 'activated', sortable: true},
    ])

    // Checkbox to select all rows in table
    const selectAllRowsCheckbox = computed({
      get: () => selectedRows.value.length === fetchedRows.value.length,
      set: (value) => selectAllRows(value),
    });

    // Translation of the filter selection options
    const permissionsArray = [
      'admin', 'issuer', 'planner', 'none'
    ]
    const permissionOptions = permissionsArray.map(permissionStat => ({
      label: t(`admin_user_management.permissionsArray.${permissionStat}`),
      value: permissionStat
    }))
    const activationArray = [
      'yes', 'no'
    ]
    const activationOptions = activationArray.map(activatedStat => ({
      label: t(`admin_user_management.activationArray.${activatedStat}`),
      value: activatedStat
    }))
    const searchArray = [
      'forename', 'surname', 'email'
    ]
    const searchOptions = searchArray.map(searchStat => ({
      label: t(`admin_user_management.searchArray.${searchStat}`),
      value: searchStat
    }))


    // Get users from Backend that satisfy filters
    function getFilteredUsers() {
      showLoading()
      api.admin_users.fetchFilteredUsers(
          selectedPermission.value, selectedActivation.value, selectedSearch.value, search.value).then((response: any) => {
        fetchedRows.value = []

        for (let user of response) {
          let roles = ""
          let activated = ""

          if (user.activated) {
            activated = "yes"
          } else {
            activated = "no"
          }

          for (let role of user.roles) {
            switch (role) {
              case("ROLE_ADMIN"):
                role = "Admin"
                break
              case("ROLE_ISSUER"):
                role = "Issuer"
                break
              case("ROLE_EDITOR"):
                role = "Planner"
                break
            }

            if (roles == "") {
              roles = role
            } else {
              roles += ", " + role
            }
          }

          fetchedRows.value.push(
              {
                "selected": false,
                "forename": <string>user.forename,
                "surname": <string>user.surname,
                "email": <string>user.email,
                "permissions": <string>roles,
                "activated": <string>activated
              }
          )
        }
        hideLoading()
      }).catch((error: any) => {
        hideLoading()
      })
    }

    // Translates column names and content of array
    function translateTable() {
      // Translate table column names
      columnPlan.value = columnPlan.value.map(column => ({
        ...column,
        label: t(`admin_user_management.columnNames.${column.name}`)
      }))

      // Translate table content
      fetchedRows.value = fetchedRows.value.map(row => {
        let perms = row.permissions.split(", ")
        perms = perms.map(perm => {
          if (perm == "Admin") {
            perm = t('admin_user_management.table.admin')
            return perm
          } else if (perm == "Issuer" || perm == "Herausgeber") {
            perm = t('admin_user_management.table.issuer')
            return perm
          } else if (perm == "Planner" || perm == "Bearbeiter") {
            perm = t('admin_user_management.table.planner')
            return perm
          } else {
            return perm
          }
        })

        if (row.activated.toLowerCase() == "yes" || row.activated.toLowerCase() == "ja") {
          row.activated = t('admin_user_management.table.yes')
        } else if (row.activated.toLowerCase() == "no" || row.activated.toLowerCase() == "nein") {
          row.activated = t('admin_user_management.table.no')
        }

        row.permissions = perms.join(", ")
        return {...row}
      })
    }

    // Function to select all rows in the table
    function selectAllRows(selected: boolean) {
      if (selected) {
        selectedRows.value = fetchedRows.value.map(row => {
          row.selected = true;
          return row;
        });
      } else {
        selectedRows.value = [];
        fetchedRows.value = fetchedRows.value.map(row => {
          row.selected = false;
          return row;
        })
      }
    }

    // Send backend call to activate selected users
    function activateSelected() {
      if (selectedRows.value.length === 0) {
        return
      }

      let emails: string = ""

      for (let row of selectedRows.value) {
        if (row.email == store.currentUser?.email) {
          $q.notify({
            type: 'negative',
            message: t('admin_user_management.selected.errorActivate'),
            caption: t('admin_user_management.selected.sameUser')
          })
          continue;
        }
        emails += ", " + row.email
      }
      emails = emails.substring(2)

      showLoading()
      api.admin_users.activateUsers(
          emails).then((response: any) => {
        getFilteredUsers()

        $q.notify({
          message: t('admin_user_management.successful.activation'),
          color: 'primary'
        })

        hideLoading()
      }).catch((error: any) => {
        console.error("Error activating users:", error);
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorActivate'),
          caption: t('admin_user_management.selected.somethingWrong')
        })
        getFilteredUsers()
        hideLoading()
      });
    }

    // Send backend call to deactivate selected users
    function deactivateSelected() {
      if (selectedRows.value.length === 0) {
        return
      }

      let emails: string = ""

      for (let row of selectedRows.value) {
        if (row.email == store.currentUser?.email) {
          $q.notify({
            type: 'negative',
            message: t('admin_user_management.selected.errorDeactivate'),
            caption: t('admin_user_management.selected.sameUser')
          })
          continue;
        }
        emails += ", " + row.email
      }
      emails = emails.substring(2)

      showLoading()
      api.admin_users.deactivateUsers(
          emails).then((response: any) => {
        getFilteredUsers()

        $q.notify({
          message: t('admin_user_management.successful.deactivation'),
          color: 'primary'
        })

        hideLoading()
      }).catch((error: any) => {
        console.error("Error deactivating users:", error);
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorDeactivate'),
          caption: t('admin_user_management.selected.somethingWrong')
        })
        getFilteredUsers()
        hideLoading()
      });
    }

    // Send backend call to delete selected users
    function deleteSelected() {
      if (selectedRows.value.length === 0) {
        return
      }

      let emails: string = ""

      for (let row of selectedRows.value) {
        if (row.email == store.currentUser?.email) {
          $q.notify({
            type: 'negative',
            message: t('admin_user_management.selected.errorDelete'),
            caption: t('admin_user_management.selected.sameUser')
          })
          continue;
        }
        emails += ", " + row.email
      }
      emails = emails.substring(2)

      showLoading()
      api.admin_users.deleteUsers(
          emails).then((response: any) => {
        getFilteredUsers()

        $q.notify({
          message: t('admin_user_management.successful.deletion'),
          color: 'primary'
        })

        hideLoading()
      }).catch((error: any) => {
        console.error("Error removing users:", error);
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorDelete'),
          caption: t('admin_user_management.selected.somethingWrong')
        })
        getFilteredUsers()
        hideLoading()
      });
    }

    function openModifyDialog(id: number) {
      modifyId.value = id
      showModifyDialog.value = true
    }

    function showLoading() {
      loading.show({
        message: t('admin_user_editor.loading')
      });
      numLoading++;
    }

    function hideLoading() {
      numLoading--;
      if (numLoading === 0) {
        loading.hide();
      }
    }

    // direct admin to user edit page
    function editUser(email: string) {
      const userUrl = "/admin/users/" + email
      router.push(userUrl)
    }

    onMounted(() => {
      // when site newly loads, all users get fetched and table gets translated
      getFilteredUsers()
      translateTable()
    })

    watchEffect(() => {
      // when site refreshes, table gets translated
      translateTable()
    })

    watch(
        // Updates selected users Array with selected users
        fetchedRows,
        (newRows) => {
          selectedRows.value = newRows.filter((row) => row.selected);
        },
        {deep: true}
    )

    return {
      // Table
      columnPlan,
      pagination: ref({
        rowsPerPage: 0
      }),
      // Filters
      permissionOptions,
      activationOptions,
      searchOptions,
      selectedPermission,
      selectedActivation,
      selectedSearch,
      search,
      getFilteredUsers,
      // Table content
      fetchedRows,
      selectedRows,
      selectAllRowsCheckbox,
      selectAllRows,
      // Backend calls and corresponding
      activateSelected,
      deactivateSelected,
      deleteSelected,
      openModifyDialog,
      showModifyDialog,
      editUser,
      modifyId
    }
  }
}

</script>

<template style="max-width: 100%; overflow: hidden">
  <!-- Admin navigation bar -->
  <div class="admin-navigation-bar">
    <AdminNavigationBar section="users"/>
  </div>


  <div class="wrapper" style="overflow-x: hidden; max-height: 95%; margin: 3rem -20px -20px;">
    <div class="center-screen">
      <!-- Filters above the table -->
      <div class="full-width q-mt-xl filters" style="max-width: 99%">

        <!-- Permissions -->
        <div class="permissions-filter">
          <div class="text-h4 text-header">{{ $t("admin_user_management.permissions") }}:</div>
          <q-select v-model="selectedPermission"
                    :options="permissionOptions"
                    bg-color="primary"
                    class="filter-element"
                    clearable
                    color="white"
                    dense
                    dark
                    filled
                    emit-value
                    map-options
                    label="Selection"
                    label-color="white"
                    scrollable="false"
                    stack-label
                    style="margin-right: 1rem"/>
        </div>

        <!-- Activated -->
        <div class="activated-filter filter-div">
          <div class="text-h4 text-header">{{ $t("admin_user_management.activationStatus") }}:</div>
          <q-select v-model="selectedActivation"
                    :options="activationOptions"
                    bg-color="primary"
                    class="filter-element"
                    clearable
                    color="white"
                    dense
                    dark
                    filled
                    emit-value
                    map-options
                    label="Selection"
                    label-color="white"
                    scrollable="false"
                    stack-label
                    style="margin-right: 1rem"/>
        </div>

        <!-- Search -->
        <div class="search-filters filter-div">
          <div class="text-h4 text-header filter-element-search-header">{{ $t("admin_user_management.search") }}:</div>
          <q-input dense v-model="search" class="filter-element-search"/>
          <q-select v-model="selectedSearch"
                    :options="searchOptions"
                    bg-color="primary"
                    class="filter-element-search-select"
                    clearable
                    color="white"
                    dense
                    dark
                    filled
                    emit-value
                    map-options
                    label="Selection"
                    label-color="white"
                    scrollable="false"
                    stack-label
                    style="margin-right: 1rem"/>

          <!-- Apply -->
          <q-btn no-caps color="primary" :label="$t('admin_user_management.apply')" class="filter-apply-button"
                 @click="getFilteredUsers"/>
        </div>
      </div>

      <!-- Table -->
      <div style="max-height: 70%">
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
            bordered
            virtual-scroll
            style="background-color: var(--background)">

          <template v-slot:header="props">
            <q-tr :props="props">
              <q-th style="width: 5%">
                <q-checkbox
                    v-model="selectAllRowsCheckbox"
                    color="primary"
                    label=""
                    style="text-align: center"
                />
              </q-th>
              <q-th v-for="col in props.cols" :key="col.name" :props="props"
                    style="color: white; font-weight: bold; font-size: 15px; width: 18%">
                {{ col.label }}
              </q-th>
              <q-th style="width: 5%;"></q-th>
            </q-tr>
          </template>


          <template v-slot:body="props">
            <q-tr :props="props">
              <q-td>
                <q-checkbox v-model="props.row.selected" color="primary" label=""/>
              </q-td>
              <q-td style="text-align: left">{{ props.row.forename }}</q-td>
              <q-td style="text-align: left">{{ props.row.surname }}</q-td>
              <q-td style="text-align: left">{{ props.row.email }}</q-td>
              <q-td style="text-align: left">{{ props.row.permissions }}</q-td>
              <q-td style="text-align: left">{{ props.row.activated }}</q-td>
              <q-td auto-width>
                <q-btn size="sm" flat dense @click="editUser(props.row.email)" icon="edit"/>
              </q-td>
            </q-tr>
          </template>


        </q-table>
      </div>
      <!-- Change user status buttons -->
      <div class="buttons">
        <q-btn no-caps color="accent" :label="$t('admin_user_management.selected.delete')" class="select-button"
               @click="openModifyDialog(3)"/>
        <q-btn no-caps color="accent" :label="$t('admin_user_management.selected.deactivate')" class="select-button"
               @click="openModifyDialog(2)"/>
        <q-btn no-caps color="primary" :label="$t('admin_user_management.selected.activate')" class="select-button"
               @click="openModifyDialog(1)"/>
      </div>

      <!-- Delete Users Dialog -->
      <ModifyUsersDialog v-model="showModifyDialog" :dialog-visible="showModifyDialog"
                         @update:dialog-visible="showModifyDialog = $event" :modifyId="modifyId"
                         @activate="activateSelected" @deactivate="deactivateSelected" @delete="deleteSelected"
      />
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

.center-screen {
  justify-content: center;
  align-items: center;
  text-align: center;
  margin-top: -2rem;
  margin-left: auto;
  margin-right: auto;
  width: 95%;
  height: 95%;
}

.text-header {
  text-align: left;
}

.filters {
  display: grid;
  grid-template-columns: 29rem 23rem 1fr;
}

.permissions-filter {
  grid-column: 1;
  display: flex;
}

.activated-filter {
  grid-column: 2;
  display: flex;
}

.search-filters {
  grid-column: 3;
  display: grid;
  grid-template-columns: 7.5rem 15.5rem 12rem 10%;
}

.filter-element {
  position: relative;
  height: 36px;
  width: auto;
  min-width: 180px;
  max-width: 500px;
}

.filter-element-search-header {
  grid-column: 1;
}

.filter-element-search {
  height: 36px;
  width: 15rem;
  font-size: 1.8rem;
  grid-column: 2;
}

.filter-element-search-select {
  position: relative;
  height: 36px;
  width: 10rem;
}

.filter-apply-button {
  position: relative;
  height: 40px;
  width: 96px;
}

.filter-div {
  margin-bottom: 1rem;
}

.text-header {
  margin-bottom: 1rem;
  padding-right: 10px;
}

.buttons {
  display: flex;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
  margin-top: 10px;
  gap: 1rem;
}

.select-button {
  width: 12rem;
}

@media (max-width: 1569px) {
  .filters {
    grid-template-columns: 28rem 1fr 0;
  }

  .search-filters {
    grid-column: 1;
    grid-row: 2;
    margin-top: -1rem;
  }
}

@media (max-width: 920px) {
  .activated-filter {
    grid-column: 1;
    grid-row: 2;
  }

  .search-filters {
    grid-row: 3;
  }
}

@media (max-width: 720px) {
  .search-filters {
    grid-template-columns: 7rem 1fr;
  }

  .filter-element-search-select {
    grid-row: 2;
  }

  .filter-apply-button {
    grid-column: 3;
  }

  .filter-apply-button {
    grid-row: 2;
    grid-column: 2;
    margin-left: 4rem;
  }

  .buttons{
    margin-bottom: 30px;
    flex-wrap: wrap;
  }
}

@media (max-width: 520px) {
  .permissions-filter {
    flex-direction: column;
    align-items: flex-start;
  }

  .activated-filter {
    flex-direction: column;
    align-items: flex-start;
    margin-top: 1rem;
  }

  .search-filters {
    margin-top: 0;
  }
}

@media (max-width: 420px) {
  .search-filters {
    grid-template-columns: 100%;
    justify-items: left;
  }

  .filter-element-search {
    grid-column: 1;
    grid-row: 2;
    margin-top: -1rem;
  }

  .filter-element-search-select {
    grid-column: 1;
    grid-row: 3;
    margin-top: 1rem;
  }

  .filter-apply-button {
    grid-column: 1;
    grid-row: 4;
    margin-top: 1rem;
    margin-left: 0;
  }
}
</style>