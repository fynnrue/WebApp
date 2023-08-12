<script lang="ts">
import {computed, defineComponent, Ref, ref, toRefs, watch} from "vue";
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import {Role, userStore} from "@/main/vue/stores/userStore";
import {RoomInfoBar} from "@/main/vue/types/room";
import {useI18n} from "vue-i18n";
import UploadDialogue from "@/main/vue/components/UploadDialogue.vue";
import {RoomGroupInfoData} from "@/main/vue/types/roomGroup";
import {
  addRoomGroup,
  fetchRoomGroups,
  getDoors,
  hexColorFromInt,
  intFromHexColor,
  updateRoom,
  updateRoomGroup
} from "@/main/vue/api/floor";
import {useQuasar} from "quasar";

export type groupDialogData = {
  groupAddMode: boolean,
  newGroupName: string,
  groupColor: string
}
export default defineComponent({
  name: "RoomInfo",
  computed: {
    Role() {
      return Role
    }
  },
  components: {UploadDialogue, RoleFilter},
  props: {
    room: {
      type: Object,
      required: false
    },
    editMode: {
      type: Boolean,
      required: false
    }
  },
  emits: ["addCredential", "repaint", "deleteRoom", "openGroupEditDialog", "toCredentialView"],
  setup(props, emits) {
    const $q = useQuasar();
    const roomInfo = ref() as Ref<RoomInfoBar>;
    const propsRef = toRefs(props);
    const groupDialog = ref(false) as Ref<boolean>
    const groupAddMode = ref(false) as Ref<boolean>
    const groupColor = ref("#000000") as Ref<string>
    const newGroupName = ref("") as Ref<string>

    const credentialDialog = ref(false) as Ref<boolean>;
    roomInfo.value = props.room as RoomInfoBar;

    const {t} = useI18n();
    //const selectedDoorIndex = ref() as Ref<number>;
    const selectedCredentialIndex = ref() as Ref<number>;

    /**
     * If editMode is not set, set it to false
     */
    const editModeRef = ref(false) as Ref<boolean>;
    editModeRef.value = propsRef.editMode ? propsRef.editMode.value : false;
    const store = userStore()
    const user = computed(() => store.currentUser)

    const floorGroups = ref(null) as Ref<Map<string, RoomGroupInfoData | null> | null>
    const groupNames = ref([]) as Ref<string[]>
    const filteredGroupNames = ref(groupNames.value) as Ref<string[]>
    const currentGroup = ref(roomInfo.value.groupInfo.name) as Ref<string>
    const allDoors = ref([]) as Ref<string[]>
    const filteredDoors = ref(allDoors.value) as Ref<string[]>

    getDoors().then((doors) => {
      allDoors.value = doors
      filteredDoors.value = doors
    })

    function updateRoomGroups() {
      $q.loading.show({
        message: t("editor.loading"),
        delay: 200
      })
      fetchRoomGroups(roomInfo.value.floorId as any).then((groups) => {
        const map = new Map<string, RoomGroupInfoData | null>()
        groups.forEach((g) => {
          map.set(g.name, g)
        })
        groupNames.value = groups.map((g) => g.name)
        floorGroups.value = map
        filteredGroupNames.value = groupNames.value
      })
      $q.loading.hide()
    }

    updateRoomGroups()

    watch(currentGroup, (_, newGroup) => {
      roomInfo.value.groupInfo.name = newGroup
    })

    watch(() => props.room, (newRoom) => {
      roomInfo.value = newRoom as RoomInfoBar;
    });

    watch(() => props.editMode, (newEditMode) => {
      editModeRef.value = newEditMode;
    });

    function toCredView() {
      emits.emit("toCredentialView")
    }

    watch(currentGroup, (_, newName) => {
      updateGroup(newName)
    })

    function selectCredential(idx: number) {
      selectedCredentialIndex.value = idx;
    }

    function deleteRoom(roomId: bigint) {
      emits.emit("deleteRoom", roomId)
    }

    async function submitGroup(newGroupName: string, groupColor:string, groupAddMode:boolean) {
      try {
        $q.loading.show({
          message: t("editor.loading")
        })
        if (groupAddMode) {
          const group = await addRoomGroup(roomInfo.value!.floorId, newGroupName, groupColor)
          groupNames.value.push(group.name)
          floorGroups.value?.set(group.name, group)
        } else {
          const oldGroup = floorGroups.value?.get(currentGroup.value)
          roomInfo.value.groupInfo = await updateRoomGroup({
            name: newGroupName,
            color: intFromHexColor(groupColor)
          }, oldGroup!.id)
          currentGroup.value = roomInfo.value.groupInfo.name
          updateRoomGroups()
        }
      } catch (e) {
        console.error(e)
      } finally {
        groupDialog.value = false
      }
      $q.loading.hide()
    }
    function openGroupEditDialog() {
      groupAddMode.value = false
      groupDialog.value = true
      newGroupName.value = currentGroup.value
      groupColor.value = hexColorFromInt(roomInfo.value.groupInfo.color)
      emits.emit("openGroupEditDialog", groupAddMode, newGroupName, groupColor)
    }

    function openGroupCreateDialog() {
      groupAddMode.value = true
      groupDialog.value = true
      newGroupName.value = ""
      groupColor.value = "#000000"

      emits.emit("openGroupEditDialog", groupAddMode, newGroupName, groupColor)
    }

    async function updateGroup(name: string) {
      $q.loading.show({
        message: t("editor.loading")
      })
      try {
        await updateRoom(roomInfo.value.id, {
          roomGroupId: floorGroups.value?.get(currentGroup.value)?.id
        })
      } catch (e) {
        console.error(e)
      }
      $q.loading.hide()
      emits.emit("repaint")
    }

    function filterGroupNames(val: string, update: any) {
      update(() => {
        filteredGroupNames.value = groupNames.value.filter((name) => name.toLowerCase().includes(val.toLowerCase()))
      })
    }

    function emitAddCredential(idx: number) {
      emits.emit("addCredential", idx)
    }

    function filterDoors(val: string, update: any) {
      update(() => {
        filteredDoors.value = allDoors.value.filter(
            (name) =>
                !roomInfo.value.doors.includes(name) &&
                name.toLowerCase().includes(val.toLowerCase())
        )
      })
    }

    function removeCredential() {
    }

    async function updateDoors() {
      await updateRoom(roomInfo.value.id, {
        doors: roomInfo.value.doors
      })
    }

    async function updateName() {
      // Unfocus the input field
      (document.activeElement as HTMLElement | null)?.blur()
      $q.loading.show({
        message: t("editor.loading")
      })
      await updateRoom(roomInfo.value.id, {
        name: roomInfo.value.name
      })
      $q.loading.hide()
    }

    async function updateDescription() {
      // Unfocus the input field
      (document.activeElement as HTMLElement | null)?.blur()
      $q.loading.show({
        message: t("editor.loading")
      })
      await updateRoom(roomInfo.value.id, {
        description: roomInfo.value.description
      })
      $q.loading.hide()
    }

    function removeDoor(door: string) {
      roomInfo.value.doors.splice(roomInfo.value.doors.indexOf(door, 1))
      updateDoors()
    }

    function createDoor(door: string, _: any) {
      roomInfo.value.doors.push(door)
      updateDoors()
    }

    return {
      roomInfo,
      emitAddCredential, removeCredential,
      deleteRoom, selectCredential, t,
      credentialDialog,
      user,
      floorGroups,
      currentGroup,
      submitGroup,
      groupDialog,
      groupColor,
      groupNames,
      newGroupName,
      filteredGroupNames,
      filterGroupNames,
      editModeRef,
      toCredView,
      allDoors,
      filterDoors,
      filteredDoors,
      updateDoors,
      removeDoor,
      createDoor,
      updateName,
      groupAddMode,
      openGroupEditDialog,
      openGroupCreateDialog,
      updateDescription
    }
  }
})

