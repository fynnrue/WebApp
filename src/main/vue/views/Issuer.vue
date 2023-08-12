<script lang="ts">

import {defineComponent, onMounted, ref} from "vue";
import {getIssuableCredentials} from "@/main/vue/api/credentials";
import {CredentialSchema} from "@/main/vue/types/credentials";
import axios from "axios";
import QrcodeVue from "qrcode.vue";
import router from "@/main/vue/router";


export default defineComponent({
  name: "Issuer",

  data() {
    return {
      credentials: null as unknown as (CredentialSchema[] | null),
      currentCredentialName: ref(null) as unknown as (null | string),
      currentCredential: ref(null) as unknown as (CredentialSchema | null),
      checkboxState: ref<boolean[]>([]),
      fieldsState: ref<string[] | null>(null),
      date: ref<string>(""),

      qr_ready: ref<boolean>(false),
      qrCodeValue: '',
      qrCodeSize: 500,
    }
  },
  created() {
    this.fetchCredentials()
  },
  computed: {
    allCheckboxesChecked(): boolean {
      return this.checkboxState.every((value) => value)
    },
  },
  methods: {
    async fetchCredentials() {
      this.$q.loading.show({message: this.$t("")})
      try {
        this.credentials = await getIssuableCredentials()
      } catch (e: any | Error) {
        this.$q.notify({
          type: 'negative',
          message: this.$t("issuer.failedToFetchCredentials"),
        })
        console.error(e.message)
      } finally {
        this.$q.loading.hide()
      }
    },
    updateCurrentCredential() {
      if (this.credentials == null) return null;
      const credentialNameMapping = new Map<string, CredentialSchema>(this.credentials.map((c) => [c.name, c]))
      this.currentCredential = credentialNameMapping.get(this.currentCredentialName ?? "") ?? null
      this.checkboxState = Array(this.currentCredential!.checklist.length).fill(false)
      this.fieldsState = Array(this.currentCredential!.fields.length).fill("")
    },
    onEditPressed() {
      router.push({name: "editChecklist", params: {credentialId: this.currentCredential?.id!.toString()}})
    },
    sendIssueRequest() {

      const data = new URLSearchParams();

      data.append("credentialDefinitionId", this.currentCredential!.credentialDid)

      this.currentCredential?.fields.forEach((value, index) => {
        if(value == "birth_date"){
          data.append(value, this.fieldsState![index].replaceAll("-", ""))
        }
        else if(value == "expiration_date"){
          data.append(value, this.fieldsState![index].replaceAll("-", ""))
        }
        else {
          data.append(value, this.fieldsState![index])
        }
      })

      this.$q.loading.show()
      axios.post('/api/issuer/create/qr', data).then(response => {
        if(response.data == "error"){
          this.$q.notify({
            type: 'negative',
            message: this.$t('issuer.error'),
            caption: this.$t('issuer.error-title')
          })
        }
        else {
          this.qrCodeValue = response.data
          this.qr_ready = true
        }
      }).finally(() => {
        this.$q.loading.hide()
      })

    },
    hideQR() {
      this.qr_ready = false
    },
  },
  components: {
    QrcodeVue
  },

  setup() {
    let currentDate = ref(new Date())
    const currentYear = currentDate.value.getFullYear();
    const currentMonth = String(currentDate.value.getMonth() + 1).padStart(2, "0");
    const expirationDate = ref(new Date())

    const birthdateOptions = (date: string) => {
      const selectedDate = new Date(date);
      return selectedDate < currentDate.value;
    };

    const expirationOptions = (date: string) => {
      const selectedDate = new Date(date);
      return selectedDate >= currentDate.value;
    };

    onMounted(() => {
      currentDate.value = new Date()
    })

    return {
      birthdateOptions,
      expirationOptions,
      currentYear,
      currentMonth,
      expirationDate,
    }
  }

})

</script>

<template>
  <div class="container centerItems">
    <h3>{{ $t("issuer.issueCredential") }}</h3>
    <div v-if="credentials != null">
      <q-tabs
          active-color="primary"
          v-model="currentCredentialName"
          @update:model-value="updateCurrentCredential()">

        <q-tab v-for="credential in credentials" :name="credential.name" :label="credential.name"/>
      </q-tabs>

      <q-form class="centerItems" v-if="currentCredential != null" >
        <div class="form-section centerItems">
          <q-input v-for="i in currentCredential?.fields.keys()" v-model="fieldsState[i]"
                   :label="$t('issuer.' + currentCredential?.fields[i])" :name="currentCredential?.fields[i]"
                   :rules="[]"
                   class="input-field">
            <template v-slot:append v-if="currentCredential?.fields[i] == 'birth_date'">
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="fieldsState[i]" mask="YYYY-MM-DD" :options="birthdateOptions" :navigation-max-year-month="`${currentYear}/${currentMonth}`">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('issuer.datePickerClose')" color="primary" flat/>
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>

            <template v-slot:append v-if="currentCredential?.fields[i] == 'expiration_date'">
              <q-icon name="event" class="cursor-pointer">
                <q-popup-proxy cover transition-show="scale" transition-hide="scale">
                  <q-date v-model="fieldsState[i]" mask="YYYY-MM-DD" :options="expirationOptions" :navigation-max-year-month="`${currentYear}/${currentMonth}`">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup :label="$t('issuer.datePickerClose')" color="primary" flat/>
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>

        <div class="form-section centerItems">
          <q-checkbox v-model="checkboxState[i]" v-for="i in currentCredential?.checklist.keys()" left-label
                      :label="currentCredential?.checklist[i]"/>
        </div>

        <q-btn type="button" :disable="!allCheckboxesChecked" color="primary"
               v-on:click="sendIssueRequest"
               :label="$t('issuer.issue')"/>
      </q-form>
      <h5 v-else>
        {{ $t("issuer.selectCredential") }}
      </h5>
      <q-card v-if="qr_ready" class="bg-white q-pa-md absolute-center" >
        <qrcode-vue :value="qrCodeValue" :size="qrCodeSize"/>
        <div>
          <q-btn v-on:click="hideQR" color="primary">{{$t("issuer.qrcode-done")}}</q-btn>
        </div>

      </q-card>
    </div>

  </div>

</template>

<style scoped>
.container {
  padding-bottom: 50px;
}

.centerItems {
  align-items: center;
  text-align: center;
  justify-content: center;
  display: flex;
  flex-direction: column;
}

.input-field {
  width: 300px
}

.form-section {
  margin-block: 25px;
}
</style>