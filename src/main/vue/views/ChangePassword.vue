<script lang="ts">
import {Role, userStore} from "@/main/vue/stores/userStore";
import {useQuasar} from "quasar";
import {computed, defineComponent, ref} from "vue";
import {useI18n} from "vue-i18n";
import axios from "axios";

export default defineComponent({
  name: "ChangePassword",
  data() {
    return {
      oldPassword: ref<string>(""),
      newPassword: ref<string>(""),
      passwordRepeat: ref<string>(""),
    };
  },
  methods: {
    saveChanges() {
      if (this.passwordRepeat !== this.newPassword) {
        this.$q.notify(
            {
              message: this.$t('profile.passwordsNotEqual'),
              color: "negative",
            }
        )
      } else {
        const credentials = new URLSearchParams();
        credentials.append('oldPassword', this.oldPassword);
        credentials.append('newPassword', this.newPassword);
        credentials.append('email', this.user!.email)
        axios.post("/api/user/changePassword", credentials)
            .then((response) => {
              if (response.data == "valid") {
                this.$q.notify(
                    {
                      message: this.$t('profile.changed'),
                      color: "primary",
                    }
                )
              } else {
                this.$q.notify(
                    {
                      message: this.$t('profile.wrongPassword'),
                      color: "negative",
                    }
                )
              }
            })
      }
      return
    }
  },
  setup() {
    const store = userStore()
    const q = useQuasar()
    const user = computed(() => store.currentUser)
    const {t} = useI18n()

    return {user}
  }
})
</script>

<template>
  <!-- Card to change the password of user -->
  <q-card class="absolute-center" style="background-color: var(--background)">
    <q-card-section>
      <div class="text-h4" style="margin-bottom: 5%; text-align: center">{{ $t("profile.changePassword") }}</div>
      <div class="inputs">
        <q-input
            class="input"
            outlined
            style="margin-bottom: 20px"
            v-model="oldPassword"
            :label="$t('profile.oldPassword')"
            type="password"
        />
        <q-input
            class="input"
            outlined
            v-model="newPassword"
            :label="$t('profile.newPassword')"
            type="password"
            :rules="[val => val.length > 0 || 'Password is required']"
        />
        <q-input
            class="input"
            outlined
            v-model="passwordRepeat"
            :label="$t('profile.repeatPassword')"
            type="password"
            :rules="[val => val.length > 0 || 'Password is required']"
        />
      </div>
    </q-card-section>
    <q-card-actions>
      <div class="buttons">
        <q-btn class="button" type="button" color="accent" :label="$t('profile.back_to_profile')"
               @click="$router.push('/profile')"/>
        <q-btn class="button" type="submit" color="primary" :label="$t('profile.save_changes')"
               @click="saveChanges"/>
      </div>
    </q-card-actions>
  </q-card>
</template>

<style scoped>
.q-card {
  padding-top: 1rem;
  min-width: 320px;
  width: 35%;
  height: fit-content;
  margin: 20px auto 20px;
  overflow-y: hidden;
}

.inputs {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.input {
  align-self: center;
  justify-self: center;
  width: 90%;
  margin: 0 auto;
}

.buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  width: 90%;
  margin: 0 auto;
}

.button {
  width: 48%;
  min-width: 8rem;
  margin-bottom: 5%;
}

@media (max-width: 1247px) {
  .buttons {
    flex-direction: column;
  }

  .button {
    width: 100%;
  }
}
</style>