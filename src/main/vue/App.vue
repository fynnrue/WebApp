<script setup lang="ts">
import {RouterView} from 'vue-router'
import TitleBarButton from "@/main/vue/components/TitleBarButton.vue";
import LangSwitcher from "@/main/vue/components/LangSwitcher.vue";
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import {Role, userStore} from "@/main/vue/stores/userStore";
import {computed, watch} from "vue";
import {useLoginStore} from "@/main/vue/stores/loginStore";
import {useQuasar} from "quasar";
import {applyColorScheme, loadThemeMode, saveThemeMode, themeStore} from "@/main/vue/stores/themeStore";
import {useI18n} from "vue-i18n";
import router from "@/main/vue/router";
import {useCredentialStore} from "@/main/vue/stores/credentialStore";

const q = useQuasar()
const store = userStore()
const user = computed(() => store.currentUser)
useLoginStore().loadPreviousSession()
useCredentialStore().updateCredentials()

const logo = themeStore().logoImage

const { t } = useI18n()

function logout() {
  q.notify({
    message: t("title_login.logout"),
    color: 'primary'
  })
  useLoginStore().logout()
  router.push("/")
}

watch(() => q.dark.isActive, _ => {
  applyColorScheme()
  saveThemeMode()
})

loadThemeMode()
applyColorScheme()

</script>
<template>
  <q-layout view="hHh lpR fFf">

    <q-header class="surface shadow-5" height-hint="98">
      <q-toolbar class="toolbar">

        <q-toolbar-title style="height: 60px; overflow: visible">
          <router-link to="/">
            <img :src="themeStore().logoImage" alt="logo" class="logo"/>
          </router-link>
        </q-toolbar-title>

        <TitleBarButton :title="$t('title_bar.home')" route="/" icon-name="home" i18nName="title_bar.home"/>

        <RoleFilter :role="Role.Admin">
          <TitleBarButton :title="$t('title_bar.admin')" route="/admin" icon-name="shield" i18nName="title_bar.admin"/>
        </RoleFilter>
        <RoleFilter :role="Role.Issuer">
          <TitleBarButton :title="$t('title_bar.issue')" route="/issue" icon-name="edit_document" i18nName="title_bar.issue"/>
        </RoleFilter>
        <TitleBarButton v-if="user != null"
            :title="$t('title_bar.profile')"
            icon-name="account_circle"
            id="profile_button" route="/profile"
            i18nName="title_bar.profile">
        </TitleBarButton>
        <TitleBarButton v-else :title="$t('login')" route="/login" icon-name="login" i18nName="login"/>

      </q-toolbar>



    </q-header>

    <q-page-container class="content">
      <router-view :key="$route"/>
    </q-page-container>

    <q-footer class="footer surface">
      <div @click="toggleTheme" class="clickable">
        <q-icon v-if="$q.dark.isActive" name="dark_mode" size="20px" />
        <q-icon v-else name="light_mode" size="20px"/>
      </div>
      <router-link class="footer-link" to="/imprint">{{ $t("imprint.imprint") }}</router-link>
      <LangSwitcher lang1="de" lang2="en"/>
    </q-footer>

  </q-layout>


</template>

<script lang="ts">
import {Dark, useQuasar} from "quasar";
import {useLoginStore} from "@/main/vue/stores/loginStore";


export default {
    data() {
        return {
            menu: false
        }
    },
    methods: {
        toggleTheme() {
            Dark.toggle()
        },
    }
}
</script>

<style scoped lang="scss">
@import "styles/quasar.scss";


.profileMenu {
  display: flex;
  padding: 20px;
  flex-direction: column;
  align-content: center;
}

.profileMenuButton q-btn:hover {
  color: $primary;
}

.footer * {
  cursor:pointer;
  user-select: none;
  text-decoration: none;
  color: inherit;
}

.footer {
  padding: 10px;
  display: flex;
  justify-content: space-between;
}

.footer {
  padding: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-link {
  margin-left: 33px;
}

.toolbar {
  height: 70px;
}

.content {
  padding: 20px;
}

.logo {
  margin: 10px;
  width: 70px;
  height: 70px;
  overflow: visible;
}
</style>

<style lang="scss">

::-webkit-scrollbar {
  width: 0.6rem;
  height: 0.5rem;
}
::-webkit-scrollbar-button {
  width: 0;
  height: 0;
}
::-webkit-scrollbar-thumb {
  background: var(--surface);
  border: 0 none transparent;
  border-radius: 50px;
}
::-webkit-scrollbar-track {
  background: transparent;
  border: 0 none transparent;
  border-radius: 0;
}
::-webkit-scrollbar-corner {
  background: transparent;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--q-primary);
}
::-webkit-scrollbar-thumb:active {
  background: var(--q-primary);
}
</style>
