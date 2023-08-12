<script>
import {useI18n} from "vue-i18n";
import {onMounted, ref} from "vue";
import {useRoute} from "vue-router";
import {useQuasar} from "quasar";
import api from "@/main/vue/api";
import {getCredentialById, updateCredential} from "@/main/vue/api/credentials"
import {useCredentialStore} from "@/main/vue/stores/credentialStore";

export default {
    name: "CredentialEdit",

    setup() {
        const {t} = useI18n()
        const name = ref('')
        const origin = ref('')
        const additional = ref('')
        const route = useRoute();
        const $q = useQuasar();
        const credentialName = ref('')
        const credentialID = BigInt(route.params.id[0])
        const credentialStore = useCredentialStore()

        function loadCurrent () {
            name.value ="asd"
            getCredentialById(credentialID).then(response => {
                name.value = response.name
            })
        }

        function saveCredential() {
            updateCredential(credentialID, name.value, origin.value, additional.value).then(response => {
                if(response === "success") {
                    $q.notify({
                        message: ("Credential wurde erfolgreich bearbeitet"),
                        color: 'primary'
                    })
                } else {
                    $q.notify({
                        message: ("Credential konnte nicht bearbeitet werden"),
                        color: 'negative'
                    })
                }
                credentialStore.updateCredentials()
            })
        }

        onMounted(() => {
            loadCurrent();
        })

        return {
            name,
            origin,
            additional,
            credentialID,
            saveCredential
        }
    }
}
</script>

<template style="max-width: 100%; overflow: hidden">
    <div class="wrapper" style="overflow-x: hidden; max-height: 95%; margin: 0 -20px -20px;">
        <div class="flex surface" style="height: 3rem">
            <q-space/>
            <q-btn flat icon="groups" to="/admin/users"/>
            <q-btn flat color="primary" icon="o_badge"/>
            <q-btn flat icon="o_palette" to="/admin/settings"/> <!-- edit_squared -->
        </div>
    </div>

    <q-card-section align="left" class="add-components">
        <div class="q-pa-md">
            <div class="q-gutter-y-md column" >

                <div class="header-text">
                    <h3> {{$t(`admin_credential_management.edit`)}} </h3>
                </div>

                <q-input filled v-model="credentialID"
                         color="white"
                         label= "ID"
                         placeholder = credentialID
                         :dense="dense"
                         disable style="font-size: 40px; height: 110px; color:white" />

                <q-input filled v-model="name"
                         label="Name:"
                         placeholder= name.value
                         :dense="dense"
                         disable style="font-size: 30px; height: 110px; color:white" />

                <q-input filled v-model="origin"
                         :label="$t('admin_credential_management.columnNames.origin')"
                         style="font-size: 30px; height: 110px;" />


                <q-input
                        v-model="additional"
                        filled
                        clearable
                        type="textarea"
                        color="primary"
                        :label= "$t('admin_credential_group_management.columnNames.AdditionalText')"
                        style="font-size:30px; height:auto"
                />


            </div>
            <div class="button-components">
                <q-btn to="/admin/credentials" no-caps color="grey"
                       :label="$t('admin_credential_group_management.buttons.back')"
                       class="add-button"
                       style="height: 40px; flex-grow: 0.2"/>

                <q-btn no-caps color="primary"
                       :label="$t('admin_credential_group_management.buttons.save')"
                       class="add-button"
                       style="height: 40px; flex-grow: 0.4"
                       @click="saveCredential"/>
            </div>
        </div>
    </q-card-section>

</template>

<style scoped>
.button-components {
    display: flex;
    justify-content:space-between;
    margin-top: 20px;
    flex-grow: 10;
}
.button-components button {
    flex-grow: 0.5;
    margin-right: 10px;
    margin-bottom: 10px;
}


</style>
