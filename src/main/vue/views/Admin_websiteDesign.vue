<script lang="ts">
import {computed, onMounted, reactive, ref} from "vue";
import {Dark, useQuasar} from "quasar";
import {applyColorScheme, loadWebsiteConfiguration, themeStore} from "@/main/vue/stores/themeStore";
import UploadDialogue from "@/main/vue/components/UploadDialogue.vue";
import axios from "axios";
import {useI18n} from "vue-i18n";
import AdminNavigationBar from "@/main/vue/components/AdminNavigationBar.vue";

export default {
  name: "adminWebsiteDesign",
  components: {AdminNavigationBar, UploadDialogue},

  setup() {
    const $q = useQuasar()
    const {t} = useI18n()

    const imprintCard = ref(false)
    const uploadLogoDialogVisible = ref(false)
    const uploadFavIconDialogVisible = ref(false)

    const initWebsiteConfiguration = reactive({...themeStore()})
    const websiteConfiguration = ref(themeStore())
    const initColorScheme = ref({...websiteConfiguration.value.darkMode})
    const editorMode = ref(Dark.isActive) //True = Dark False = Light
    const imprintText = ref(initWebsiteConfiguration.imprint)

    let logoName = ""
    const logoFile = ref()
    let favIconName = ""
    const favIconFile = ref()

    onMounted(async () => {
      await loadWebsiteConfiguration();
      websiteConfiguration.value = themeStore();
      editorMode.value = Dark.isActive;
      if(editorMode.value) {
        initColorScheme.value = { ...websiteConfiguration.value.darkMode };
      } else {
        initColorScheme.value = { ...websiteConfiguration.value.lightMode };
      }
      imprintText.value = websiteConfiguration.value.imprint;
      initWebsiteConfiguration.name = websiteConfiguration.value.name;
    })

    function toggleColors() {
      if (editorMode.value == true) {
        initWebsiteConfiguration.darkMode = initColorScheme.value;
        initColorScheme.value = initWebsiteConfiguration.lightMode;
      } else {
        initWebsiteConfiguration.lightMode = initColorScheme.value;
        initColorScheme.value = initWebsiteConfiguration.darkMode;
      }
    }

    async function saveChanges() {
      if (editorMode.value) {
        initWebsiteConfiguration.darkMode = initColorScheme.value;
      } else {
        initWebsiteConfiguration.lightMode = initColorScheme.value;
      }
      try {
        websiteConfiguration.value = initWebsiteConfiguration;
        const data = new FormData()
        data.append("name", websiteConfiguration.value.name);
        data.append("darkMode", new Blob([JSON.stringify(websiteConfiguration.value.darkMode)], {type: "application/json"}));
        data.append("lightMode", new Blob([JSON.stringify(websiteConfiguration.value.lightMode)], {type: "application/json"}));


        data.append("logoImage", logoFile.value);
        data.append("favIconImage", favIconFile.value);

        data.append("imprint", imprintText.value);
        $q.loading.show();
        await axios.post('/api/admin/designsettings', data)
        await loadWebsiteConfiguration();
        $q.loading.hide();
        applyColorScheme();
        $q.notify({
          color: "primary",
          message: t('admin_design_settings.UploadSuccessful')
        })
      } catch {
        console.error("Failed to upload website design");
        $q.notify({
          type: 'negative',
          caption: t('admin_design_settings.UploadFailed'),
          message: t('admin_design_settings.UploadFailed')
        })
        $q.loading.hide();
      }
    }

    function closeLogoUploadDialog() {
      uploadLogoDialogVisible.value = false
    }

    function closeFavIconUploadDialog() {
      uploadFavIconDialogVisible.value = false
    }

    async function handleLogoUpload({name, image}: { name: string, image: File }) {
      logoFile.value = image;
      logoName = name;
      closeLogoUploadDialog();
    }

    async function handleFavIconUpload({name, image}: { name: string, image: File }) {
      favIconFile.value = image;
      favIconName = name;
      closeFavIconUploadDialog();
    }

    function saveImprint() {
      imprintCard.value = false;
    }

    function cancelImprint() {
      imprintCard.value = false
      imprintText.value = websiteConfiguration.value.imprint
    }

    function switchMode() {
      toggleColors()
      Dark.toggle()
      editorMode.value = !editorMode.value
    }

    return {
      t,
      imprintCard,
      websiteConfiguration,
      initWebsiteConfiguration,
      initColorScheme,
      editorMode,
      imprintText,
      toggleColors,
      uploadLogoDialogVisible,
      uploadFavIconDialogVisible,
      saveChanges,
      logoName,
      logoFile,
      favIconName,
      favIconFile,
      handleLogoUpload,
      handleFavIconUpload,
      closeLogoUploadDialog,
      closeFavIconUploadDialog,
      saveImprint,
      cancelImprint,
      switchMode
    };
  },
  data() {

  },
  methods: {}

}


