<script lang="ts">
import {defineComponent, ref} from "vue";

export default defineComponent({
      name: "EventOnHold",
      props: {
        title: {
          type: String,
          default: "cred.delete",
        },
        tooltip: {
          type: String,
          default: "cred.delete.tooltip",
        },
      },
      emits: ['finished'],
      setup(props, emits) {
        const interval = ref();
        const progressNorm = ref(0);

        function onStart() {
          // increase progress bar holding it for 2s before deleting
          // increase by 0.1 every 200ms -> 2s, 200ms*10 = 2000ms = 2s
          interval.value = setInterval(() => {
            if (progressNorm.value >= 1) {
              progressNorm.value = 1
              emits.emit('finished')
              onCancel()
            }
            progressNorm.value += 0.1
          }, 200)
        }

        function onCancel() {
          clearInterval(interval.value)
          progressNorm.value = 0
        }

        return {onStart, onCancel, progressNorm}
      }
    }
)
</script>

<template>
  <q-item-label class="text" @mousedown="onStart" @mouseup="onCancel"
                @mouseleave="onCancel" @touchstart="onStart" @touchend="onCancel()">
    {{ $t(title) }}
    <q-linear-progress :value="progressNorm" animation-speed="600"></q-linear-progress>
    <q-tooltip>
      {{ $t(tooltip) }}
    </q-tooltip>
  </q-item-label>
</template>

<style scoped>
.text {
  position: absolute;
  right: 1vw;
  bottom: 1vh;
  color: var(--on-surface);
  opacity: 80%;
  text-align: center;
  user-select: none;
  cursor: pointer;
}

.text:hover {
  text-decoration: underline;
}
</style>