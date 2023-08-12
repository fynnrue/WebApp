<script lang="ts">
import {defineComponent} from 'vue'
import {useI18n} from "vue-i18n";

export default defineComponent({
  name: "UserCredentialCards",
  props: {
    title: String,
    description: String,
    origin: String
  },
  setup () {
    const {t} = useI18n()
    return {t}
  }
})
</script>

<template>
<div id="card">
  <div id="top">
    <div id="title">{{title || t('userCredentialCard.error')}}</div>
    <div id="origin">{{origin || t('userCredentialCard.noOrigin')}}</div>
  </div>
  <div id="description">{{description || t('userCredentialCard.noDescription')}}</div>
</div>
</template>

<style scoped>
#card {
  position: relative;
  /*noinspection CssUnresolvedCustomProperty*/
  background-color: var(--background);
  min-height: 23vh;
  width: fit-content;
  min-width: calc(1.618 * 23vh);
  border-radius: 0.25rem;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.4);
}

#top{
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: start;
  min-height: 30%;
  background-color: var(--surface);
  border-top-left-radius: inherit;
  border-top-right-radius: inherit;
  padding: 0.25rem 1rem 0.5rem;
}

#title {
  position: relative;
  font-size: x-large;
  font-weight: bold;
  text-align: center;
  width: fit-content;
}

#description {
  position: relative;
  font-size: medium;
  text-align: left;
  padding: 1rem;
}

#origin {
  position: relative;
  font-size: medium;
  text-align: left;
  width: fit-content;
}
</style>