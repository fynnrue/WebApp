<script lang="ts">
import {useQuasar} from "quasar";
import {useI18n} from "vue-i18n";
import {ref} from "vue";
import {resetPasswordWithToken} from "@/main/vue/api/auth";
import router from "@/main/vue/router";

export default {
  name: 'PasswordReset',
  components: {},

  props: {
    email: {
      type: String,
      required: true
    },
    token: {
      token: String,
      required: true
    }
  },

  setup(props: any) {
    const $q = useQuasar()
    const {t} = useI18n()

    const password = ref('')
    const repPassword = ref('')
    const isPwd = ref(true)
    const isPwdRep = ref(true)

    function resetPassword() {
      if (password.value === '') {
        $q.notify({
          message: t('resetPassword.passwordEmpty'),
          type: 'negative'
        })
        return
      }
      if (password.value !== repPassword.value) {
        $q.notify({
          message: t('resetPassword.passwordsNotEqual'),
          type: 'negative'
        })
        return
      }
      resetPasswordWithToken(props.email, props.token, password.value).then(() => {
        $q.notify({
          message: t('resetPassword.passwordChanged'),
          color: 'primary'
        })
        router.push('/login')
      }).catch(() => {
        $q.notify({
          message: t('resetPassword.passwordNotChanged'),
          type: 'negative',
        })
      })
    }

    return {
      isPwd, isPwdRep, password, repPassword, resetPassword
    }
  },
}

</script>

<template>
  <div>
    <q-card class="q-pa-md absolute-center qcard">
      <q-card-section align="center">
        <div class="text-h4">{{ $t('resetPassword.header') }}</div>
      </q-card-section>

      <q-card-section align="center">
        <q-input v-model="password" :label="$t('resetPassword.password')"
                 :type="isPwd ? 'password' : 'text'">
          <template v-slot:append>
            <q-icon
                :name="isPwd ? 'visibility_off' : 'visibility'"
                class="cursor-pointer"
                @click="isPwd = !isPwd"
            />
          </template>
        </q-input>
        <q-input v-model="repPassword" :label="$t('resetPassword.repeatPassword')"
                 :type="isPwdRep ? 'password' : 'text'">
          <template v-slot:append>
            <q-icon
                :name="isPwdRep ? 'visibility_off' : 'visibility'"
                class="cursor-pointer"
                @click="isPwdRep = !isPwdRep"
            />
          </template>
        </q-input>

        <q-btn :label="$t('resetPassword.setPassword')" @click="resetPassword" color="primary"
               style="margin-top: 2rem"/>
      </q-card-section>
    </q-card>

  </div>
</template>

<style scoped>

.q-card {
  min-width: 300px;
  width: 35%;
  height: fit-content;
}

</style>