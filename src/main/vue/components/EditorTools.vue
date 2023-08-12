<template>
  <div class="editor-tools">
    <div v-for="(button, index) in buttons" :key="index" class="tool-container">
      <q-btn
          :name="t(button.name)"
          color="primary"
          @click="toggleExpanded(index)"
          rounded
      >
        <div class="button-content">
          <q-icon :name="button.icon"/>
          <span class="active-text q-tab--no-caps" v-if="button.active">{{ t(button.name) }}</span>
        </div>
        <q-tooltip>{{ t(button.name) }}</q-tooltip>
      </q-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import {reactive, ref} from 'vue';
import {useI18n} from 'vue-i18n';

const {t} = useI18n();
const hover = ref('');
const emit = defineEmits(['boxDrawingTool', 'cancelMethod', 'saveMethod', 'selectionTool', 'polygonTool']);

const buttons = reactive([
  {name: 'editor.tools.select', icon: 'fas fa-mouse-pointer', method: (_:any) => saveMethod(), active: true},
  //{name: 'editor.tools.drag', icon: 'pan_tool', method: (_:any) => cancelMethod(), active: false},
  //{name: 'editor.tools.group', icon: 'o_layers', method: (_:any) => cancelMethod(), active: false},
  {name: 'editor.tools.boxdrawing', icon: 'o_add_box', method: (active: boolean) => boxDrawingTool(active), active: false},
  {name: 'editor.tools.polygon', icon: 'o_polyline', method: (active: boolean) => polygonTool(active), active: false},
  //{name: 'editor.tools.cut', icon: 'o_close', method: (_:any) => cancelMethod(), active: false},
]);

function saveMethod() {
  emit('selectionTool', true);
}

function boxDrawingTool(active: boolean) {
  emit('boxDrawingTool', active);
}

function polygonTool(active: boolean) {
  emit('polygonTool', active);
}

function cancelMethod() {
}

function toggleExpanded(index: number) {
  for (let i = 0; i < buttons.length; i++) {
    if (i !== index) {
      buttons[i].active = false;
    }
  }
  buttons[index].active = !buttons[index].active;
  buttons[index].method(buttons[index].active);
  if(!buttons[index].active) {
    buttons[0].active = true;
  }
}

</script>

<style scoped>

.tool-container {
  margin-left: -5px;
}

.button-content {
  display: flex;
  align-items: center;
}

.q-btn {
  position: relative;
  height: 3.75rem;
  width: auto;
  min-width: 3.75rem;
  max-width: 100rem;
  transition: max-width 0.5s 0.5s;
}

.active-text {
  margin-left: 10px;
}

</style>
