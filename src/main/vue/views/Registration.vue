<script lang="ts">
import {ref} from 'vue'
import {useQuasar} from "quasar"
import {useI18n} from "vue-i18n"
import api from "../api"
import router from "@/main/vue/router";

export default {
  name: "Registration",
  components: {},

  setup() {
    const $q = useQuasar()
    const {t} = useI18n()

    const register = ref(true)
    const forename = ref('')
    const surname = ref('')
    const email = ref('')
    const password = ref('')
    const repPassword = ref('')
    const isPwd = ref(true)
    const isPwdRep = ref(true)

    function registerUser() {
      $q.loading.show();
      if (forename.value == "" || surname.value == "" || email.value == "" || password.value == "" || repPassword.value == "") {
        $q.notify({
          type: 'negative',
          message: t('registration.registrationFailed'),
          caption: t('registration.fillFields')
        });
        $q.loading.hide();
        return;
      }
      if (password.value === repPassword.value) {
        const checkCreated = api.register.registerUser(forename.value, surname.value, email.value, password.value).then(response => {
          const data = response.data

          if (data == "user exists") {
            $q.notify({
              type: 'negative',
              message: t('registration.registrationFailed'),
              caption: t('registration.emailExists')
            })
          } else if (data == "registered") {
            $q.notify({
              message: t('registration.success'),
              color: 'primary'
            })
            router.push("/login")
          } else {
            $q.notify({
              type: 'negative',
              message: t('registration.registrationFailed'),
              caption: t('registration.somethingWrong')
            })
          }
          $q.loading.hide();
        })
      } else {
        $q.notify({
          type: 'negative',
          message: t('registration.registrationFailed'),
          caption: t('registration.repeatPassword')
        })
        $q.loading.hide();
      }
    }

    return {forename, surname, email, password, repPassword, isPwd, isPwdRep, registerUser, register}
  }
}
</script>

<template>
  <q-card class="q-pa-md absolute-center" style="background-color: var(--background)">

    <!-- header -->
    <q-card-section align="center">
      <div class="text-h4">{{ $t('registration.header') }}</div>
    </q-card-section>

    <!-- text-inputs and register button -->
    <q-card-section align="center" class="registration-components">
      <q-input v-model="forename" :label="$t('registration.forename-hi')"/>
      <q-input v-model="surname" :label="$t('registration.surname-hi')"/>
      <q-input v-model="email" :label="$t('registration.email-hi')"/>
      <q-input v-model="password" :label="$t('registration.password-hi')"
               :type="isPwd ? 'password' : 'text'">
        <template v-slot:append>
          <q-icon
              :name="isPwd ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwd = !isPwd"
          />
        </template>
      </q-input>
      <q-input v-model="repPassword" :label="$t('registration.rep-password-hi')"
               :type="isPwdRep ? 'password' : 'text'">
        <template v-slot:append>
          <q-icon
              :name="isPwdRep ? 'visibility_off' : 'visibility'"
              class="cursor-pointer"
              @click="isPwdRep = !isPwdRep"
          />
        </template>
      </q-input>

      <!-- buttons for registration and to get to login page -->
      <q-btn :label="$t('registration.register')" @click="registerUser" color="primary"
             style="margin-bottom: 0.5rem; margin-top: 0.7rem"/>
      <hr>
      <q-btn flat :label="$t('registration.logIn')" to="login" color="text-primary"/>
    </q-card-section>
  </q-card>

</template>

<style scoped>
.q-card {
  margin-top: 1rem;
  width: 40%;
  min-width: 300px;
  height: fit-content;
  margin: 20px auto 20px;
  overflow-y: hidden;
}

.registration-components {
  margin: auto;
  width: 90%;
}
</style>