<template>
  <q-dialog class="q-dialog">
    <q-card style="background-color: var(--background)">
      <q-bar class="dialog-bar">
        <div class="text-h4">{{ t(title) }}</div>
        <q-space />
        <q-icon name="close" class="cursor-pointer"  @click="closeDialog"/>
      </q-bar>
      <q-card-section>
        <q-input v-model="newName" :label="t('start.name')" />
        <q-file v-model="newImage" accept=".jpg, image/*" single :label="t('start.select_image')" clearable/>
      </q-card-section>
      <q-card-actions>
        <div class="buttons">
          <q-btn :label="t('start.cancel')" @click="closeDialog" color="accent" class="q-tab--no-caps button" />
          <q-btn :label="t('start.add')" @click="uploadBuilding" color="primary" class="q-tab--no-caps button" />
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useQuasar } from 'quasar';

export default defineComponent({
  name: 'UploadDialogue',
  emits: ['submit', 'closeUploadDialog'],
  props: {
    title: String,
    requireNameAndImage: {type: Boolean, default: true},
  },
  setup(props, { emit }) {
    const newImage = ref<File | null>(null);
    const newName = ref<string | null>(null);
    const { t } = useI18n();
    const $q = useQuasar();

    function notifyError(message: string) {
      $q.notify({
        type: 'negative',
        caption: t('start.upload_failed'),
        message: t(message),
      });
    }

    async function uploadBuilding() {
      try {
        if (newName.value == null && newImage.value == null) {
          return;
        }
        if (props.requireNameAndImage) {
          if (newName.value == null) {
            notifyError('start.invalid_name');
            return;
          }
          if (newImage.value == null) {
            notifyError('start.invalid_image');
            return;
          }
        }
        //await addBuilding(newName.value, newImage.value);
        emit( 'submit', { name: newName.value, image: newImage.value });
        newImage.value = null;
        newName.value = null;
      } catch (error: unknown) {
        console.error((error as Error).message);
      }
    }

    function closeDialog() {
      newImage.value = null;
      newName.value = null;
      emit('closeUploadDialog');
    }
    return { newImage, newName, t, uploadBuilding, closeDialog };
  },
});
</script>

<style scoped>

.dialog-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  /* it works, but the IDE doesn't like it */
  /*noinspection CssUnresolvedCustomProperty*/
  background-color : var(--surface);
  height: 40px;
}

.q-dialog {
  max-width: 500px;
  max-height: 400px;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.q-card {
  width: 100%;
  min-width: 300px;
  box-sizing: border-box;
  background-color: var(--background);
}

.buttons {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  width: 98%;
  margin: 0 auto;
}

.button {
  width: 48%;
  min-width: 8rem;
  margin-bottom: 5%;
}

@media (max-width: 350px) {
  .buttons {
    flex-direction: column;
  }

  .button {
    width: 100%;
  }
}
</style>