</script>

<template>
  <!-- Sidebar -->
  <div class="outer-container col items-center content-center justify-center shadow-5">

      <!-- Header Room Name -->
      <div class="row center-align">
        <q-input class="header" style="font-size: 1.8rem" id="nameTextBox" @keydown.enter.prevent="updateName()" @submit="updateName()" v-model="roomInfo.name" v-if="editModeRef" />
        <p class="header overflow-hidden ellipsis" v-else >{{ roomInfo.name }}
        <q-tooltip>
          <div class="text-body2">{{ roomInfo.name }}</div>
        </q-tooltip>
        </p>
      </div>
    <div>
      <q-input class="description" @keydown.enter.prevent="updateDescription()" @submit="updateDescription()" v-model="roomInfo.description" v-if="editModeRef"></q-input>
      <p class="text-subtitle1 q-mb-md description" v-else>{{roomInfo.description}}
        <q-tooltip>
          <div class="text-body2">{{ roomInfo.description }}</div>
        </q-tooltip>
      </p>
    </div>

    <div class="col info" style="width: fit-content">
      <div style="word-wrap: break-word">{{ t("room_info.group") }}:</div>
      <div style="width: 100%; display: flex; flex-direction: row">
        <q-select :disable="!user?.roles.includes(Role.Editor) || !editModeRef"
                  class="select-size"
                  use-input
                  v-model="currentGroup" :options="filteredGroupNames"
                  @filter="filterGroupNames" input-debounce="0"
                  style="width:100%;">
          <template v-slot:selected-item="scope">
            <div class="select-break">{{scope.opt}}</div>
          </template>
          <template v-slot:after v-if="editModeRef">
            <q-btn flat dense :ripple="false" icon="edit" @click="openGroupEditDialog()"/>
            <q-btn flat dense :ripple="false" icon="add" @click="openGroupCreateDialog()"/>
          </template>

        </q-select>
        <RoleFilter :role="Role.Editor" v-if="editModeRef" style="display: flex; flex-direction: column">


        </RoleFilter>

      </div>
      <q-space/>
    </div>

    <div class="col info-margin info">
      <p>{{ t("room_info.credentials") }}:</p>
      <div class="list-container scroll">
        <q-list dense class="small-list">
          <q-item dense v-for="(credentialORConnection, group_index) in roomInfo.requiredCredentials" :key="group_index"
                  :clickable="false" style="padding: 5px 0 0 0">
            <q-list dense class="small-list credentials">
              <q-item dense v-for="(credential, index) in credentialORConnection.credentials" :key="index" clickable v-ripple>
                <q-item-section class="hide-scrollbar item-text" @click="selectCredential(0)">
                  {{ credential.name }}
                </q-item-section>
                <div class="text-uppercase text-right right-or q-mr-md" v-if="index < credentialORConnection.credentials.length-1">OR
                </div>
              </q-item>
              <div class="q-mx-md" v-if="group_index < roomInfo.requiredCredentials.length-1">
                <div class="text-line text-body1 on-surface text-uppercase">AND</div>
              </div>
            </q-list>
          </q-item>
        </q-list>
      </div>
      <p class="text-subtitle2 text-center detailed-view" @click="toCredView">{{ $t('room_info.detailedView') }}</p>
    </div>

    <div class="col info-margin">
      <p>{{ t("room_info.doors") }}:</p>
      <div class="list-container scroll">
        <q-list dense class="small-list">
          <q-item v-for="door in roomInfo.doors" :key="door" clickable v-ripple>
            <q-item-section>{{ door }}</q-item-section>
            <RoleFilter :role="Role.Editor" v-if="editModeRef">
              <q-btn flat dense :ripple="false" icon="delete" size="sm" @click="removeDoor(door)"/>
            </RoleFilter>
          </q-item>
          <q-item v-if="editModeRef">
            <q-item-section>
              <q-select
                  v-model="roomInfo.doors"
                  :options="filteredDoors"
                  @new-value="createDoor"
                  use-input
                  @update:model-value="updateDoors"
                  @filter="filterDoors"
                  style="width: 100%"
                  multiple>
                <template v-slot:label>
                  Add Doors
                </template>
                <template v-slot:selected>

                </template>
              </q-select>
            </q-item-section>
          </q-item>
        </q-list>

      </div>
    </div>

    <RoleFilter :role="Role.Editor" v-if="editModeRef">
      <q-btn class="delete-button" color="primary" :label="t('room_info.delete_room')"
             @click="deleteRoom(roomInfo.id)"/>
    </RoleFilter>

    <!-- cheat for for scrolling -->
    <div style="margin-bottom: 70px"></div>

    <q-space/>
  </div>
