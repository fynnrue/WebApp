<template>
  <div class="q-mx-xl row items-start q-gutter-md">
    <q-card v-for="building in buildings" :key="building.id" class="my-card" :title="building.name" @click="toBuilding(building.id)" :buildingId="building.id">
      <img :src="building.image" width="100" height="100" :alt="building.name" draggable="false">
      <q-card-section class="text-center my-card-section">
        <div class="text-h6 my-text-ellipsis">{{ building.name }}</div>
      </q-card-section>
    </q-card>
    <RoleFilter :role="Role.Editor">
      <q-btn color="primary" round class="fab" @click="openDialogAdd">
        <q-icon name="add" size="md"/>
        <q-tooltip color="primary">{{ $t("start.add_building") }}</q-tooltip>
      </q-btn>
      <q-btn color="primary" round class="fab" @click="openDialogDelete">
        <q-icon name="delete" size="md"/>
        <q-tooltip color="primary">{{ $t("start.delete_building") }}</q-tooltip>
      </q-btn>
    </RoleFilter>
    <div id="button-container">
      <q-btn @click="toUserCredentialView" color="primary" id="credential-view-button" round>
        <q-icon name="badge" size="md"/>
        <q-tooltip>{{ $t("start.to_user_credential_view") }}</q-tooltip>
      </q-btn>
      <q-btn to="/list" color="primary" class="my-list-button" round>
        <q-icon name="list" size="md"/>
        <q-tooltip>{{ $t("start.to_list_view") }}</q-tooltip>
      </q-btn>
    </div>
  </div>
  <UploadDialogue v-model="dialogVisibleAdd" title="start.add_building" @submit="handleUpload" @closeUploadDialog="closeUpload"/>
  <DeleteDialogue v-model="dialogVisibleDelete" title="start.delete_building" :floors="buildings" @delete="handleDelete" @closeDeleteDialog="closeDelete"/>
</template>
<script lang="ts">
import {defineComponent, Ref, ref} from "vue";
import {addBuilding, deleteBuildingF, fetchBuildings} from "@/main/vue/api/buildingApi";
import {Building} from "@/main/vue/types/building";
import {useI18n} from "vue-i18n"
import {useQuasar} from "quasar";
import {Role} from "@/main/vue/stores/userStore";
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import UploadDialogue from "@/main/vue/components/UploadDialogue.vue";
import DeleteDialogue from "@/main/vue/components/DeleteDialogue.vue";

import router from "@/main/vue/router";
import {deleteRoom} from "@/main/vue/api/floor";

export default defineComponent({
  name: "Card",
  components: {UploadDialogue, RoleFilter, DeleteDialogue},
  data() {
    return {
      buildings: [] as Building[],
      newBuilding: {
        name: "",
        image: ""
      } as Building,
      dialogVisible: false,
    };
  },
  created() {
    try {
      this.fetchBuildings();
    } catch (error: unknown) {
      console.error((error as Error).message);
    }
  },
  setup() {
    const newImage: Ref<File | null> = ref(null)
    const newBuildingName: Ref<string | null> = ref(null)
    const {t} = useI18n()
    const $q = useQuasar()
    const dialogVisibleAdd = ref(false)
    const dialogVisibleDelete = ref(false)

    function notifyError(message: string) {
      $q.notify({
        type: 'negative',
        caption: t('start.upload_failed'),
        message: t(message)
      })
    }

    function openDialogAdd() {
      dialogVisibleAdd.value = true;
      dialogVisibleDelete.value = false;
    }
    function openDialogDelete() {
      dialogVisibleAdd.value = false;
      dialogVisibleDelete.value = true;
    }

    function toUserCredentialView() {
      router.push({path: '/credentials'})
    }

    return {newImage, newBuildingName, t, notifyError, Role, openDialogAdd, openDialogDelete, dialogVisibleAdd, dialogVisibleDelete, toUserCredentialView}
  },
  methods: {
    toBuilding(id: number) {
      router.push({
        name: 'FloorSelection',
        params: {
          buildingId: id.toString(),
        }
      });
     // router.push({ path:'/buildings/' +  id, params: { id: id.toString() }});
    },
    async fetchBuildings() {
      this.$q.loading.show({
        message: this.t("start.loading_buildings"),
        delay: 200,
      });
      try {
        this.buildings = await fetchBuildings();
      } catch (error: unknown) {
        console.error((error as Error).message);
      }
      this.$q.loading.hide()
    },

    async handleUpload({name, image}: { name: string, image: File }) {
      try {
        this.closeUpload();
        await addBuilding(name, image);
      } catch {
        console.error("Failed to upload building");
        this.notifyError("start.upload_failed");
      }
      await this.fetchBuildings();
    },

    closeUpload() {
      this.dialogVisibleAdd = false;
    },

    async handleDelete({id}: {id: number}) {
      try {
        this.closeUpload();
        await deleteBuildingF(BigInt(id));
      } catch {
        console.error("Failed to delete building");
        this.notifyError("delete_dialog.delete_failed");
      }
      await this.fetchBuildings();
    },

    closeDelete() {
      this.dialogVisibleDelete = false;
    }
  },
});

</script>

<style scoped>

#credential-view-button {
  position: relative;
  height: 3.75rem;
  width: 3.75rem;
}

#button-container {
  display: flex;
  flex-direction: column;
  position: fixed;
  bottom: 60px;
  right: 30px;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

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
  position: relative;
  height: 3.75rem;
  width: 3.75rem;
}

.add-building-dialog {
  padding: 10px;
  align-content: center;
  text-align: center;
}

</style>