</script>

<template>

  <div class="admin-navigation-bar">
    <AdminNavigationBar section="settings"/>
  </div>


  <div class="full-width outer-container">
    <div class="left-column">
      <div style="text-align: center; width: fit-content">
        <div class="text-h4 q-ma-xl" style="font-weight: bold">{{ $t("admin_design_settings.change_color") }}</div>
        <div v-if="editorMode" class="text-h4 q-ma-xl" style="margin-top: -40px; font-style: italic">
          {{ $t("admin_design_settings.DarkMode") }}
        </div>
        <div v-else class="text-h4 q-ma-xl" style="margin-top: -40px; font-style: italic">
          {{ $t("admin_design_settings.LightMode") }}
        </div>

        <div class="color-col">

          <q-icon name="circle" class="q-icon-circle" :style="{ color: initColorScheme.background}" size="2rem"/>
          <div class="text-h4 color-text">
            {{ $t("admin_design_settings.BGC") }}
          </div>
          <q-input
              v-model="initColorScheme.background"
              :rules="['anyColor']"
              class="my-input"
              dense
          >
            <template v-slot:append>
              <q-icon name="colorize" class="cursor-pointer">
                <q-popup-proxy cover>
                  <q-color v-model="initColorScheme.background"/>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>

        <div class="color-col">
          <q-icon name="circle" class="q-icon-circle" :style="{ color: initColorScheme.surface}" size="2rem"/>
          <div class="text-h4 color-text">
            {{ $t("admin_design_settings.SFC") }}
          </div>
          <q-input
              v-model="initColorScheme.surface"
              :rules="['anyColor']"
              class="my-input"
              dense
          >
            <template v-slot:append>
              <q-icon name="colorize" class="cursor-pointer">
                <q-popup-proxy cover>
                  <q-color v-model="initColorScheme.surface"/>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>

        <div class="color-col">
          <q-icon name="circle" class="q-icon-circle" :style="{ color: initColorScheme.primary}" size="2rem"/>
          <div class="text-h4 color-text">
            {{ $t("admin_design_settings.HLC") }}
          </div>
          <q-input
              v-model="initColorScheme.primary"
              :rules="['anyColor']"
              class="my-input"
              dense
          >
            <template v-slot:append>
              <q-icon name="colorize" class="cursor-pointer">
                <q-popup-proxy cover>
                  <q-color v-model="initColorScheme.primary"/>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>

        <div class="color-col">
          <q-icon name="circle" class="q-icon-circle" :style="{ color: initColorScheme.accent}" size="2rem"/>
          <div class="text-h4 color-text">
            {{ $t("admin_design_settings.ACC") }}
          </div>
          <q-input
              v-model="initColorScheme.accent"
              :rules="['anyColor']"
              class="my-input"
              dense
          >
            <template v-slot:append>
              <q-icon name="colorize" class="cursor-pointer">
                <q-popup-proxy cover>
                  <q-color v-model="initColorScheme.accent"/>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-input>
        </div>

        <div class="slimrow">
          <q-btn text-color="#ffffff" color="primary" class="admin-button" @click="switchMode">
            {{ $t("admin_design_settings.SwitchMode") }}
          </q-btn>
        </div>
      </div>
    </div>

    <div class="right-column">
      <div class="modify-header text-h4 q-mt-xl">
        {{ $t("Name: ") }}
        <q-input
            type="text"
            class="name-input"
            id="userInput"
            dense
            v-model="initWebsiteConfiguration.name"
        />
      </div>

      <div class="text-h4 modify-div" style="grid-row: 2">
        {{ $t("admin_design_settings.UploadLogo") }}
        <q-btn text-color="#ffffff" color="primary" class="admin-button"
               @click="uploadLogoDialogVisible = true">
          {{ $t("admin_design_settings.UploadImage") }}
        </q-btn>
      </div>

      <div class="text-h4 modify-div" style="grid-row: 3">
        {{ $t("admin_design_settings.ChangeFavIcon") }}
        <q-btn text-color="#ffffff" color="primary" class="admin-button"
               @click="uploadFavIconDialogVisible = true">
          {{ $t("admin_design_settings.UploadImage") }}
        </q-btn>
      </div>

      <div class="text-h4 modify-div" style="grid-row: 4">
        {{ $t("admin_design_settings.EditImprint") }}
        <q-btn text-color="#ffffff" color="primary" @click="imprintCard = true" class="admin-button">
          {{ $t("admin_design_settings.EditImprint") }}
        </q-btn>
      </div>
    </div>
  </div>

  <div class="bottom-buttons">
    <div class="button-container">
      <q-btn text-color="#ffffff" color=accent class="admin-button"
             to="../admin">
        {{ $t("admin_design_settings.Cancel") }}
      </q-btn>
    </div>
    <div class="button-container">
      <q-btn text-color="#ffffff" color="primary" class="admin-button"
             @click="saveChanges">
        {{ $t("admin_design_settings.Save") }}
      </q-btn>
    </div>
  </div>

  <q-dialog v-model="imprintCard" class="q-dialog">
    <q-card class="my-imprint-card">
      <q-bar class="dialog-bar">
        <div class="text-h4">{{ $t("admin_design_settings.EditImprint") }}</div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer" @click="cancelImprint"/>
      </q-bar>
      <q-card-section>
        <div class="q-pa-md q-gutter-sm">
          <q-editor
              class="text-field"
              v-model="imprintText"
              :dense="$q.screen.lt.md"
              :toolbar="[
        [
          {
            label: $q.lang.editor.align,
            icon: $q.iconSet.editor.align,
            fixedLabel: true,
            options: ['left', 'center', 'right', 'justify']
          }
        ],
        ['bold', 'italic', 'strike', 'underline', 'subscript'],
        ['token', 'hr', 'link', 'custom_btn'],
        ['fullscreen'],
        [
          {
            label: $q.lang.editor.fontSize,
            icon: $q.iconSet.editor.fontSize,
            fixedLabel: true,
            fixedIcon: true,
            list: 'no-icons',
            options: [
              'size-1',
              'size-2',
              'size-3',
              'size-4',
              'size-5',
              'size-6',
              'size-7'
            ]
          },
          {
            label: $q.lang.editor.defaultFont,
            icon: $q.iconSet.editor.font,
            fixedIcon: true,
            list: 'no-icons',
            options: [
              'default_font',
              'arial',
              'arial_black',
              'comic_sans',
              'courier_new',
              'impact',
              'lucida_grande',
              'times_new_roman',
              'verdana'
            ]
          },
          'removeFormat'
        ],
        ['quote', 'unordered', 'ordered'],

        ['undo', 'redo'],
        ['viewsource']
      ]"
              :fonts="{
        arial: 'Arial',
        arial_black: 'Arial Black',
        comic_sans: 'Comic Sans MS',
        courier_new: 'Courier New',
        impact: 'Impact',
        lucida_grande: 'Lucida Grande',
        times_new_roman: 'Times New Roman',
        verdana: 'Verdana'
      }"/>
        </div>

      </q-card-section>
      <q-card-actions class="actions-container">
        <q-space/>
        <q-btn no-caps color="accent" :label='$t("admin_design_settings.Cancel")' @click="this.cancelImprint"/>
        <q-btn no-caps color="primary" :label='$t("admin_design_settings.Apply")' @click="this.saveImprint"/>
      </q-card-actions>
    </q-card>
  </q-dialog>

  <UploadDialogue v-model="uploadLogoDialogVisible" title="admin_design_settings.UploadImage"
                  @submit="handleLogoUpload" @closeUploadDialog="closeLogoUploadDialog"/>
  <UploadDialogue v-model="uploadFavIconDialogVisible" title="admin_design_settings.UploadImage"
                  @submit="handleFavIconUpload" @closeUploadDialog="closeFavIconUploadDialog"/>


