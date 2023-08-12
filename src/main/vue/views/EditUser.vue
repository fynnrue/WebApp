<script lang="ts">
import AdminNavigationBar from "@/main/vue/components/AdminNavigationBar.vue";
import {onMounted, ref, watch, watchEffect} from "vue";
import {useI18n} from "vue-i18n";
import api from "@/main/vue/api";
import {useRoute} from "vue-router";
import router from "@/main/vue/router";
import {useQuasar} from "quasar";
import {Role, userStore} from "@/main/vue/stores/userStore";
import ModifyUsersDialog from "@/main/vue/components/ModifyUsersDialog.vue";

interface Credential {
  id: number,
  credential: string
}

interface Permission {
  id: number,
  role: string
}

interface TableColumn {
  name: string;
  label: string;
  field: string | ((row: any) => any);
  required?: boolean;
  align?: "left" | "right" | "center";
  sortable?: boolean;
  sort?: (a: any, b: any, rowA: any, rowB: any) => number;
  sortOrder?: "ad" | "da";
  format?: (val: any, row: any) => any;
  style?: string | ((row: any) => string);
  classes?: string | ((row: any) => string);
}

export default {
  components: {ModifyUsersDialog, AdminNavigationBar },

  setup() {
    const { t } = useI18n();
    const $q = useQuasar()
    const route = useRoute();
    const store = userStore();

    const qloading = $q.loading
    let numLoading = 0;
    let loading = false;

    const showModifyDialog = ref(false)
    const modifyId = ref(0)

    const selectedPermissions = ref([] as {id: number, role: string;}[]);
    const selectedCredentials = ref([] as {id: number, credential: string;}[]);
    const showIssuerMenu = ref(false);
    const credentialSearch = ref("");

    const email: string = route.params.email.toString();
    const name = ref("");
    const status = ref("");
    let statusCode: number = 0;

    const permissionsTableColumns: TableColumn[] = [
      {
        name: "Role",
        label: "Role",
        field: "role",
        align: "left"
      },
    ];

    const permissionsTableData = ref<Permission[]>([
      { id: 1, role: t('admin_user_editor.permissionsArray.admin') },
      { id: 2, role: t('admin_user_editor.permissionsArray.issuer') },
      { id: 3, role: t('admin_user_editor.permissionsArray.planner') },
    ]).value;

    const credentialsTableColumns: TableColumn[] = [
      {
        name: "Credential",
        label: "Credential",
        field: "credential",
        align: "left"
      },
    ];

    let credentialsTableData: Credential[] = [];
    const credentialsTableDataFiltered = ref(credentialsTableData);

    // Translation functions
    function translatePermissionsTable() {
      permissionsTableData[0].role = t('admin_user_editor.permissionsArray.admin');
      permissionsTableData[1].role = t('admin_user_editor.permissionsArray.issuer');
      permissionsTableData[2].role = t('admin_user_editor.permissionsArray.planner');
    }

    function translateStatus() {
      if (statusCode === 1) {
        status.value = t('admin_user_editor.status.active');
      } else {
        status.value = t('admin_user_editor.status.deactivated');
      }
    }

    function showLoading() {
      qloading.show({
        message: t('admin_user_editor.loading')
      });
      numLoading++;
    }

    function hideLoading() {
      numLoading--;
      if (numLoading === 0) {
        qloading.hide();
      }
      if (loading) {
        loading = false;
        $q.notify({
          message: t("admin_user_editor.successful"),
          color: 'primary'
        });
      }
    }

    // User information and role functions

    function loadUserInformation() {
      getUserInformation();
      getRoles();
      refreshCredentials();
    }

    function getUserInformation() {
      showLoading();
      api.admin_users.fetchUserInformation(email).then((response: any) => {
        name.value = response.forename + " " + response.surname;

        if (response.activated) {
          statusCode = 1;
        } else {
          statusCode = 0;
        }

        translateStatus();
        hideLoading();
      }).catch((error: any) => {
        hideLoading();
      });
    }

    function getRoles() {
      showLoading();
      api.admin_users.fetchUserRoles(email).then((response: any) => {
        selectedPermissions.value = [];

        if (response.includes('ROLE_ADMIN')) {
          selectedPermissions.value.push(permissionsTableData[0]);
        }
        if (response.includes('ROLE_ISSUER')) {
          selectedPermissions.value.push(permissionsTableData[1]);
        }
        if (response.includes('ROLE_EDITOR')) {
          selectedPermissions.value.push(permissionsTableData[2]);
        }
        hideLoading();
      }).catch((error: any) => {
        hideLoading();
      });
    }

    function setRoles() {
      let roles = "";

      for (const permission of selectedPermissions.value) {
        roles += permission.id + ",";
      }
      roles = roles.slice(0, -1);

      if(!roles.includes("1") && email == store.currentUser?.email) {
        roles += ",1";

        $q.notify({
          type: 'negative',
          message: t('admin_user_editor.roles.error'),
          caption: t('admin_user_editor.roles.message')
        })
      }

      api.admin_users.setUserRoles(email, roles).then((response: any) => {
        if (email == store.currentUser?.email) {
          const user = store.currentUser
          let userRoles: Role[] = [];

          if (user) {
            for (const role of roles) {
              switch (role) {
                case "1":
                  userRoles.push(Role.Admin);
                  break;
                case "2":
                  userRoles.push(Role.Issuer);
                  break;
                case "3":
                  userRoles.push(Role.Editor);
                  break;
              }
            }

            if (user) {
              const newUser = {
                ...store.currentUser,
                roles: userRoles,
                forename: user.forename,
                surname: user.surname,
                email: user.email,
                username: user.username,
              };

              store.setUser(newUser);
            }
          }
        }
        getRoles();
      })
    }

    // Credential functions

    function refreshCredentials() {
      showLoading();
      api.admin_users.fetchAllCredentials().then((response: any) => {
        credentialsTableData = [];

        response.map((cred: any) => {
          let credential = {
            id: cred.id,
            credential: cred.name
          }

          credentialsTableData.push(credential);
        });

        credentialsTableDataFiltered.value = credentialsTableData;
        hideLoading();
      }).catch((error: any) => {
        hideLoading();
      }).finally(() => {
        getIssuableCredentials();
      });
    }

    function deselectCredential(credential: Credential) {
      selectedCredentials.value.map((selectedCredential: Credential, index) => {
        if (selectedCredential.id === credential.id) {
          selectedCredentials.value.splice(index, 1);
        }
      });
    }

    function getIssuableCredentials() {
      showLoading();
      api.admin_users.fetchIssuableCredentials(email).then((response: any) => {
        selectedCredentials.value = [];

        response.map((cred: any) => {
          let credential = {
            id: cred.id,
            credential: cred.name
          }

          selectedCredentials.value.push(credential);
        });
        hideLoading();
      }).catch((error: any) => {
        hideLoading();
      });
    }

    function save() {
      loading = true;

      let creds = "";
      for (let cred of selectedCredentials.value) {
        creds += cred.id + ",";
      }
      creds = creds.slice(0, -1);

      api.admin_users.setIssuerCredentials(email, creds).then((response: any) => {
        refreshCredentials();
        setRoles();
      });
    }

    // Account actions
    function activateUser() {
      if (email == store.currentUser?.email) {
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorActivate'),
          caption: t('admin_user_management.selected.sameUser')
        })
        return;
      }

      api.admin_users.activateUsers(
          email).then((response: any) => {
        loadUserInformation();

        $q.notify({
          message: t('admin_user_management.successful.activation'),
          color: 'primary'
        })
      }).catch((error: any) => {
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorActivate'),
          caption: t('admin_user_management.selected.somethingWrong')
        })
        loadUserInformation();
      });
    }

    function deactivateUser() {
      if (email == store.currentUser?.email) {
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorDeactivate'),
          caption: t('admin_user_management.selected.sameUser')
        })
        return;
      }

      api.admin_users.deactivateUsers(
          email).then((response: any) => {
        loadUserInformation();

        $q.notify({
          message: t('admin_user_management.successful.deactivation'),
          color: 'primary'
        })
      }).catch((error: any) => {
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorDeactivate'),
          caption: t('admin_user_management.selected.somethingWrong')
        })
        loadUserInformation();
      });
    }

    function deleteUser() {
      if (email == store.currentUser?.email) {
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorDelete'),
          caption: t('admin_user_management.selected.sameUser')
        })
        return;
      }

      api.admin_users.deleteUsers(
          email).then((response: any) => {
        $q.notify({
          message: t('admin_user_management.successful.deletion'),
          color: 'primary'
        })

        router.push("/admin/users")
      }).catch((error: any) => {
        $q.notify({
          type: 'negative',
          message: t('admin_user_management.selected.errorDelete'),
          caption: t('admin_user_management.selected.somethingWrong')
        })
        loadUserInformation();
      });
    }

    function openModifyDialog(id: number) {
      modifyId.value = id
      showModifyDialog.value = true
    }

    // Watchers

    watch(selectedPermissions, () => {
      let issuerChosen = false;

      selectedPermissions.value.map((selectedPermission) => {
        if (selectedPermission.id === 2) {
          issuerChosen = true;
        }
      });
      showIssuerMenu.value = issuerChosen;
    })

    watch(credentialSearch, () => {
      credentialsTableDataFiltered.value = [];

      credentialsTableData.map((credential: Credential) => {
        if (credentialSearch.value === "") {
          credentialsTableDataFiltered.value = credentialsTableData;
          return;
        }
        if (credential.credential.toLowerCase().includes(credentialSearch.value.toLowerCase())) {
          credentialsTableDataFiltered.value.push(credential);
        } else {
          if (credential.id == <number><unknown>credentialSearch.value) {
            credentialsTableDataFiltered.value.push(credential);
          }
        }
      });
    })

    watchEffect(() => {
      translatePermissionsTable();
      translateStatus();
    })

    onMounted(() => {
      loadUserInformation();
    });

    return {
      showIssuerMenu,
      name,
      email,
      status,

      // account actions
      activateUser,
      deactivateUser,
      deleteUser,
      openModifyDialog,
      showModifyDialog,
      modifyId,
      loading,

      // permissions table
      permissionsTableColumns,
      permissionsTableData,
      selectedPermissions,

      // credentials table
      credentialsTableColumns,
      credentialsTableDataFiltered,
      selectedCredentials,
      deselectCredential,
      refreshCredentials,
      credentialSearch,

      pagination: ref({
        rowsPerPage: 0
      }),

      // buttons
      save
    };
  },
};
</script>

