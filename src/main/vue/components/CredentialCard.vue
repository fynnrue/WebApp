<script lang="ts">
import {defineComponent, Ref, ref, watch} from "vue";
import {useQuasar} from "quasar";
import {todayMagic} from "@/main/vue/views/CredentialView.vue";
import {useI18n} from "vue-i18n";
import EventOnHold from "@/main/vue/components/EventOnHold.vue";
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import {Role} from "@/main/vue/stores/userStore";
import {useCredentialStore} from "@/main/vue/stores/credentialStore";
import {CredentialGroupUnion} from "@/main/vue/types/credentials";

export type cardType = {
  type: "Attribute" | "Predicate",
}
// attributes are type + value
export type PredicateItem = {
  valueType: PredValueType, // Number, Date, Today
  type: AttributeRelation, // >, <, >=, <=, =
  value: string // 2021-06-01
}

export type Predicate = {
  name: string, // expiration_date
  attributes: PredicateItem[] // values to check TODAY
}

export type CredCard = {
  type: cardType, // Predicate, Attribute
  credentials: string[],
  predicates: Predicate[]
}

// enum for pred format (Either number or Date format)
export enum PredValueType {
  Number = "Number",
  Date = "Date",
  Today = "Today"
}

// enum for pref types ("=", "<", ">", "<=", ">=")
export enum AttributeRelation {
  Equal = "=",
  LessThan = "<",
  GreaterThan = ">",
  LessThanOrEqual = "<=",
  GreaterThanOrEqual = ">="
}

