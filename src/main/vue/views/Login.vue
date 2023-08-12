<script lang="ts">
import {useI18n} from "vue-i18n"
import {useQuasar} from 'quasar'
import {ref} from "vue"
import {useLoginStore} from "../stores/loginStore"
import {useRouter} from "vue-router"
import ForgotPasswordDialog from "@/main/vue/components/ForgotPasswordDialog.vue";

export default {
  name: "Login",
  components: {ForgotPasswordDialog},

  setup() {
    const $q = useQuasar()
    const {t} = useI18n()
    const loginStore = useLoginStore()
    const router = useRouter()
    const isPwd = ref(true)
    const emailInp = ref('')
    const passwordInp = ref('')
    const forgotPassword = ref(false)


        function login() {
            loginStore.requestToken({
                email: emailInp.value,
                password: passwordInp.value
            }).then(() => {
                $q.notify({
                  message: t("title_login.loginSuccess"),
                  color: 'primary'
                })
              router.push("/")
            }).catch((error) => {
                if (error instanceof Error && error.message === "activation") {
                  $q.notify({
                    type: 'negative',
                    message: t("title_login.loginFailed"),
                    caption: t('title_login.authenticationFailed')
                  })
                } else {
                  $q.notify({
                    type: 'negative',
                    message: t("title_login.loginFailed"),
                    caption: t('title_login.wrongLogin')
                  })
                }
            })
        }

    function closeDialog() {
      forgotPassword.value = false;
    }

    function openDialog() {
      forgotPassword.value = true;
    }

    return {emailInp, passwordInp, login, isPwd, forgotPassword, openDialog, closeDialog}
  }
}
</script>

<template>
  <div class="row justify-center align-center">
    <q-dialog v-model="forgotPassword" persistent>
      <ForgotPasswordDialog @close="closeDialog"/>
    </q-dialog>
  </div>

  <div class="outer-container align-center">
    <q-card class="q-pa-md row outer-card">
      <q-card-section>
        <q-card class="inner-card">
          <div class="text-h6 text-center">Login</div>
          <q-input class="input" v-model="emailInp" :label="$t('title_login.email')" dense/>
          <q-input ref="password" class="input" v-model="passwordInp" :label="$t('title_login.password')" dense
                   :type="isPwd ? 'password' : 'text'"
                   @keydown.enter.prevent="login()">
            <template v-slot:append>
              <q-icon
                  :name="isPwd ? 'visibility_off' : 'visibility'"
                  class="cursor-pointer"
                  @click="isPwd = !isPwd"
              />
            </template>
          </q-input>
          <q-card-section>
            <div class="q-gutter-y-md column">
              <q-btn color="primary" label="Login" @click="login()"></q-btn>
              <a class="forgot-password-btn" @click="openDialog()">{{ $t('forgotPassword') }}</a>
            </div>
          </q-card-section>
        </q-card>
      </q-card-section>

      <q-card-section>
        <q-card class="column inner-card" style="justify-content: center" align="center">
          <q-card-section>
            <div class="text">{{ $t('title_login.justWantToLookAtTheFloorplan') }}</div>
            <div class="q-gutter-y-md column">
              <q-btn color="primary" to="/">{{ $t("title_login.continueAsGuest") }}</q-btn>
            </div>
          </q-card-section>
          <q-card-section>
            <div class="text">{{ $t('title_login.newHere') }}</div>
            <div class="q-gutter-y-md column">
              <q-btn color="primary" to="registration">{{ $t("title_login.registration") }}</q-btn>
            </div>
          </q-card-section>
        </q-card>
      </q-card-section>
    </q-card>
  </div>
</template>

<style scoped>
.q-card {
  background-color: var(--background);
}

.outer-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
}

.outer-card {
  display: inline-block;
  width: fit-content;
  margin: 2rem auto 2rem;
  justify-content: center;
  padding: 0;
}

.inner-card {
  width: 20rem;
  height: 100%;
}

.input {
  width: 90%;
  margin: auto;
}

.forgot-password-btn {
  text-decoration: underline;
  text-transform: lowercase;
  cursor: pointer;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
}

@media (min-width: 55rem) {
  .outer-card {
    display: flex;
  }
}
</style>