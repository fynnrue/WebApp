<script lang="ts">

import {defineComponent, onMounted, ref} from "vue";
import {fetchBuilding} from "@/main/vue/api/buildingApi";
import {fetchFloorById, newImageAndName, updateImage, updateName} from "@/main/vue/api/floor";
import UploadDialogue from "@/main/vue/components/UploadDialogue.vue";
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import {Role} from "@/main/vue/stores/userStore";

export default defineComponent({

  name: "NameBar",
  computed: {
    Role() {
      return Role
    }
  },
  components: {RoleFilter, UploadDialogue},

  data() {
    return {
      dialog: false,
    }
  },

  emits: ['refreshImage'],

  props: {
    buildingId: Number,
    floorId: Number,
  },

  setup(props, {emit}) {
    const buildingName = ref('');
    const floorName = ref('');
    const dialog = ref(false);

    onMounted(async () => {
      let newFloorName
      try {
        buildingName.value = (await fetchBuilding(BigInt(props.buildingId as number))).name;
        newFloorName = (await fetchFloorById(props.floorId as number)).name;
      } catch (error: unknown) {
        console.error((error as Error).message);
        newFloorName = "Error";
      }
      setFloorName(newFloorName);
    });

    function setFloorName(newFloorName: string) {
      floorName.value = newFloorName;
    }

    function changeImageAndName() {
      dialog.value = true;
    }

    async function updateInformation({name, image}: {name: string, image: File}) {
      closeDialogue()
      if (name != null && image != null) {
        await newImageAndName(props.floorId as number, name, image)
        setFloorName(name)
        const imageUrl = (await fetchFloorById(props.floorId as number)).image
        emit('refreshImage', imageUrl)
      }
      if (image == null) {
        await updateName(props.floorId as number, name)
        setFloorName(name)
      }
      if (name == null) {
        await updateImage(props.floorId as number, image)
        const imageUrl = (await fetchFloorById(props.floorId as number)).image
        emit('refreshImage', imageUrl)
      }
    }

    function closeDialogue() {
      dialog.value = false;
    }

    return {
      buildingName,
      floorName,
      changeImageAndName,
      dialog,
      updateInformation,
      closeDialogue,
      };
  },
})




</script>

<template>
  <div id="outerBox">
    <div id="buildingNameBox" class="text-h4">{{ buildingName }} - {{ floorName }}</div>
    <RoleFilter :role="Role.Editor">
    <q-btn flat icon="edit" :ripple="false" size="large" class="changeButton" @click="changeImageAndName"/>
    </RoleFilter>
  </div>
  <UploadDialogue v-model="dialog" title="editor.tools.changeImageAndName" :require-name-and-image="false" @submit="updateInformation" @close-upload-dialog="closeDialogue" />
</template>

<style scoped>
#outerBox {
  display: flex;
  flex-direction: row;
  height: fit-content;
  width: fit-content;
  margin: 1rem auto;
  align-items: center;
}
#buildingNameBox {
  display: flex;
  justify-content: center;
  align-items: center;
  height: fit-content;
  width: fit-content;
  text-align: center;
  padding: 0 0 0 0;
}

.changeButton {
  align-items: center;
  transition: color 0.2s ease-in-out, scale 0.2s ease-in-out;
}

.changeButton:hover {
  color: var(--q-primary);
  transform: scale(1.1);
  transition: color 0.2s ease-in-out, scale 0.2s ease-in-out;
}
</style>