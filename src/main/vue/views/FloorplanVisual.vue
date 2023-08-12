<script lang="ts">
import Floorplan from "@/main/vue/components/Floorplan.vue";
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import {Role} from "@/main/vue/stores/userStore";
import {defineComponent, Ref, ref} from "vue";
import RoomInfo, {groupDialogData} from "@/main/vue/components/RoomInfo.vue";
import {RoomLeaflet} from "@/main/vue/types/room";
import {removeRoom} from "@/main/vue/api/floor";
import NameBar from "@/main/vue/components/NameBar.vue";
import router from "@/main/vue/router";

export default defineComponent({
  name: "FloorplanVisual",
  /**
   * Passes props via vue-router from FloorSelection.vue
   */
  props: {
    buildingId: String,
    floorId: String,
  },
  computed: {
    Role() {
      return Role
    }
  },
  data() {
    return {
      rebuildKey: 0
    }
  },
  components: {NameBar, RoomInfo, RoleFilter, Floorplan},
  setup() {
    const editMode = ref(false)
    const showRoomInfo = ref(false);
    const imageUrl = ref("") as Ref<String>;
    const currentRoom = ref() as Ref<RoomLeaflet>;

    const floorplanRef = ref() as Ref<InstanceType<typeof Floorplan>>;
    const roomInfoRef = ref() as Ref<InstanceType<typeof RoomInfo>>;

    const groupDialogToggle = ref(false);
    const groupData = ref() as Ref<groupDialogData>;

    const toggleRoomInfo = () => {
      showRoomInfo.value = !showRoomInfo.value;
    }

    function selected(room: RoomLeaflet) {
      if (!room) {
        showRoomInfo.value = false;
        return;
      }
      currentRoom.value = room;
      showRoomInfo.value = true;
    }

    async function refreshImage(url: string) {
      imageUrl.value = url
    }

    function editModeToggle(value: boolean) {
      editMode.value = value
    }


    function callDeleteRoom(id: bigint) {
      showRoomInfo.value = false;
      removeRoom(id);
      floorplanRef.value.removeLayer(id);
    }

    function groupDialog(groupAddMode:boolean, newGroupName:string, groupColor:string) {
      groupData.value =
      {
        groupAddMode: groupAddMode,
        newGroupName: newGroupName,
        groupColor: groupColor
      }
      groupDialogToggle.value = true;
    }

    function submitGroupDialog() {
      roomInfoRef.value.submitGroup(groupData.value.newGroupName, groupData.value.groupColor, groupData.value.groupAddMode);
      floorplanRef.value.getRooms();
      groupDialogToggle.value = false;
    }

    function toCredView() {
      router.push({name: "credentialView", params: {roomId: Number(roomInfoRef.value.roomInfo.id)}})
    }


    return {
      showRoomInfo, toggleRoomInfo, refreshImage, imageUrl, selected, currentRoom, roomInfoRef, groupData,
      editModeToggle, editMode, callDeleteRoom, floorplanRef, groupDialog,groupDialogToggle, submitGroupDialog, toCredView
    }
  },
});

</script>
<template>
  <div class="outerBox">
    <NameBar class="nameBar" :building-id="Number(buildingId)" :floor-id="Number(floorId)"
             @refresh-image="refreshImage"/>
    <Floorplan class="floorplan"
               @roomSelected="selected"
               @editModeToggle="editModeToggle"
               :floor-id="floorId!"
               :new-image-url="imageUrl"
               ref="floorplanRef"/>
  </div>
  <transition name="slide" appear>
    <RoomInfo v-if="showRoomInfo" class="roominfo" :room="currentRoom" ref="roomInfoRef"
              :editMode="editMode"
              @repaint="floorplanRef.getRooms()"
              @delete-room="callDeleteRoom"
              @openGroupEditDialog="groupDialog"
              @toCredentialView="toCredView"
    />
  </transition>

  <q-dialog v-model="groupDialogToggle">
    <q-card>
      <q-bar class="dialog-bar">
        <div class="text-h4">{{
            $t(groupData.groupAddMode ? "room_info.add_room_group" : "room_info.edit_room_group")
          }}
        </div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer" v-close-popup/>
      </q-bar>

      <q-card-section>
        <q-input :label="$t('start.name')" v-model="groupData.newGroupName"/>

        <q-input
            filled
            v-model="groupData.groupColor"
            class="my-input"
        >
          <template v-slot:append>
            <q-icon name="colorize" class="cursor-pointer">
              <q-popup-proxy cover>
                <q-color v-model="groupData.groupColor"/>
              </q-popup-proxy>
            </q-icon>
          </template>
        </q-input>
      </q-card-section>

      <q-card-section class="row" style="display: flex; justify-content: space-between">
        <q-btn :label="$t('delete_dialog.cancel')" color="primary" v-close-popup/>
        <q-btn :label="$t('delete_dialog.confirm')" color="primary" @click="submitGroupDialog"/>
      </q-card-section>

    </q-card>
  </q-dialog>
</template>

<style scoped>

.outerBox {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: fit-content;
  margin: 0 auto;
  align-items: center;
}

.roominfo {
  position: fixed;
  width: 30%;
  height: 100%;
  right: 0;
  z-index: 1000;
}

/* are used, but IDE doesn't recognize it */
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease-in-out;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}

.dialog-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  background-color: var(--surface);
  height: 40px;
}

</style>
