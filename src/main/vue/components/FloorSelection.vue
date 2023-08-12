<template>
  <div class="text-h4 q-ma-xl"> {{buildingName}} - {{ $t("start.select_floor")}}</div>
    <div class="q-mx-xl row items-start q-gutter-md" v-if="building != null">
      <q-card v-for="floor in floors" :key="floor.id" class="my-card" :title="floor.name" @click="toFloorplan(floor.id, floor.image)" :floorImage="floor.image">
        <img :src="floor.image" width="100" height="100" :alt="floor.name" class="">
        <q-card-section class="text-center my-card-section">
          <div class="text-h6 my-text-ellipsis">{{ floor.name }}</div>
        </q-card-section>
      </q-card>
      <RoleFilter :role="Role.Editor">
        <q-btn color="primary" round class="fab" @click="openDialogAdd">
          <q-icon name="add" size="md"/>
          <q-tooltip color="primary">{{ $t("start.add_floor") }}</q-tooltip>
        </q-btn>

        <q-btn color="primary" round class="fab" @click="openDialogDelete">
          <q-icon name="delete" size="md"/>
          <q-tooltip color="primary">{{ $t("start.delete_floor") }}</q-tooltip>
        </q-btn>
      </RoleFilter>
    </div>

  <UploadDialogue v-model="dialogVisibleAdd" title="start.add_floor" @submit="handleUpload" @closeUploadDialog="closeUpload"/>
  <DeleteDialogue v-model="dialogVisibleDelete" title="start.delete_floor" :floors="floors" @delete="handleDelete" @closeDeleteDialog="closeDelete"/>
</template>

<script lang="ts">
import {defineComponent, onBeforeMount, Ref, ref} from "vue";
import {addFloor, deleteRoom, fetchFloor} from "@/main/vue/api/floor";
import {useI18n} from "vue-i18n"
import {useQuasar} from "quasar";
import {Role} from "@/main/vue/stores/userStore";
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import {Building} from "@/main/vue/types/building";
import {addBuilding, fetchBuilding} from "@/main/vue/api/buildingApi";
import {Floor} from "@/main/vue/types/floor";
import {useRouter} from "vue-router";
import {useFloorStore} from "@/main/vue/stores/floorStore";
import UploadDialogue from "@/main/vue/components/UploadDialogue.vue";
import DeleteDialogue from "@/main/vue/components/DeleteDialogue.vue";


export default defineComponent({
  name: "FloorSelection",
  components: {DeleteDialogue, RoleFilter, UploadDialogue},
  props: {
    buildingId: String,
  },
  setup(props) {
    const buildingIdNum = parseInt(props.buildingId!)
    const newImage: Ref<File | null> = ref(null)
    const newFloorName: Ref<string | null> = ref(null)
    const {t} = useI18n()
    const $q = useQuasar()
    const router = useRouter()
    const building = ref(null) as Ref<null | Building>
    const floors = ref(null) as Ref<null | Floor[]>
    const buildingName = ref("")
    const dialogVisibleAdd = ref(false)
    const dialogVisibleDelete = ref(false)

    onBeforeMount(() => {
      try {
        callFetchFloor();
      } catch (error: unknown) {
        console.error((error as Error).message);
      }
    });

    function openDialogAdd() {
      dialogVisibleAdd.value = true;
      dialogVisibleDelete.value = false;
    }
    function openDialogDelete() {
      dialogVisibleAdd.value = false;
      dialogVisibleDelete.value = true;
    }

    function toFloorplan(floorId: number, floorImage: string) {
      useFloorStore().setFloorId(floorId);
      useFloorStore().setImagePath(floorImage);
      useFloorStore().setBuildingId(building.value!.id);
      router.push({
        name: 'FloorplanVisual',
        params: {
          buildingId: building.value?.id,
          floorId: floorId,
        }
      });
    }

    function notifyError(message: string) {
      $q.notify({
        type: 'negative',
        caption: t('start.upload_failed'),
        message: t(message)
      })
    }

    async function callFetchFloor() {
      $q.loading.show({
        message: t("start.loading_floors"),
        delay: 300,
      });
      try {
        building.value = await fetchBuilding(BigInt(buildingIdNum))
        floors.value = await fetchFloor(building.value.id)
        buildingName.value = building.value.name
      } catch (error: unknown) {
        console.error((error as Error).message);
      }
      $q.loading.hide()
    }
    async function handleUpload({name, image}: { name: string, image: File }){
      try {
        closeUpload();
        await addFloor(name, image, buildingIdNum);
      } catch {
        console.error("Failed to upload floor");
        notifyError("start.upload_failed");
      }
      await callFetchFloor();
    }

    function closeUpload() {
      dialogVisibleAdd.value = false;
    }

    async function handleDelete({id}: {id: number}) {
      try {
        closeUpload();
        await deleteRoom(BigInt(id));
      } catch {
        console.error("Failed to delete floor");
        notifyError("delete_dialog.delete_failed");
      }
      await callFetchFloor();
    }

    function closeDelete() {
      dialogVisibleDelete.value = false;
    }

    return {newImage, newFloorName, t, notifyError, Role, building, buildingName, dialogVisibleDelete, dialogVisibleAdd, openDialogDelete, openDialogAdd, floors, toFloorplan, handleUpload, closeUpload, handleDelete, closeDelete}
  }
});

</script>


<style scoped>


.fab {
  position: relative;
  height: 3.75rem;
  width: 3.75rem;
  right: -20px;
  top: 10px;
  margin-top: 45px;
}

.q-card img {
  object-fit: cover;
  filter: grayscale(90%);
}

.my-card {
  width: 200px;
  height: 150px;
  position: relative;
  transition: background-color 0.2s ease-in-out, transform 0.2s ease-in-out;
  background-color: var(--surface);
}

.my-card:hover {
  background-color: var(--q-primary);
  color: white;
  cursor: pointer;
  transform: scale(1.05);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
}

.my-card:hover img {
  filter: grayscale(0%);
}

.my-card-section {
  padding-top: 10px;
  padding-bottom: 10px;
}

.my-text-ellipsis {
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap;
  width: 100%;
}

.my-list-button {
  position: fixed;
  height: 3.75rem;
  width: 3.75rem;
  right: 30px;
  bottom: 70px;
}

.add-building-dialog {
  padding: 10px;
  align-content: center;
  text-align: center;
}

</style>