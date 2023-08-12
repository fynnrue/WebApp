<script lang="ts">
import {computed, defineComponent} from 'vue';
import {useI18n} from 'vue-i18n';
import {useQuasar} from 'quasar';

export default defineComponent({
  name: 'DeleteUsersDialog',
  props: {
    dialogVisible: {
      type: Boolean,
      required: true,
    },
    modifyId: {
      type: Number,
      required: true
    }
  },
  emits: ['update:dialogVisible', 'delete', 'activate', 'deactivate'],
  setup(props, {emit}) {
    const {t} = useI18n();
    const $q = useQuasar();

    function confirmModify() {
      switch (props.modifyId) {
        case 1:
          confirmActivateUsers();
          break;
        case 2:
          confirmDeactivateUsers();
          break;
        case 3:
          confirmDeleteUsers();
          break;
        case 4:
          confirmActivateUsers();
          break;
        case 5:
          confirmDeactivateUsers();
          break;
        case 6:
          confirmDeleteUsers();
          break;
        default:
          closeDialog();
          break;
      }
    }

    const getConfirmHeader = computed(() => {
      switch (props.modifyId) {
        case 1:
          return t('modify_user_dialog.activateUsersH');
        case 2:
          return t('modify_user_dialog.deactivateUsersH');
        case 3:
          return t('modify_user_dialog.deleteUsersH');
        case 4:
          return t('modify_user_dialog.activateUserH');
        case 5:
          return t('modify_user_dialog.deactivateUserH');
        case 6:
          return t('modify_user_dialog.deleteUserH');
        default:
          return '';
      }
    });

    function confirmActivateUsers() {
      emit('activate')
      closeDialog()
    }

    function confirmDeactivateUsers() {
      emit('deactivate')
      closeDialog()
    }

    function confirmDeleteUsers() {
      emit('delete')
      closeDialog()
    }

    function closeDialog() {
      emit('update:dialogVisible', false);
    }

    return {t, closeDialog, confirmModify, props, getConfirmHeader};
  },
});
</script>

<template>
  <q-dialog :model-value="props.dialogVisible" persistent>
    <q-card class="delete-users-dialog items-center">
      <q-bar>
        <div class="text-h4">{{ getConfirmHeader }}</div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer"  @click="closeDialog"/>
      </q-bar>

      <q-card-section>
        <div>{{ t('modify_user_dialog.confirmQuestion') }}</div>
      </q-card-section>
      <q-card-actions align="right">
        <q-btn :label="$t('modify_user_dialog.cancel')" @click="closeDialog" class="q-tab--no-caps" color="accent"/>
        <q-btn :label="$t('modify_user_dialog.confirm')" @click="confirmModify" class="q-tab--no-caps" color="primary"/>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<style scoped>

.delete-users-dialog {
  align-content: center;
  text-align: center;
}

.q-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  /*noinspection CssUnresolvedCustomProperty*/
  background-color : var(--background);
  height: 40px;
}

.q-btn {
  margin: 0 10px;
}

</style>