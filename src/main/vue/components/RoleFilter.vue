<template>
  <slot v-if="user?.roles.includes(role) ?? false"/>
</template>

<script lang="ts">
import {Role, userStore} from "@/main/vue/stores/userStore";
import {computed} from "vue";

export default {
  name: "RoleFilter",
  props: {
    role: Role
  },
  setup() {
    const store = userStore()
    const user = computed(() => store.currentUser)
    return {user}
  }
}
</script>

<style scoped>

</style>