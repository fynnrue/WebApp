import {defineStore} from "pinia";
import {Dark, setCssVar} from "quasar";
import axios from "axios";
import logoImg from "@/main/resources/logo.png";

export interface ColorScheme {
    readonly primary: string
    readonly background: string
    readonly surface: string
    readonly accent: string
}

export interface WebsiteConfiguration {
    readonly name: string
    readonly darkMode: ColorScheme
    readonly lightMode: ColorScheme
    readonly logoImage: string
    readonly favIconImage: string
    readonly imprint: string
}

export const themeStore = defineStore("theme", {
    state: (): WebsiteConfiguration => ({
        // TODO: Get theme configuration from backends
        name: "Sesam",
        logoImage: logoImg,
        favIconImage: logoImg,
        imprint: "Impressum",
        darkMode: {
            background: "#313338",
            primary: "#e20075",
            surface: "#2b2d31",
            accent: "#4d4d4d"
        },lightMode: {
            background: "#FFFFFF",
            primary: "#e20075",
            surface: "#e8e8e8",
            accent: "#4d4d4d"
        }
    }),
    actions: {
        setWebsiteConfiguration(websiteConfiguration: WebsiteConfiguration) {
            this.$patch(websiteConfiguration);
        }
    }
})

/**
 Sets the css variables to their according values in the {@link themeStore}.
 */
export function applyColorScheme() {
    const store = themeStore()
    const colorScheme = Dark.isActive ? store.darkMode : store.lightMode
    setCssVar("background", colorScheme.background)
    setCssVar("primary", colorScheme.primary)
    setCssVar("accent", colorScheme.accent)

    document.body.style.setProperty("--surface", colorScheme.surface)
    document.body.style.setProperty("--background", colorScheme.background)
    document.body.style.background = colorScheme.background  // TODO: Remove this dirty fix
    let title = document.querySelector('title');
    if (title) {
        title.textContent = themeStore().name;
    }


    let favIconlink = document.querySelector("link[rel~='icon']") as HTMLLinkElement;
    if (!favIconlink) {
        favIconlink = document.createElement('link');
        favIconlink.rel = 'icon'
        document.head.appendChild(favIconlink)
    }
    favIconlink.href = themeStore().favIconImage;
}

export function loadThemeMode() {
    Dark.set(localStorage.getItem("darkMode") == "true")
}

export function saveThemeMode() {
    localStorage.setItem("darkMode", Dark.isActive ? "true" : "false")
}


export async function loadWebsiteConfiguration() {
    try {
        const response = await axios.get("api/admin/designsettings");
        const websiteConfiguration = response.data as WebsiteConfiguration;
        const store = themeStore();
        store.setWebsiteConfiguration(websiteConfiguration);
        applyColorScheme();
    } catch (error) {
        console.error("Error while loading website configuration", error)
    }
}
loadWebsiteConfiguration();