</template>

<style scoped>

.outer-container {
  position: fixed;
  padding-left: 1rem;
  padding-right: 1rem;
  right: 0;
  top: 70px;
  margin-right: 0 !important;
  height: calc(100% - 100px) !important;
  max-width: fit-content;
  min-width: 150px;
  background-color: var(--surface);
  opacity: 0.9;
  z-index: 1000;
  overflow-y: auto;
  margin-bottom: 50px;
  overflow-x: hidden;
}

.text-line {
  overflow: hidden;
  text-align: center;
  user-select: none;
  opacity: 0.5;
}

.text-line:before,
.text-line:after {
  background-color: var(--on-surface);
  content: "";
  display: inline-block;
  height: 1px;
  position: relative;
  vertical-align: middle;
  width: 50%;
}

.text-line:before {
  right: 0.5em;
  margin-left: -50%;
}

.text-line:after {
  left: 0.5em;
  margin-right: -50%;
}

.right-or {
  opacity: 0.5;
  right: 0;
  position: absolute;
  user-select: none;
}

.header {
  max-width: 11rem;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 15px;
  font-size: 1.8rem;
  text-align: center;
  margin-bottom: 0;
}

.item-text {
  position: absolute;
  max-width: fit-content;
  white-space: nowrap;
  overflow: auto;
  text-overflow: fade;
}

