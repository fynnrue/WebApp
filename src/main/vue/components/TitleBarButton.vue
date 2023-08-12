<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "TitleBarButton",
  props: {
    title: {
      type: String,
      required: true
    },
    route: {
      type: String,
      required: true
    },
    iconName: {
      type: String,
      required: true
    },
    i18nName: {
      type: String,
      required: true
    }
  }
})

</script>

<template>
  <router-link :to="route" class="button">
    <q-icon class="icon" :name="iconName"/>
    <div class="title">{{ $t(i18nName) }}</div>
    <slot/>
  </router-link>
</template>

<style scoped lang="scss">
.button {
  color: inherit;
  text-decoration: none;
  cursor: pointer;
  margin-inline: 10px;
  text-align: center;
}

.icon {
  font-size: 30px;
}


.button:hover {
  color: var(--q-primary);
}

@media (max-width: 374px) {
  .title {
    display: none;
  }

  .icon {
    margin-right: -100%;
  }
}

</style>