export default defineComponent({
  name: "CredentialCard",
  computed: {
    Role() {
      return Role
    }
  },
  components: {RoleFilter, EventOnHold},
  props: {
    card: {
      type: Object as () => CredCard,
      required: true
    }
  },
  emits: ['update:card', 'delete:card'],
  setup(props, emits) {
    const {t} = useI18n();
    const $q = useQuasar()
    const thisCard = ref(props.card) as Ref<CredCard>
    const dialogPred = ref(false)
    const dialogAddCred = ref(false)

    const credentialStore = useCredentialStore()

    const allCreds = ref([]) as Ref<string[]>

    const availableCreds = ref([] as String[])
    const selected = ref(null)

    const allPredTypes = ref(Object.values(AttributeRelation))
    //const selectedPredType = ref(PredType.GreaterThan) as Ref<PredType>

    const allPredValueTypes = ref(Object.values(PredValueType))
    //const selectedPredValueType = ref(PredValueType.Date) as Ref<PredValueType>

    const allAttributes = ref([]) as Ref<string[]>

    const credentialNameMap = ref(null) as Ref<Map<string, CredentialGroupUnion> | null>

    credentialStore.ensureInitialized().then(() => {
      allCreds.value = credentialStore.credentialGroupUnions!.map((e) => e.name)
      credentialNameMap.value = new Map<string, CredentialGroupUnion>()
      credentialStore.credentialGroupUnions!.forEach((c) => {
        credentialNameMap.value?.set(c.name, c)
      })
      updateAttributes()
    })

    const selectedAttribute = ref("expiration_date") as Ref<String>

    const defaultPredSettings = [{
      valueType: PredValueType.Date,
      type: AttributeRelation.GreaterThan,
      value: ""
    }] as PredicateItem[]
    const predSettings = ref(defaultPredSettings)
    const predicateIndex = ref(0)

    const editMode = ref(false)

    function updateAttributes() {
      const credentials = thisCard.value.credentials.map((c) => credentialNameMap.value?.get(c))

      // Set attributes to intersection of attributes from selected credentials
      const attributeSets = credentials.map((c) => new Set(c?.attributes))
      allAttributes.value = (credentials[0]?.attributes ?? []).filter((a) => attributeSets.every((s) => s.has(a)))
      thisCard.value.predicates = thisCard.value.predicates.filter((p) => allAttributes.value.includes(p.name))
    }

    watch(() => props.card, (newVal) => {
      thisCard.value = newVal
    });

    // Functions to open dialogs
    function callDialogPred() {
      editMode.value = false
      predSettings.value = [{
        valueType: PredValueType.Date,
        type: AttributeRelation.GreaterThan,
        value: ""
      }] as PredicateItem[]
      dialogPred.value = true
    }

    function onEditPred(idx: number) {
      editMode.value = true
      predicateIndex.value = idx
      selectedAttribute.value = thisCard.value.predicates[idx].name
      predSettings.value = JSON.parse(JSON.stringify(thisCard.value.predicates[idx].attributes))
      dialogPred.value = true
    }

    function callDialogCred() {
      dialogAddCred.value = true
    }

    function closeDialog() {
      dialogPred.value = false
      dialogAddCred.value = false
      selected.value = null
    }

    // On Submit of Credential Dialog
    function addCred() {
      if (selected.value === null) {
        return
      }
      thisCard.value.credentials.push(selected.value)
      updateAttributes()
      closeDialog()
    }

    function checkInputEmpty() {
      let empty = false
      predSettings.value.forEach(setting => {
        if (setting.value === "") {
          empty = true;
        }
      })
      return empty;
    }

    // Submit of Predicate Dialog
    function submitPred() {
      if (checkInputEmpty()) {
        $q.notify({
          type: 'negative',
          message: t('error.empty_input'),
          timeout: 1000
        });
        return;
      }

      if (editMode.value) {
        editPred()
      } else {
        addingPred()
      }
      closeDialog()
    }

    function editPred() {
      thisCard.value.predicates[predicateIndex.value].attributes = []
      predSettings.value.forEach(setting => {
        thisCard.value.predicates[predicateIndex.value].attributes.push({
          valueType: setting.valueType,
          type: setting.type,
          value: setting.value
        })
      })
    }

    function addingPred() {
      predSettings.value.forEach(setting => {
        let found = false
        thisCard.value.predicates.forEach((pred, idx) => {
          if (pred.name === selectedAttribute.value) {
            pred.attributes.push({
              valueType: setting.valueType,
              type: setting.type,
              value: setting.value
            })
            found = true
          }
        })
        if (!found) {
          thisCard.value.predicates.push({
            name: selectedAttribute.value,
            attributes: [{
              valueType: setting.valueType,
              type: setting.type,
              value: setting.value
            }]
          } as Predicate)
        }
      })
    }

    function deletePredSetting(idx: number) {
      predSettings.value.splice(idx, 1)
    }

    function addPredSetting() {
      const newSetting = {
        valueType: PredValueType.Date,
        type: AttributeRelation.GreaterThan,
        value: ""
      }
      predSettings.value.push(newSetting)
    }

    function updateValueType(idx: number) {
      predSettings.value[idx].value =
          predSettings.value[idx].valueType == PredValueType.Today ? todayMagic : "";
    }

    function removeCred(idx: number) {
      thisCard.value.credentials.splice(idx, 1)
      updateAttributes()
    }

    function filterCreds(val: String, update: Function) {
      if (val === '') {
        update(() => {
          availableCreds.value = allCreds.value.filter(v => !thisCard.value.credentials.includes(v))
        })
        return
      }
      update(() => {
        const needle = val.toLowerCase()
        availableCreds.value = allCreds.value.filter(v => v.toLowerCase().indexOf(needle) > -1)
      })
    }

    function onDeleteCard() {
      emits.emit('delete:card', thisCard.value)
    }

    function predAttrToString(attr: String) {
      if (attr === todayMagic) return new Date().toISOString().slice(0, 10)
      return attr
    }

    function deletePredicate() {

      // delete selectedPredicate from thisCard
      thisCard.value.predicates.splice(predicateIndex.value, 1)
      closeDialog()
    }

    return {
      thisCard,
      dialogPred,
      dialogAddCred,
      availableCreds,
      selected,
      predSettings,
      selectedAttribute,
      allPredTypes,
      allPredValueTypes,
      allAttributes,
      callDialogPred,
      callDialogCred,
      closeDialog,
      addCred,
      removeCred,
      filterCreds,
      submitPred,
      addPredSetting,
      deletePredSetting,
      PredValueType,
      updateValueType,
      predAttrToString,
      onDeleteCard,
      onEditPred,
      deletePredicate,
      editMode
    }
  }
});

</script>

