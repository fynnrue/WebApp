<template>
  <div class="flex surface" style="height: 3rem; overflow-x: hidden; max-height: 95%;">
    <q-space/>
    <q-btn flat icon="groups" to="/admin/users" :color="getIconColor(section, 'users')"/>
    <q-btn flat icon="o_badge" to="/admin/credentialgroups" :color="getIconColor(section, 'credentials')"/>
    <q-btn flat icon="o_palette" to="/admin/designsettings" :color="getIconColor(section, 'settings')"/>
  </div>
</template>

<script lang="ts">
export default {
  props: {
    section: {
      type: String,
      required: true
    }
  },

  setup(props: any) {
    function getIconColor(section: string, button: string) {
      if (section === "users" && button === "users") {
        return "primary";
      } else if (section === "credentials" && button === "credentials") {
        return "primary";
      } else if (section === "settings" && button === "settings") {
        return "primary";
      } else {
        return "default-color";
      }
    }

    return { getIconColor };
  }
};
</script>

<style scoped>
/* CSS styling for the component */
</style>
