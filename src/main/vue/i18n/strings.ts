import en from "./strings_en.json"
import de from "./strings_de.json"
import {createI18n, I18n} from "vue-i18n";
import {Ref} from "vue";
// Create a Schema type for type safe access to strings
type StringSchema = typeof en

export function loadLang(): string {
    return localStorage.getItem("lang") ?? "en"
}

/**
 * My cool workaround for the types and global injection.
 * This is needed because the global injection is not typed.
 */
interface GlobalI18n extends I18n<[StringSchema], "en" | "de"> {
    locale: Ref<"en" | "de">
}
export const i18n = createI18n<[StringSchema], "en" | "de">({
    locale: loadLang(),
    legacy: false, // Needed for Composition API
    fallbackLocale: "en",
    globalInjection: true,
    messages: {
        en: en,
        de: de,
    },
}) as I18n<[StringSchema], "en" | "de"> & { global: GlobalI18n };

export function saveLang() {
    localStorage.setItem("lang", i18n.global.locale.value)
}