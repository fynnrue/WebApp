<template>
    <div @click="switchLanguage" class="clickable">
        <span v-bind:class="{ selected_language: $i18n.locale === lang1 }">{{ lang1.toUpperCase() }}</span> |
        <span v-bind:class="{ selected_language: $i18n.locale === lang2 }">{{ lang2.toUpperCase() }}</span>
    </div>
</template>

<script lang="ts">
import {ComponentOptions, PropType} from 'vue';
import {i18n, saveLang} from "@/main/vue/i18n/strings";

interface LangSwitcherProps {
    lang1: string;
    lang2: string;
}

export default {
    name: "LangSwitcher",
    props: {
        lang1: {
            type: String as PropType<string>,
            required: true,
        },
        lang2: {
            type: String as PropType<string>,
            required: true,
        },
    },
    methods: {
        switchLanguage() {
          //@ts-ignore
          const locale = i18n.global.locale.value;
          //@ts-ignore
          i18n.global.locale.value = locale === this.lang1 ? this.lang2 : this.lang1;
          saveLang()
        },
    } as ComponentOptions<any, LangSwitcherProps> & { switchLanguage: () => void },
};

</script>

<style scoped>
.selected_language {
    font-weight: bold;
}
</style>