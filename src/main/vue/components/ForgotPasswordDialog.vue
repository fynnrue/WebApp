<script lang="ts">
import {useQuasar} from "quasar";
import {useI18n} from "vue-i18n";
import {ref} from "vue";
import {requestPasswordReset} from "@/main/vue/api/auth";

export default {
  name: 'ForgotPasswordDialog',
  components: {},
  props: {},
  emits: ['close'],

  setup(_: any, { emit }: { emit: (event: 'close') => void }) {
    const $q = useQuasar()
    const {t} = useI18n()

    const email = ref('')

    function resetPassword() {
      if (email.value !== '') {
        requestPasswordReset(email.value).then(() => {
          $q.notify({
            message: t("forgotPasswordDialog.emailSent"),
            color: 'primary'
          })
          closeDialog();
        }).catch(() => {
          $q.notify({
            type: 'negative',
            message: t("forgotPasswordDialog.resetFailed"),
            caption: t('forgotPasswordDialog.emailNotFound')
          })
        })
      }
    }

    function closeDialog() {
      emit('close');
    }

    return {
      email, closeDialog, resetPassword
    }
  },
}

</script>

<template>
  <div>
    <q-card class="absolute-center">

      <q-bar>
        <div class="text-h4">{{ $t('forgotPasswordDialog.header') }}</div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer"  @click="closeDialog"/>
      </q-bar>

      <q-card-section align="center">
        <q-input v-model="email" :label="$t('forgotPasswordDialog.email')"
                  @keydown.enter.prevent="resetPassword">
        </q-input>
      </q-card-section>
      <q-card-actions align="right">
          <q-btn :label="$t('forgotPasswordDialog.back')" @click="closeDialog" color="accent" class="q-tab--no-caps"/>
          <q-btn :label="$t('forgotPasswordDialog.reset')" @click="resetPassword" color="primary" class="q-tab--no-caps"/>
      </q-card-actions>
    </q-card>
  </div>
</template>

<style scoped>

.q-card {
  min-width: 300px;
  width: 40%;
  height: fit-content;
  background-color: var(--background);
}

.q-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  /*noinspection CssUnresolvedCustomProperty*/
  background-color : var(--surface);
  height: 40px;
}

.q-btn {
  margin: 0 10px;
}

</style>