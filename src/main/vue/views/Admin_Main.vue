<script lang="ts">

import {outlinedBadge} from '@quasar/extras/material-icons-outlined'
import {onBeforeUnmount, onMounted, ref} from "vue";

export default {
  name: "Admin_Main",
  components: {},

  setup() {
    function editUser() {
    }

    function configureCredentials() {
    }

    function editWebsite() {
    }

    const iconSize = ref("");

    const setIconSize = () => {
      const windowWidth = window.innerWidth;
      if (windowWidth < 750) {
        iconSize.value = "10rem";
      } else {
        iconSize.value = "18vw";
      }
    };

    onMounted(() => {
      setIconSize();
      window.addEventListener("resize", setIconSize);
    });

    onBeforeUnmount(() => {
      window.removeEventListener("resize", setIconSize);
    });

    return {editUser, configureCredentials, editWebsite, outlinedBadge, iconSize}
  }
}
</script>

<template>
  <div class="outer-container">
    <div class="center">
      <div class="element">
        <q-btn :ripple="false" flat stack rounded class="my-btn" @click="editUser" to="/admin/users">
          <q-icon name="groups_outline" :size="iconSize"/>
          <p class="button-text">{{ $t('admin_main.users') }}</p>
        </q-btn>
      </div>
      <div class="element">
        <q-btn :ripple="false" flat stack rounded class="my-btn" @click="configureCredentials" to="/admin/credentialgroups">
          <q-icon name="o_badge" :size="iconSize" />
          <p class="button-text">{{ $t('admin_main.credentials') }}</p>
        </q-btn>
      </div>
      <div class="element">
        <q-btn :ripple="false" class="my-btn" flat stack rounded @click="editWebsite" to="/admin/designsettings">
          <q-icon name="o_palette" :size="iconSize"/>
          <p class="button-text">{{ $t('admin_main.website') }}</p>
        </q-btn>
      </div>
    </div>
  </div>
</template>

<style>
.center {
  position: absolute;
  top: 50%;
  left: 50%;
  -ms-transform: translate(-50%, -50%);
  transform: translate(-50%, -50%);
  display: flex;
  height: 80%;
  align-items: center;
}

.element {
  position: relative;
  width: 100%;
  margin: 0.5rem;
  overflow-x: clip;
}

.button-text {
  font-size: 1.85vw;
  height: 3rem;
  line-height: 93%;
  line-break: normal;
}

.my-btn {
  color: $text-primary;
  transition: all 0.2s ease-in-out;
  height: 120%;
}

.my-btn:hover {
  color: var(--q-primary);
  text-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);;
}

.q-btn .q-focus-helper {
  display: none;
}

@media (max-width: 750px) {
  .outer-container {
    height: 110vh;
    width: 100%;
  }

  .center {
    flex-direction: column;
    align-items: center;
    top: 55%;
    height: 100%;
  }

  .element {
    width: 100%;
    align-content: center;
    display: flex;
    flex-direction: column;
    overflow-y: visible;
  }

  .button-text {
    font-size: 2rem;
    height: 3rem;
    line-height: 100%;
  }
}

@media (max-width: 300px) {
  .button-text {
    font-size: 1.7rem;
  }
}
</style>