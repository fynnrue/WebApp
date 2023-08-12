<script lang="ts">
import {defineComponent, onMounted, Ref, ref, watch} from "vue";
import CredentialCard, {CredCard, Predicate, PredicateItem} from "@/main/vue/components/CredentialCard.vue";
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import {Role} from "@/main/vue/stores/userStore";
import {CredentialGroupUnion, CredentialORConnection, PredicateRequirement} from "@/main/vue/types/credentials";
import {useCredentialStore} from "@/main/vue/stores/credentialStore";
import {fetchFloorById, getRoomById, updateRoom} from "@/main/vue/api/floor";
import {Room} from "@/main/vue/types/room";
import {useQuasar} from "quasar";
import {useI18n} from "vue-i18n";
import router from "@/main/vue/router";

export const todayMagic = "$TODAY-YYYYMMDD"
export default defineComponent({
  name: "CredentialView",
  computed: {
    Role() {
      return Role
    }
  },
  props: {
    roomId: {
      type: Number,
      required: true
    }
  },
  components: {RoleFilter, CredentialCard},
  setup(props) {
    const roomId = props.roomId;

    const $q = useQuasar()
    const {t} = useI18n()

    const cards = ref([]) as Ref<CredCard[]>
    const changesMade = ref(false)
    const room = ref(null) as Ref<Room | null>
    const floorName = ref("")
    const buildingName = ref("")
    const isDefaultGroup = ref(false)

    const credentialStore = useCredentialStore()

    const credentialNameMap = new Map<string, CredentialGroupUnion>()

    onMounted(() => {
      $q.loading.show({
        message: t("editor.loading")
      })
      credentialStore.ensureInitialized().then(() => {
        credentialStore.credentialGroupUnions!.forEach((credential) => {
          credentialNameMap.set(credential.name, credential)
        })
      })

      getRoomById(BigInt(roomId)).then(async (r) => {
        r.requiredCredentials.forEach((or) => {
          const predicateMap = new Map<string, PredicateRequirement[]>()

          // Group predicates by name
          or.predicateRequirements.forEach((p) => {
            if (predicateMap.has(p.attributeName)) {
              predicateMap.get(p.attributeName)!.push(p)
            } else {
              predicateMap.set(p.attributeName, [p])
            }
          })
          const predicates: Predicate[] = []
          predicateMap.forEach((value, key) => {
            predicates.push({
              name: key,
              attributes: value.map((p) => ({
                valueType: p.valueType,
                type: p.relation,
                value: p.attributeValue
              } as PredicateItem))
            })
          })

          const card: CredCard = {
            type: {type: "Predicate"},
            credentials: or.credentials.map((c) => c.name),
            predicates: predicates
          }
          cards.value.push(card)
        })
        room.value = r
        const floor = await fetchFloorById(room.value?.floorId! as any)
        floorName.value = floor.name
        buildingName.value = floor.buildingName
        isDefaultGroup.value = floor.defaultRoomGroup.name === room.value?.groupInfo.name
      })
      $q.loading.hide()
    })


    watch(() => changesMade.value, (newVal) => {
      if(newVal) {
        window.onbeforeunload = function () {
          return true;
        };
      } else {
        window.onbeforeunload = null
      }
    });

    function addCard() {
      const emptyCard: CredCard = {
        type: {type: "Predicate"},
        credentials: [],
        predicates: []
      }
      cards.value.push(emptyCard)
      changesMade.value = true
    }

    function deleteCard(idx: number) {
      cards.value.splice(idx, 1)
      changesMade.value = true
    }

    function saveChanges() {
      const orConnections = cards.value.map((card) => ({
        credentials: card.credentials.map((c) => credentialNameMap.get(c)),
        predicateRequirements: card.predicates.flatMap((p) => {

          return p.attributes.map((a) => {

            return {
              attributeName: p.name,
              attributeValue: a.value,
              relation: a.type,
              valueType: a.valueType
            } as PredicateRequirement
          })
        })
      } as CredentialORConnection))
      $q.loading.show({
        message: t("editor.loading"),
        delay: 200
      })
      updateRoom(BigInt(roomId), {
        requiredCredentials: orConnections
      })
      $q.loading.hide()
      changesMade.value = false
      $q.notify({
        message: t("credential_view.updated_requirements"),
        color: 'primary'
      })
    }

    function cancelChanges() {
      changesMade.value = false
      if (window.history.state.back !== null) {
        router.back()
      } else {
        router.push("/")
      }
    }

    return {cards, addCard, deleteCard, saveChanges, cancelChanges, room, buildingName, floorName, isDefaultGroup}
  }

});

</script>

<template>
  <h4 class="q-mx-auto text-center text-responsive">
    {{ buildingName }} - {{ floorName }} - {{ $t("credential_view.room") }} {{ room?.name }}
    <template v-if="!isDefaultGroup">
      - {{ $t("credential_view.room_group") }} {{ room?.groupInfo.name }}
    </template>
  </h4>
  <h4 class="q-mx-auto text-left text-responsive">{{ $t("credential_view.required_credentials") }}</h4>
  <div class="fit justify-start cards-container">
    <div v-for="(card, idx) in cards">
      <CredentialCard v-model:card="cards[idx]" @delete:card="deleteCard(idx)"/>
    </div>
    <RoleFilter :role="Role.Editor">
      <q-btn color="primary" round class="add-fab" @click="addCard">
        <q-icon name="add" size="md"/>
        <q-tooltip color="primary">{{ $t("cred.add_credential") }}</q-tooltip>
      </q-btn>
    </RoleFilter>
  </div>
  <RoleFilter :role="Role.Editor">
    <q-btn round icon="save" size="large" class="bottom-right" color="primary" @click="saveChanges">
      <q-tooltip color="primary">{{ $t("cred.save") }}</q-tooltip>
    </q-btn>
    <q-btn round icon="close" class="bottom-left" color="accent" @click="cancelChanges">
      <q-tooltip color="primary">{{ $t("cred.cancel_save") }}</q-tooltip>
    </q-btn>
  </RoleFilter>
</template>

<style scoped>

.cards-container {
  position: relative;
  display: flex;
  flex-direction: row;
  max-height: 70vh;
  align-items: center;
  align-content: flex-start;
  flex-wrap: wrap;
  flex: 1 1 auto;
  overflow: auto;
  padding: 0 10px 10px 0;
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}

.cards-container::-webkit-scrollbar {
  display: none;
  width: 0;
  background: transparent;
}

.text-responsive {
  font-size: 1.5rem;
  font-weight: 400;
  letter-spacing: 0.03125em;
  line-height: 1.5rem;
  color: var(--on-surface);
}

.add-fab {
  height: 3.75rem;
  width: 3.75rem;
  justify-self: flex-start;
  margin-left: 2vw;
}

.bottom-right {
  position: fixed;
  bottom: 60px;
  right: 30px;
  z-index: 2;
  height: 3.75rem;
  width: 3.75rem;
}

.bottom-left {
  position: fixed;
  bottom: 60px;
  left: 30px;
  z-index: 2;
  height: 3.75rem;
  width: 3.75rem;
}


</style>