<template>
  <!--Adding a Credential Requirement (The top part of the card) -->
  <q-dialog v-model="dialogAddCred" class="dialog">
    <q-card style="background-color: var(--background)">
      <q-bar class="dialog-bar">
        <div class="text-h4">{{ $t('cred.adding_credential') }}</div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer" @click="closeDialog"/>
      </q-bar>
      <q-card-section>
        <q-select
            filled
            v-model="selected"
            use-input
            input-debounce="0"
            label="Select a credential"
            @filter="filterCreds"
            :options="availableCreds"></q-select>
      </q-card-section>
      <q-card-actions align="right">
        <q-btn label="Cancel" @click="closeDialog" color="accent" class="q-tab--no-caps"/>
        <q-btn label="Submit" @click="addCred" color="primary" class="q-tab--no-caps"/>
      </q-card-actions>
    </q-card>
  </q-dialog>

  <!--Adding a Predicate Requirement (The bottom part of the card) -->
  <q-dialog v-model="dialogPred" class="pred-dialog">
    <q-card style="background-color: var(--background)">
      <q-bar class="dialog-bar">
        <div class="text-h4">{{ $t('cred.adding_predicate') }}</div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer" @click="closeDialog"/>
      </q-bar>
      <q-card-section>
        <q-select
            :disable="editMode"
            filled
            v-model="selectedAttribute"
            label="Select an Attribute"
            :options="allAttributes"
            class="fit q-mr-md"
        >
        </q-select>
      </q-card-section>
      <q-card-section>
        <div class="row justify-start">
          <q-item-label class="q-mb-sm">
            {{ $t('cred.pred_settings') }}
          </q-item-label>
          <q-space></q-space>
          <q-icon name="o_info" size="sm" class="q-hoverable cursor-pointer self-center">
            <q-tooltip class="info" anchor="top middle" self="bottom middle">{{
                $t('cred.pred_settings_tooltip')
              }}
            </q-tooltip>
          </q-icon>
        </div>
        <div v-for="(predSetting, idx) in predSettings"
             class="fit justify-start no-wrap row items-start content-start">
          <div class="row">
            <q-select
                filled
                v-model="predSetting.valueType"
                label="Type"
                :options="allPredValueTypes"
                class="q-mr-md"
                @update:model-value="updateValueType(idx);"
                style="width: 100px; max-width: 130px; margin-bottom: 1%;"
            >
            </q-select>
            <q-select
                filled
                v-model="predSetting.type"
                :options="allPredTypes"
                class="q-mr-md"
                style="width: 70px; max-width: 70px;"
            >
            </q-select>
            <div class="row">
            <q-input type="number" step="any" v-if="predSetting.valueType == PredValueType['Number']" label="Value"
                     v-model="predSetting.value" class="input-pred">
            </q-input>
            <q-input v-if="predSetting.valueType == PredValueType['Today']" label="Value" v-model="predSetting.value"
                     :disable="true" style="padding:0; min-width: 45%;" class="input-pred">
            </q-input>
            <q-input class="input-pred"
                     v-if="predSetting.valueType == PredValueType['Date']" label="Value"
                     v-model="predSetting.value" mask="####-##-##">
              <template v-slot:append>
                <q-icon name="event" style="opacity: 100%" class="cursor-pointer delete">
                  <q-popup-proxy cover>
                    <q-date mask="YYYY-MM-DD" v-model="predSetting.value">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup label="Close" color="primary" flat/>
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
            <q-icon v-if="idx != 0" name="delete" size="sm" class="cursor-pointer self-center no-wrap delete-setting"
                    @click="deletePredSetting(idx)" style="padding: 0;"/>
            </div>
          </div>
        </div>
      </q-card-section>
      <q-card-section>
        <q-icon name="add" size="sm" class="cursor-pointer self-center add-icon" @click="addPredSetting"/>
      </q-card-section>
      <q-card-actions class="row">
        <q-btn v-if="editMode" :label="$t('cred.delete')" flat @click="deletePredicate" color="on-surface"
               class="q-tab--no-caps delete-pred"/>
        <q-space></q-space>
        <q-btn label="Cancel" @click="closeDialog" color="accent" class="q-tab--no-caps"/>
        <q-btn label="Submit" @click="submitPred" color="primary" class="q-tab--no-caps"/>
      </q-card-actions>
    </q-card>
  </q-dialog>

  <!--The card itself -->
  <q-card class="cred-card shadow-5">
    <div class="row">
      <q-item-label class="connection-type text-overline">{{ $t('cred.or_connection') }}</q-item-label>
      <q-space></q-space>
      <q-item-label class="card-type text-overline">{{ thisCard.type.type }}</q-item-label>
    </div>
    <q-card-section>
      <div v-for="(cred, idx) in thisCard.credentials" :key="cred">
        <div class="row">
          <q-item-label class="text-h6"> {{ cred }}</q-item-label>
          <RoleFilter :role="Role.Editor">
            <q-icon name="delete" class="delete" size="20px" @click="removeCred(idx)"/>
          </RoleFilter>
        </div>
      </div>
      <RoleFilter :role="Role.Editor">
        <q-item-label class="add-text" @click="callDialogCred">{{ $t('cred.add') }}</q-item-label>
      </RoleFilter>
    </q-card-section>
    <div class="q-mx-md">
      <div class="text-line text-body1 on-surface">predicates</div>
    </div>
    <q-card-section>
      <div v-for="(pred,idx) in thisCard.predicates" :key="pred">
        <div class="row Predicate-title">
          <q-item-label class="text-h6"> {{ pred.name }}</q-item-label>
          <RoleFilter :role="Role.Editor">
            <q-icon name="edit" class="delete" size="20px" @click="onEditPred(idx)"/>
          </RoleFilter>
        </div>
        <div v-for="Attribute in pred.attributes">
          <div class="row">
            <q-item-label class="text-body1 Predicate"> {{ Attribute.type }}</q-item-label>
            <q-item-label class="text-body1 pred-attributes"> {{ predAttrToString(Attribute.value) }}</q-item-label>
          </div>
        </div>
      </div>
      <RoleFilter :role="Role.Editor">
        <q-item-label class="add-text" @click="callDialogPred">{{ $t('cred.add') }}</q-item-label>
        <event-on-hold @finished="onDeleteCard" tooltip="cred.delete_card"></event-on-hold>
      </RoleFilter>
    </q-card-section>
  </q-card>
