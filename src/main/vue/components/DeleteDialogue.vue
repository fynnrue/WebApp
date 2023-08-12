<template>
  <q-dialog class="q-dialog">
    <q-card>
      <q-bar class="dialog-bar">
        <div class="text-h4">{{ t(title) }}</div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer" @click="closeDialog"/>
      </q-bar>
      <q-card-section align="center">
        {{ t('delete_dialog.select') }}
        <div class="q-pa-md">
          <div class="q-gutter-y-md column" style="max-width: 300px">
            <q-select clearable filled color="primary" v-model="buildingFloor" :options="getBuildingOptions"
                      :label="t('delete_dialog.selection')"/>
            <div v-if="buildingFloor" class="q-mt-sm">{{ t('delete_dialog.s') }}: {{ buildingFloor.name }}</div>
          </div>
        </div>
        {{ t('delete_dialog.confirmation') }}
      </q-card-section>
      <q-card-actions>
        <div class="buttons">
          <q-btn :label="t('delete_dialog.cancel')" @click="closeDialog" color="accent" class="q-tab--no-caps button"/>
          <q-btn :label="t('delete_dialog.confirm')" @click="deleteBuilding" color="primary" class="q-tab--no-caps button"/>
        </div>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import {defineComponent, ref, computed, PropType} from 'vue';
import {useI18n} from 'vue-i18n';
import {useQuasar} from 'quasar';
import {Floor} from "@/main/vue/types/floor";
import {Building} from '../types/building';
import {compileString} from "sass";

type BuildingOrFloor = Building | Floor
type BuildingFloorArr = Building[] | Floor[]

export default defineComponent({
  name: 'DeleteDialogue',
  emits: ['delete', 'closeDeleteDialog'],
  props: {
    title: String,
    floors: {
      type: Array as () => BuildingFloorArr || null,
      required: true
    }
  },
  setup(props, {emit}) {
    const {t} = useI18n();
    const $q = useQuasar();
    const buildingFloor = ref<Floor | null>(null)

    const getBuildingOptions = computed(() => {
      if (props.floors) {
        return props.floors.map((floor: BuildingOrFloor) => {
          return {
            label: `${floor.name}`,
            value: floor,
            name: floor.name,
            id: floor.id
          }
        });
      }
      return [];
    });

    function notifyError(message: string) {
      $q.notify({
        type: 'negative',
        caption: t('delete_dialog.delete_failed'),
        message: t(message),
      });
    }


    async function deleteBuilding() {
      try {
        if (buildingFloor.value) {
          emit('delete', {id: buildingFloor.value.id});
          buildingFloor.value = null;
        } else {
          notifyError('delete_dialog.no_building_selected');
          return;
        }
      } catch (error: unknown) {
        console.error((error as Error).message);
      }
    }

    function closeDialog() {
      buildingFloor.value = null;
      emit('closeDeleteDialog');
    }

    return {t, deleteBuilding, closeDialog, buildingFloor, getBuildingOptions};
  },
});
</script>

<style scoped>

.dialog-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  /* it works, but the IDE doesn't like it */
  /*noinspection CssUnresolvedCustomProperty*/
  background-color: var(--surface);
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