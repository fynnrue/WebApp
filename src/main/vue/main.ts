import {createApp} from 'vue'
import {createPinia} from 'pinia'
import {Quasar} from 'quasar'
import quasarUserOptions from './quasar-user-options'
import 'leaflet/dist/leaflet.css'
import '@geoman-io/leaflet-geoman-free/dist/leaflet-geoman.css'

import App from './App.vue'
import router from './router'

// import store from "./stores";

import 'quasar/src/css/index.sass'
import '@quasar/extras/material-icons/material-icons.css'
import {i18n} from "@/main/vue/i18n/strings";

const app = createApp(App)


app.use(createPinia())
    .use(router)
    .use(Quasar, quasarUserOptions)
    .use(i18n)

/*
    .use(Quasar, {
        plugins: {
            Loading
        },
        config: {
            loading: {}
        }
    })
 */
// app.use(store)


app.mount('#app')
