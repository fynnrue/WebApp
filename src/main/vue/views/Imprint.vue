<script lang="ts">
import {computed, defineComponent, ref, watch} from "vue";
import {themeStore} from "@/main/vue/stores/themeStore";

export default defineComponent({
  name: "Imprint",
  setup() {
    const imprintText = computed(() => {
      return themeStore().imprint
    })

    return {
      imprintText
    }
  }
})
</script>

<template>
    <q-card class="q-mt-md" style="width: 50%; height: fit-content; margin: auto;">
      <q-card-section v-html="imprintText" style="margin: 5%">
      </q-card-section>
    </q-card>
</template>