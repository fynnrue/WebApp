<template>
    <div class="text-h4 q-ma-xl">{{ $t("start.select_building") }}</div>
    <BuildingCards />
</template>

<script lang="ts">
import BuildingCards from "../components/BuildingCards.vue";

export default {
  name: "Start",
    components: {
        BuildingCards,
    },
}
</script>

<style scoped>

</style>