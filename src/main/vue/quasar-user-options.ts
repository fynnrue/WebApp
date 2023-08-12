import "./styles/quasar.scss";
import iconSet from "quasar/icon-set/fontawesome-v5.js";
import "@quasar/extras/fontawesome-v5/fontawesome-v5.css";
import "@quasar/extras/material-icons-outlined/material-icons-outlined.css";
import {Dark, Loading, Notify} from "quasar";

// To be used on app.use(Quasar, { ... })
export default {
    config: {
        loading: {}
    },
    plugins: {
        Notify,
        Dark,
        Loading
    },
    iconSet: iconSet,
    extras: [
        'material-icons',
        'material-icons-outlined']
};