</template>

<style scoped>
.text-line {
  overflow: hidden;
  text-align: center;
  user-select: none;
}

.text-body1 {
  font-size: 1rem;
  font-weight: 400;
  letter-spacing: 0.03125em;
  line-height: 1.5rem;
  opacity: 50%;
  color: var(--on-surface);
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


.card-type {
  text-align: right;
  padding-right: 3%;
  padding-top: 3%;
  font-style: italic;
}

.connection-type {
  text-align: left;
  padding-left: 3%;
  padding-top: 3%;
  font-style: italic;
}

.cred-card {
  min-width: 20em;
  min-height: 10em;
  margin: 1em;
  border-radius: 15px;
  transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
  overflow: auto;
  background-color: var(--background);
}


.cred-card:hover {
  box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.5);
  transform: scale(1.05);

  transition: box-shadow 0.3s ease-in-out, transform 0.3s ease-in-out;

}

.delete {
  margin-left: auto;
  margin-right: 0;
  opacity: 50%;
  cursor: pointer;
  transition: opacity 0.3s ease-in-out, transform 0.3s ease-in-out, color 0.3s ease-in-out;
}

.delete:hover {
  opacity: 100%;
  transform: scale(1.05);
  color: var(--q-primary);
  transition: opacity 0.3s ease-in-out, transform 0.3s ease-in-out, color 0.3s ease-in-out;
}

.delete-setting {
  margin-left: 1vw;
  margin-right: 0;
  opacity: 50%;
  cursor: pointer;
  transition: opacity 0.3s ease-in-out, transform 0.3s ease-in-out, color 0.3s ease-in-out;
}

.delete-setting:hover {
  opacity: 100%;
  transform: scale(1.05);
  color: var(--q-primary);
  transition: opacity 0.3s ease-in-out, transform 0.3s ease-in-out, color 0.3s ease-in-out;
}

.add-text {
  color: var(--q-primary);
  text-align: center;
  cursor: pointer;
  width: fit-content;
  margin: auto;
  user-select: none;
  padding-top: 3%;
  transition: filter 0.3s ease-in-out;
}

.add-text:hover {
  filter: drop-shadow(0 0 0.75rem var(--q-primary)) brightness(1.5);
  transition: filter 0.3s ease-in-out;
}

.pred-attributes {
  padding-left: 3%;
  font-style: italic;
  margin: 0;
  max-width: 75%;
  overflow-x: scroll;
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.pred-attributes::-webkit-scrollbar {
  display: none;
}


.Predicate {
  padding-left: 5%;
}

.Predicate-title {
  padding-bottom: 3%;
}

.dialog-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  /* it works, but the IDE doesn't like it */
  /*noinspection CssUnresolvedCustomProperty*/
  background-color: var(--surface);
  height: 40px;
}

.pred-dialog {
  width: 555px;
  min-width: 555px;
  /*noinspection CssUnresolvedCustomProperty*/
  color: var(--background);
}

.add-icon {
  margin-left: auto;
  margin-right: 0;
  opacity: 80%;
  cursor: pointer;
  transition: opacity 0.3s ease-in-out, transform 0.3s ease-in-out, color 0.3s ease-in-out;
}

.add-icon:hover {
  opacity: 100%;
  transform: scale(1.05);
  color: var(--q-primary);
  transition: opacity 0.3s ease-in-out, transform 0.3s ease-in-out, color 0.3s ease-in-out;
}

.delete-pred {
  color: var(--on-surface);
  opacity: 80%;
  text-align: center;
  user-select: none;
  cursor: pointer;
}

.delete-pred:hover {
  text-decoration: underline;
}

.input-pred {
  min-width: 45%;
  width: 80%;
  margin-bottom: 1%;
  padding: 0;
}

</style>