<template>
  <!-- Admin navigation bar -->
  <div class="admin-navigation-bar">
    <AdminNavigationBar section="users"/>
  </div>

  <div class="split-layout-big">

    <div class="left-half-big">

      <div class="information-header">{{ $t('admin_user_editor.userInformation') }}</div>

      <div>{{ $t('admin_user_editor.user') }}: {{ name }}</div>
      <div>{{ $t('admin_user_editor.email') }}: {{ email }}</div>
      <div>{{ $t('admin_user_editor.status.status') }}: {{ status }}</div>

      <div class="permissions-table-div">
        <q-table
            class="permissions-table"
            :rows="permissionsTableData"
            :columns="permissionsTableColumns"
            row-key="id"
            :rows-per-page-options="[]"
            selection="multiple"
            v-model:selected="selectedPermissions"
            hide-bottom
            hide-header
            bordered
            style="background-color: var(--background)"
        >

          <template v-slot:top-left>
            <q-th key="permissions" flat class="table-header">
              {{ $t('admin_user_editor.permissions') }}
            </q-th>
          </template>

          <template v-slot:body-cell-selection="props">
            <q-td key="selection">
              <q-checkbox v-model="props.row.selected" class="checkbox" label="" color="primary"/>
            </q-td>
          </template>

          <template v-slot:body-cell-role="props">
            <q-td key="role">
              <div style="text-align: left">
                {{ props.row.role }}
              </div>
            </q-td>
          </template>
        </q-table>
      </div>
    </div>

    <div v-show="showIssuerMenu" class="right-half-big">

      <div class="information-header">{{ $t('admin_user_editor.issuerInformation') }}</div>

      <div class="split-layout-small">
        <div class="left-half-small">
          <div class="credentials-table-div">

            <div class="search-bar">
              <div class="text-h4">{{ $t('admin_user_editor.search') }}:</div>
              <q-input v-model="credentialSearch" dense style="width: 20rem" class="filter-element-search"/>
            </div>

            <q-table
                v-model:pagination="pagination"
                class="credentials-table no-scrollbar disable-scroll"
                :rows="credentialsTableDataFiltered"
                :columns="credentialsTableColumns"
                :virtual-scroll-sticky-size-start="48"
                row-key="id"
                :rows-per-page-options="[]"
                selection="multiple"
                v-model:selected="selectedCredentials"
                hide-header
                hide-bottom
                virtual-scroll
                bordered
                style="background-color: var(--background)"
            >

              <template v-slot:top-left>
                <q-th key="credential" flat class="table-header">
                  <div class="header-content">
                    <div>
                      {{ $t('admin_user_editor.credentials') }}
                      <q-icon class="refresh-icon" name="refresh" size="xs" @click="refreshCredentials()"
                              style="cursor: pointer"/>
                    </div>
                  </div>
                </q-th>
              </template>

              <template v-slot:body-cell-selection="props">
                <q-td key="selection">
                  <q-checkbox v-model="props.row.selected" class="checkbox" color="primary"/>
                </q-td>
              </template>

              <template v-slot:body-cell-credential="props">
                <q-td key="credential">
                  <div style="text-align: left">
                    {{ props.row.credential }}
                  </div>
                </q-td>
              </template>
            </q-table>
          </div>
        </div>

        <div class="right-half-small">
          <div class="selection">
            <div class="text-h4">{{ $t('admin_user_editor.selected') }}:</div>

            <q-list dense padding class="rounded-borders" style="width: 11rem">
              <template v-for="credential in selectedCredentials">
                <q-item clickable v-ripple dense>
                  <q-item-section style="text-overflow: unset">
                    {{ credential.credential }}
                  </q-item-section>
                  <q-space/>
                  <q-icon name="cancel" size="xs" style="align-self: center"
                          @click="() => deselectCredential(credential)"/>
                </q-item>
              </template>
            </q-list>
          </div>
        </div>
      </div>
    </div>

    <div v-show="showIssuerMenu" class="vertical-line"/>
  </div>



  <div class="split-layout-buttons relative-bottom">
    <div class="left-half-buttons">
      <div class="account-actions-buttons">
        <q-btn class="button" :label="$t('admin_user_editor.buttons.delete')" color="accent" @click="openModifyDialog(6)" style="margin-right: 1rem"/>
        <q-btn class="button" :label="$t('admin_user_editor.buttons.deactivate')" color="accent" @click="openModifyDialog(5)" style="margin-right: 1rem"/>
        <q-btn class="button" :label="$t('admin_user_editor.buttons.activate')" color="primary" @click="openModifyDialog(4)" style="margin-right: 1rem"/>
      </div>
    </div>
    <div class="right-half-buttons">
      <div class="save-buttons">
        <q-btn class="button" :label="$t('admin_user_editor.buttons.cancel')" color="accent" style="margin-right: 1rem;" to="/admin/users"/>
        <q-btn class="button" :label="$t('admin_user_editor.buttons.save')" color="primary" @click="save"/>
      </div>
    </div>
  </div>

  <ModifyUsersDialog v-model="showModifyDialog" :dialog-visible="showModifyDialog"
                     @update:dialog-visible="showModifyDialog = $event" :modifyId="modifyId"
                     @activate="activateUser" @deactivate="deactivateUser" @delete="deleteUser"
  />