.hide-scrollbar::-webkit-scrollbar {
  display: none;
  overflow: hidden;
  width: 100%;
  height: 100%;
}

.info {
  font-size: 1rem;
}

.list-container {
  width: 100%;
  height: 22vh;
  max-width: 220px;
  overflow-y: auto;
  font-size: 1rem;
  scrollbar-width: none;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.5);
}

.delete-button {
  margin-left: 18%;
  margin-top: 10px;
}

.list-container.scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

.list-container.scroll {
  scrollbar-width: none;
  -ms-overflow-style: none;
  overflow-y: scroll;
}

.credentials {
  display: table-row;
  flex-wrap: nowrap;
  width: 100%;
}

.detailed-view {
  margin: 0 auto 0 auto;
  user-select: none;
  cursor: pointer;
  color: var(--q-primary);
}

.detailed-view:hover {
  text-decoration: underline;
}

.info-margin {
  margin-top: 1rem;
}

.single-line-value-select .q-field__native {
  flex-wrap: nowrap;
}
.single-line-value-select .q-field__native > span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}


.select-size {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  max-width: 220px;
  max-height: 22vh;
}

.select-break {
  max-width: fit-content;
  max-height: fit-content;
  text-overflow: ellipsis;
  white-space: break-spaces;
  overflow-wrap: break-word;
  overflow: hidden;
}

.description {
  font-style: italic;
  font-size: 1rem;
  max-width: 12rem;
  overflow: hidden;
  word-break: break-word;
  text-overflow: ellipsis;
}


</style>
