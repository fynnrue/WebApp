<script lang="ts">
import {useRouter} from "vue-router";
import {defineComponent, onMounted, ref} from "vue";
import {confirmRegistration} from "@/main/vue/api/register";
import {useQuasar} from "quasar";

export default defineComponent({
  name: "RedirectRegistration",
  components: {},
  props: {
    email: {
      type: String,
      required: true
    },
    token: {
      type: String,
      required: true
    }
  },

  setup(props: { email: string; token: string; }) {
    const $router = useRouter();
    const $q = useQuasar();

    let seconds = ref(5);
    const registered = ref(false);
    const loaded = ref(false);

    function redirect() {
      const amntSeconds = seconds.value;
      for (let i = 0; i < amntSeconds; i++) {
        setTimeout(() => {
          seconds.value--;
          if (seconds.value === 0) {
            $router.push('/login');
          }
        }, (i + 1) * 1000);
      }
    }

    onMounted(async() => {
      $q.loading.show();
      try {
        registered.value = await confirmRegistration(props.email, props.token);
      } catch (error) {
        console.error(error);
      } finally {
        loaded.value = true;
        $q.loading.hide();
        redirect();
      }
    });

    return {seconds, registered, loaded}
  }
})
</script>

<template>
  <div v-show="loaded" class="text-div">
    <p class="text-header" v-show="registered">{{$t('registration.redirect.headerSuccess')}}</p>
    <p class="text-header" v-show="!registered">{{$t('registration.redirect.headerFailure')}}</p>
    <p class="text-countdown">{{$t('registration.redirect.text1')}} {{seconds}} {{$t('registration.redirect.text2')}}</p>
  </div>
</template>

<style scoped>
.text-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.text-header {
  font-size: 3rem;
  text-align: center;
}

.text-countdown {
  font-size: 1.5rem;
  text-align: center;
}
</style>