</template>

<style scoped>
.admin-navigation-bar {
  position: fixed;
  left: 0;
  width: 100%;
  z-index: 999;
}

.information-header {
  display: flex;
  justify-content: center;
  font-size: 3rem;
  margin-bottom: 1rem;
}

/* Big Layout */

.split-layout-big {
  display: grid;
  grid-template-columns: 1fr 1fr;
  flex-wrap: wrap;
  margin-top: 5rem;
  height: fit-content;
  margin-left: auto;
  margin-right: auto;
  width: 95%;
  position: relative;
}

.left-half-big, .right-half-big {
  margin-left: auto;
  margin-right: auto;
  width: 100%;
}

.left-half-big {
  font-size: 2rem;
}

.right-half-big {
  display: flex;
  flex-direction: column;
}

@media (max-width: 1270px) {
  .split-layout-big {
    grid-template-columns: 1fr;
  }

  .right-half-big {
    margin-top: 3rem;
  }

  .information-header {
    justify-content: start;
    font-size: 2.5rem;
    line-break: inherit;
  }
}

@media (min-width: 1271px) {
  .vertical-line {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 50%;
    width: 1px;
    height: 120%;
    background-color: grey;
    transform: translateX(-50%);
  }
}

/* Small Layout */

.split-layout-small {
  display: grid;
  grid-template-columns: 23rem 1fr;
  gap: 10px;
  width: 100%;
}

