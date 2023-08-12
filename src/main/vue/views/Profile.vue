<template>
  <q-dialog v-model="deleteDialog">
    <q-card align="center" v-if="user != null" class="deleteCard" style="background-color: var(--background)" >
      <q-bar>
        <div class="text-h4">{{ $t('profile.delete_account') }}</div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer"  @click="deleteDialog = false"/>
      </q-bar>

      <div class="card-content">
        <q-card-section>
          <div class="text-h6">{{ $t('profile.delete_account_text') }}</div>
        </q-card-section>

        <q-card-section>
          <q-input
              outlined
              style="margin-bottom: 20px"
              v-model="currentPassword"
              :label="$t('profile.password')"
              type="password"
          />
        </q-card-section>
        <q-card-actions>
          <div class="buttons">
          <q-btn class="button" size="md" color="accent" @click="deleteDialog = false">{{
              $t("profile.cancel")
            }}
          </q-btn>
          <q-btn class="button" size="md" color="primary" @click="deleteUser()">{{
            $t("profile.delete_account")
          }}
        </q-btn>
          </div>
        </q-card-actions>
      </div>
    </q-card>
  </q-dialog>


  <q-card align="center" v-if="user != null" class="q-pa-md"
          style="width: fit-content; height: fit-content; background-color: var(--background)" >

    <div class="text-h4" style="margin-bottom: 5%">{{ $t('profile.welcome') + ", " + user.forename + "!" }}</div>
    <div class="card-content" style="width: 80%">
      <div class="text-h5" style="margin-bottom: 5%">{{ $t('profile.your_profile') }}</div>
      <q-card-section>
        <q-input :disable="this.editable" class="input" filled color="primary" v-model="user.forename"
                 :label="$t('profile.forename')"/>
        <q-input :disable="this.editable" class="input" filled color="primary" v-model="user.surname"
                 :label="$t('profile.surname')"/>
        <q-input :disable="this.editable" class="input" filled color="primary" v-model="user.email"
                 :label="$t('profile.email')"/>
        <q-input :disable="true" class="input" filled color="primary"
                 :model-value="getRoleLabel(user.roles)" :label="$t('profile.roles')" readonly/>
      </q-card-section>
      <q-card-actions>
        <div class="buttons profile-buttons">
          <q-btn color="primary" to="changePassword" class="button profile-button">{{ $t("profile.change_password") }}</q-btn>
          <q-btn color="primary" @click="saveChanges" class="button profile-button"> {{ this.editable ? $t("profile.edit") : $t("profile.save_changes") }}</q-btn>
        </div>
      </q-card-actions>

      <q-card-actions>
        <div class="buttons profile-buttons">
          <q-btn color="primary" @click="deleteDialog = true" class="button profile-button">{{ $t("profile.delete_account") }}</q-btn>
          <q-btn color="primary" @click="logout()" class="button profile-button">{{ $t("profile.logout") }}</q-btn>
        </div>
      </q-card-actions>
    </div>
  </q-card>
</template>

<script lang="ts">
import {Role, userStore} from "@/main/vue/stores/userStore";
import {computed, defineComponent, Ref, ref} from "vue";
import {useLoginStore} from "@/main/vue/stores/loginStore";
import router from "@/main/vue/router";
import {useQuasar} from "quasar";
import {useI18n} from "vue-i18n";
import axios from "axios";

export default defineComponent({
  name: "Profile",
  setup() {
    const store = userStore()
    const q = useQuasar()
    const user = computed(() => store.currentUser)
    const {t} = useI18n()
    const deleteDialog = ref(false);
    const currentPassword = ref("");
    const editable = ref(true);

    function logout() {
      q.notify({
        message: t("title_login.logout"),
        color: 'primary'
      })
      useLoginStore().logout()
      router.push("/")
    }

    async function saveChanges() {
      editable.value = !editable.value;
      if (user.value !== null && editable.value) {

        //store.updateUser(user.value);
        const credentials = new URLSearchParams();
        credentials.append('username', user.value.username);
        credentials.append('newForename', user.value.forename);
        credentials.append('newSurname', user.value.surname);
        credentials.append('newEmail', user.value.email);

        if (user.value?.username !== user.value?.email) {
          await logout();
          router.push("/");
        }

        axios.post("/api/user/changeUser", credentials)
            .then((response) => {
              if (response.data === "valid") {
                q.notify({
                  message: t("profile.changed"),
                  color: "primary",
                });
              } else {
                q.notify({
                  message: t("profile.invalid"),
                  color: "negative",
                });
              }
            })
            .catch(() => {
              q.notify({
                message: t("profile.error"),
                color: "negative",
              });
            });
      }
    }

    async function deleteUser() {
      const data = new URLSearchParams();
      data.append("email", user.value!.email);
      data.append("password", currentPassword.value);

      axios.post("/api/user/deleteUser", data)
          .then((response) => {
            if (response.data === "valid") {
              q.notify({
                message: t("profile.deleted"),
                color: "primary",
              });
              logout();
            } else {
              q.notify({
                message: t("profile.invalid"),
                color: "negative",
              });
            }
          })
          .catch(() => {
            q.notify({
              message: t("profile.error"),
              color: "negative",
            });
          });

    }

    function getRoleLabel(roles: Role[]): string {
      const roleLabels: string[] = [];
      roles.forEach((role) => {
        switch (role) {
          case Role.Admin:
            roleLabels.push("Admin");
            break;
          case Role.Issuer:
            roleLabels.push("Issuer");
            break;
          case Role.Editor:
            roleLabels.push("Editor");
            break;
        }
      });
      return roleLabels.join(", ");
    }

    return {logout, user, Role, saveChanges, getRoleLabel, editable, deleteUser, deleteDialog, currentPassword}
  }
})

</script>

<style scoped>
.q-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  /*noinspection CssUnresolvedCustomProperty*/
  background-color : var(--background);
  height: 40px;
  margin-top: -15px;
}

.q-card {
  padding-top: 1rem;
  min-width: 300px;
  width: fit-content;
  height: fit-content;
  margin: 20px auto 20px;
  overflow-y: hidden;
}

.input {
  margin-bottom: 1rem;
}

.buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.button {
  width: 48%;
  min-width: 10rem;
  margin-bottom: 5%;
}

@media (max-width: 571px) {
  .profile-buttons {
    flex-direction: column;
  }

  .profile-button {
    width: 100%;
  }
}

@media (max-width: 400px) {
  .buttons {
    flex-direction: column;
  }

  .button {
    width: 100%;
  }
}

</style>