</template>

<style scoped>
input[type="text"] {
  font-size: 1.625rem;
  color: #B1B1B1;
}

.admin-navigation-bar {
  position: fixed;
  left: 0;
  width: 100%;
  z-index: 999;
}

.outer-container {
  overflow-x: hidden;
  max-height: 100vh;
  display: grid;
  grid-template-columns: 50% 50%;
  padding: 10px;
  margin: 0 -20px 10rem;
}

.q-icon-circle {
  border: 3px solid #000000;
  border-radius: 50%;
  grid-column: 1;
}

.my-imprint-card {
  height: 40rem;
  min-width: 80rem;
}

.my-card {
  height: 470px;
  width: 1400px;
}

.color-col {
  display: grid;
  grid-template-columns: 3.5rem 20rem 1fr;
}

.color-text {
  text-align: left;
  width: 20rem;
  grid-column: 2;
}

.left-column {
  grid-column: 1;
}

.q-editor {
  height: 30rem;
}

.right-column {
  grid-column: 2;
  display: grid;
  grid-gap: 2.5rem;
  justify-content: flex-end;
}

.q-dialog {
  max-width: 68.75rem;
  max-height: 25rem;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.dialog-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  /*noinspection CssUnresolvedCustomProperty*/
  background-color: var(--background);
  height: 2.5rem;
}