.left-half-small {
  margin-left: 1rem;
  grid-column: 1;
  grid-row: 1;
  align-items: flex-start;
  display: flex;
  justify-content: flex-start;
}

.right-half-small {
   grid-column: 2;
 }

.selection {
  display: flex;
  flex-direction: column;
}

@media (max-width: 640px) {
  .split-layout-small {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr 1fr;
  }

  .left-half-small {
    align-self: flex-start;
    justify-content: flex-start;
    justify-items: flex-start;
    grid-column: 1;
    grid-row: 1;
  }

  .right-half-small {
    grid-column: 1;
    grid-row: 2;
    height: fit-content;
  }

  .credentials-table-div {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 1270px) {
  .left-half-small {
    margin-left: 0;
    margin-top: 0.5rem;
  }

  .right-half-small {
     margin-left: 0;
   }
}

/* Buttons */

.split-layout-buttons {
  display: grid;
  grid-template-columns: 75% 25%;
  margin-top: 5rem;
  height: fit-content;
  padding: 1rem;
}

.left-half-buttons {
  grid-column: 1;
  display: flex;
}

.right-half-buttons {
  grid-column: 2;
}

.account-actions-buttons {
  display: initial;
  overflow-x: visible;
}

.save-buttons {
  display: flex;
  justify-content: flex-end;
}

.button {
  width: fit-content;
  min-height: max-content;
  margin-top: 1rem;
}

@media (max-width: 1270px) {
  .split-layout-buttons {
    grid-template-columns: 100% 0;
  }

  .right-half-buttons {
    grid-column: 1;
    grid-row: 2;

    margin-top: 1rem;
    margin-bottom: 1rem;
  }

  .save-buttons {
    display: initial;
    justify-content: flex-start;
  }
}

/* Tables */

.table-header {
  text-align: left;
  width: 18rem;
}

.permissions-table {
  width: 20rem;
  max-height: 13.5rem;
  font-size: 1rem;
  margin-top: 1rem;
}

.credentials-table-div {
  display: flex;
  flex-direction: column;
}

.search-bar {
  justify-content: start;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}


.refresh-icon {
  justify-self: flex-end;
  align-self: flex-end;
}

.credentials-table {
  width: 20rem;
  max-height: 30rem;
  justify-self: end;
  margin-top: 1rem;
}

.disable-scroll, .scroll {
  overflow: hidden;
}

.credentials-table.scroll {
  scrollbar-width: none;
  -ms-overflow-style: none;
  overflow-y: scroll;
}

.checkbox {
  padding: 0;
  margin: 0;
  width: 20px;
  height: 20px;
}

</style>

<style lang="sass">
.permissions-table
  .q-table__top,
  thead tr:first-child th /* bg color is important for th; just specify one */
        background-color: var(--q-primary)
  thead tr th
    position: sticky
    z-index: 1
  thead tr:last-child th
    top: 48px
  thead tr:first-child th
    top: 0
  tbody
    scroll-margin-top: 48px

.credentials-table
  .q-table__top,
  thead tr:first-child th /* bg color is important for th; just specify one */
        background-color: var(--q-primary)
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