.bottom-buttons {
  position: absolute;
  display: flex;
  justify-content: center;
  gap: 2rem;
  bottom: 60px;
  left: 50%;
  right: 50%;
}

.my-input {
  width: 9.375rem;
  background-color: transparent;
  grid-column: 3;
}

.name-input {
  background-color: transparent;
  border-top: none;
  border-left: none;
  border-right: none;
  width: 10rem;
  font-size: 1.625rem;
}

.text-field {
  overflow-y: scroll;
}

.admin-button {
  height: 3rem;
  width: 15rem;
  margin-left: auto;
  margin-right: auto;
}

.modify-div {
  display: flex;
  flex-direction: column;
  justify-items: center;
  gap: 0.5rem;
  text-align: center;
}

.modify-header {
  display: flex;
  grid-row: 1;
}

.actions-container {
  margin-right: 2%;
}

@media (max-width: 1050px) {
  .outer-container {
    grid-template-columns: 100%;
    grid-gap: 3rem;
  }

  .right-column {
    grid-column: 1;
    grid-row: 2;
    margin-left: auto;
    margin-right: auto;
  }

  .left-column {
    margin-left: auto;
    margin-right: auto;
  }
}

@media (max-width: 600px) {
  .color-col {
    grid-template-columns: 10% 3.5rem 1fr;
  }

  .q-icon-circle {
    grid-column: 2;
  }

  .color-text {
    grid-column: 3;
  }

  .my-input {
    grid-row: 2;
  }

  .bottom-buttons {
    flex-direction: column;
    gap: 1rem;
    align-items: center;
  }
}

@media (max-width: 410px) {
  .color-col {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .q-icon-circle {
    grid-column: 1;
  }

  .color-text {
    grid-column: 1;
    grid-row: 2;
    text-align: center;
  }

  .my-input {
    grid-column: 1;
    grid-row: 3;
  }
}
</style>