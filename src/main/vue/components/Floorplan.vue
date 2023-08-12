<template>
  <q-dialog v-model="dialogVisible" persistent>
    <q-card style="background-color: var(--background)">
      <q-bar class="dialog-bar">
        <div class="text-h4">{{ $t('floorplan.room_creation') }}</div>
        <q-space/>
        <q-icon name="close" class="cursor-pointer" @click="onCancel"/>
      </q-bar>
      <q-card-section>
        <q-input v-model="roomPropertiesRef.name" label="Name"/>
        <q-input v-model="roomPropertiesRef.description" label="Description"/>
      </q-card-section>
      <q-card-actions align="right">
        <q-btn label="Cancel" class="surface" color="accent" @click="onCancel"/>
        <q-btn label="Save" color="primary" @click="onSave"/>
      </q-card-actions>
    </q-card>
  </q-dialog>
  <div ref="mapContainer" class="map-container" v-bind="$attrs"></div>
  <RoleFilter :role="Role.Editor">
    <q-fab icon="edit"
           color="primary"
           class="editor-button"
           v-model="editMode"
           direction="up"
           vertical-actions-align="left"
           @before-hide="selectionTool"
           @click="emitEditModeToggle"
    >
      <EditorTools
          @box-drawing-tool="boxDrawing"
          @selection-tool="selectionTool"
          @polygon-tool="polygonDrawing"
      />
    </q-fab>
  </RoleFilter>
  <q-btn color="accent" icon="filter_alt" class="my-filter-button" round @click="confirm = true">
    <q-tooltip>{{ $t("start.filter_Required_Credentials") }}</q-tooltip>
  </q-btn>


  <q-dialog v-model="confirm" persistent>
    <q-card style="width: 700px; background-color: var(--background)">

        <q-bar class="dialog-bar">
          <div class="text-h4">{{ $t("start.filter_rooms.title") }}</div>
          <q-space/>
          <q-icon name="close" class="cursor-pointer" v-close-popup/>
        </q-bar>
      <div class="q-pa-md">
        <q-table
            flat bordered
            :rows="fetchedRows"
            :rows-per-page-options="[0]"
            :virtual-scroll-sticky-size-start="48"
            class="my-sticky-virtscroll-table"
            :columns="columnPlan"
            row-key="ID"
            selection="multiple"
            style="background-color: var(--background)"
            max-height="20px"
            v-model:selected="selectedCredentials"
        />
      </div>
      <div class="button-components">
        <q-btn no-caps color="accent"
               :label="$t('start.filter_rooms.back')"
               v-close-popup/>
        <q-btn no-caps color="accent"
               :label="$t('start.filter_rooms.reset')"
               @click="resetFilter"
               v-close-popup/>
        <q-btn no-caps color="primary"
               :label="$t('start.filter_rooms.apply')"
               @click="filterRooms"
               v-close-popup/>
      </div>
    </q-card>
  </q-dialog>
</template>

<script lang="ts">
import {defineComponent, defineEmits, onBeforeUnmount, onMounted, onUnmounted, reactive, ref, Ref} from 'vue';
import L, {LatLngExpression, Map} from 'leaflet';
import '@geoman-io/leaflet-geoman-free'
import RoleFilter from "@/main/vue/components/RoleFilter.vue";
import EditorTools from "@/main/vue/components/EditorTools.vue";
import {Role} from "@/main/vue/stores/userStore";
import {addRoom, canEnterRoom, fetchFloorById, fetchRoomsAndGroups} from "@/main/vue/api/floor";
import {getCssVar, useQuasar} from "quasar";
import {useI18n} from "vue-i18n";
import {Coordinate, Room, RoomCreationData, RoomLeaflet} from "@/main/vue/types/room";
import {RoomGroup} from "@/main/vue/types/roomGroup";
import {Column, Credential} from "@/main/vue/views/CredentialGroupEdit.vue";
import {getAllCredentials} from "@/main/vue/api/credentials";


let columnPlan: Column[] = [
  {
    name: 'ID',
    required: true,
    label: 'ID',
    align: 'left',
    field: row => row.ID,
    format: val => `${val}`,
    sortable: true
  },
  {name: 'name', label: 'Name', align: 'left', field: 'name', sortable: true, filter: true},
  {name: 'DID', label: 'DID', align: 'left', field: 'DID', sortable: true, filter: true}
]

export default defineComponent({
  name: 'LeafletMap',
  computed: {
    Role() {
      return Role
    }
  },
  props: {
    newImageUrl: String,
    floorId: {
      type: String,
      required: true
    },
  },
  watch: {
    newImageUrl: {
      handler: function (newImageUrl: string) {
        this.draw(newImageUrl)
      }
    }
  },
  components: {EditorTools, RoleFilter},
  emit: defineEmits(['roomSelected', 'editModeToggle']),
  setup(props, emit) {

    const defaultStyle = {color: "green"}
    const selectedStyle = reactive({color: getCssVar("primary")});
    //const selectedStyle = {color: "#e20075"}

    const floorId: number = parseInt(props.floorId)
    const floorName = ref('Loading...');
    const overlay = ref();
    const {t} = useI18n()
    const $q = useQuasar()
    const roomList = [] as RoomLeaflet[]
    const editMode = ref(false)
    const mapContainer = ref<HTMLDivElement | null>(null);
    const mapSize = ref();
    let map: Map | null = null;
    const drawnLayers = L.featureGroup();
    const dialogVisible = ref(false);
    const roomPropertiesRef = ref({
      name: '',
      description: ''
    });
    const currentTool = ref('');
    const currentLayer = ref(null) as Ref<any>;
    const selectedLayer = ref(null) as any;
    const defaultGroup = ref(null) as Ref<RoomGroup | null>;
    const fetchedRows = ref<Credential[]>([]);
    const selectedCredentials = ref([]) as Ref<Credential[]>;

    onMounted(async () => {
      getFetchedCredentials();
      mapSize.value = mapContainer.value?.clientWidth as number
      $q.loading.show({
        message: t("editor.loading")
      })
      const roomNoFloors = await fetchFloorById(Number(props.floorId))
      $q.loading.hide()
      floorName.value = roomNoFloors.name
      // Create the map instance
      map = L.map(mapContainer.value!, {
        center: [0, 0],
        zoom: 0,
        crs: L.CRS.Simple,
        minZoom: -1,
        maxZoom: 4,
        zoomControl: false,
        attributionControl: false,
        zoomSnap: 0.01,
      });

      // Set background image
      const imageUrl = roomNoFloors.image //"/api/images/2";
      await getRooms()
      await draw(imageUrl)
    });

    onBeforeUnmount(() => {
      map?.off('click');
      map?.off('pm:create');
      drawnLayers.eachLayer((layer) => {
        layer.off('click');
      });
      drawnLayers.clearAllEventListeners();
    });

    onUnmounted(() => {
      // Clean up resources when the component is unmounted
      drawnLayers.clearLayers();
      drawnLayers.remove();
      if (map) {
        map.clearAllEventListeners();
        map.remove();
        map = null;
      }
    });

    async function getRooms() {
      const roomGroups = await fetchRoomsAndGroups(floorId)

      drawnLayers.clearLayers();

      roomGroups.forEach((roomGroup, index) => {
        // Takes the first group in the floorplan as default
        if (index === 0) {
          defaultGroup.value = roomGroup
        }

        roomGroup.rooms.forEach(function (room) {
          let vertices = [] as LatLngExpression[]
          room.vertices.forEach(function (vert) {
            const coords = [vert.y, vert.x] as LatLngExpression

            vertices.push(coords)
          })

          const polygon = L.polygon(vertices, {
            color: 'green',
            fillColor: `#${roomGroup.color.toString(16).padStart(6, '0')}`, // Convert to hex string
            fillOpacity: 0.5,
            className: room.id.toString()
          })

          polygon.on("click", function (e: L.LayerEvent) {
            clickEvent(e)
          });

          polygon.addTo(drawnLayers)
          const newRoom = {
            leafletId: drawnLayers.getLayerId(polygon),
            group: roomGroup,
            polygon: polygon,
            ...room
          } as RoomLeaflet

          roomList.push(newRoom)
        })
      })

      map!.on('pm:create', onPmCreate)
      map!.on("click", function (e: L.LeafletMouseEvent) {
        if (currentTool.value != '') {
          return;
        }
        // =)
        let found = false;
        drawnLayers.eachLayer((layer) => {
          if (layer instanceof L.Polygon) {
            if (isMarkerInsidePolygon(e.latlng.lat, e.latlng.lng, layer)) {
              found = true;
            }
          }
        });
        if (found) return;
        selectedLayer.value?.setStyle(defaultStyle);
        selectedLayer.value?.bringToBack();
        emit.emit('roomSelected', null)
      });
    }

    async function draw(imageUrl: string) {
      const thisMap = map!
      const img = new Image()
      img.src = imageUrl
      $q.loading.show({
        message: "Loading",
        delay: 200,
      });
      await new Promise(r => img.onload = r)
      $q.loading.hide()

      const imageBounds: L.LatLngBoundsExpression = [
        [0, 0],
        [img.height, img.width],
      ];

      thisMap.eachLayer(function (layer) {
        thisMap.removeLayer(layer);
      });

      overlay.value = L.imageOverlay(imageUrl, imageBounds).addTo(thisMap);
      thisMap.fitBounds(imageBounds, {animate: false, maxZoom: 1});
      thisMap.setMaxBounds(imageBounds);
      drawRooms()
    }

    function drawRooms() {
      map!.removeLayer(drawnLayers)
      drawnLayers.addTo(map!)
    }

    // Source: https://stackoverflow.com/questions/31790344/leaflet-check-if-marker-is-inside-polygon
    function isMarkerInsidePolygon(x: number, y: number, poly: any) {
      let inside = false;
      for (let ii = 0; ii < poly.getLatLngs().length; ii++) {
        const polyPoints = poly.getLatLngs()[ii];
        for (let i = 0, j = polyPoints.length - 1; i < polyPoints.length; j = i++) {
          const xi = polyPoints[i].lat, yi = polyPoints[i].lng;
          const xj = polyPoints[j].lat, yj = polyPoints[j].lng;

          const intersect = ((yi > y) != (yj > y))
              && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
          if (intersect) inside = !inside;
        }
      }
      return inside;
    }

    /**
     * Handles the pm:create event
     * Opens the dialog and sets the current layer
     * After this: the user can submit and the room is created
     *
     * @param e should be the PmCreateEvent
     */
    function onPmCreate(e: any) {
      dialogVisible.value = true;
      currentLayer.value = e.layer;
    }

    function getRoomByLeafletID(id: number): RoomLeaflet | null {
      let room = null;
      roomList.forEach((r) => {
        if (r.leafletId == id) {
          room = r;
        }
      });
      return room;
    }

    /**
     * Handles the click event on a layer
     * Checks if the users uses the selection Tool (empty string)
     * On Click, the layer is selected and the room is emitted
     *
     * @param e The LayerEvent
     */
    function clickEvent(e: L.LayerEvent) {
      if (currentTool.value != '') {
        return;
      }
      if (selectedLayer.value) {
        selectedLayer.value.setStyle(defaultStyle);
      }
      e.target.bringToFront();
      if (selectedLayer.value
          && L.Util.stamp(selectedLayer.value) == L.Util.stamp(e.target)) {
        e.target.bringToBack();
      }
      selectedLayer.value = e.target;
      e.target.setStyle(selectedStyle);


      const leafletId = L.Util.stamp(e.target);
      const room = getRoomByLeafletID(leafletId);
      if (room == null) {
        return;
      }
      emit.emit('roomSelected', room);
    }

    async function removeLayer(id: bigint) {
      roomList.forEach((room, index) => {
        if (room.id == id) {
          drawnLayers.removeLayer(room.polygon)
          roomList.splice(index, 1)
        }
      })
    }

    async function addLayerToMap() {
      // Fail Safe Check
      if (!currentLayer.value || !map) {
        return;
      }

      currentLayer.value.on("click", function (e: L.LayerEvent) {
        clickEvent(e)
      });

      const vertices = [] as Coordinate[]

      currentLayer.value.getLatLngs()[0].forEach(function (v: L.LatLng) {
        vertices.push({
          x: v.lng,
          y: v.lat
        } as Coordinate)
      })

      const creationData = {
        name: roomPropertiesRef.value.name,
        description: roomPropertiesRef.value.description,
        vertices: vertices,
      } as RoomCreationData

      $q.loading.show({
        message: t("editor.loading_room"),
        delay: 200,
      });
      await addRoom(BigInt(defaultGroup.value!.id), creationData).then(function (r: Room) {
        roomList.push({
          leafletId: L.Util.stamp(currentLayer.value),
          // group: defaultGroup.value!.name,
          polygon: currentLayer.value,
          ...r
        } as RoomLeaflet)
        currentLayer.value.addTo(drawnLayers);
        $q.notify({
          color: 'primary',
          message: t("editor.create_success") + " " + roomPropertiesRef.value.name,
          position: 'bottom',
          icon: 'done'
        })
      }).catch(function (e) {
        $q.notify({
          color: 'negative',
          message: t("editor.create_error") + e,
          position: 'bottom',
          icon: 'report_problem'
        })
      });
      $q.loading.hide();

      // Reset the values in dialog and the currentLayer
      roomPropertiesRef.value.name = '';
      roomPropertiesRef.value.description = '';
      currentLayer.value = null;
    }

    function onSave() {
      addLayerToMap()
      onCloseDialog();
    }

    function onCancel() {
      currentLayer.value.remove();
      currentLayer.value = null;
      onCloseDialog();
    }

    function onCloseDialog() {
      dialogVisible.value = false;
      boxDrawing(currentTool.value === 'Rectangle');
    }

    function selectionTool() {
      currentTool.value = '';
      map?.pm.disableDraw('Rectangle');
    }

    function boxDrawing(active: boolean) {
      currentTool.value = active ? 'Rectangle' : '';
      if (!active) {
        map?.pm.disableDraw('Rectangle');
        return;
      }

      map?.pm.enableDraw('Rectangle', {
        snappable: true,
        snapDistance: 20,
        allowSelfIntersection: false,
        finishOn: 'dblclick',
        tooltips: true,
        templineStyle: {
          color: 'green',
          dashArray: [5, 5],
        },
        hintlineStyle: {
          color: 'green',
          dashArray: [5, 5],
        },
        cursorMarker: true,
        markerStyle: {
          draggable: true,
        },
        pathOptions: {
          color: 'green',
          fillColor: 'green',
          fillOpacity: 0.5,
        },
      });
    }

    function polygonDrawing(active: boolean) {
      currentTool.value = active ? 'Polygon' : '';
      if (!active) {
        map?.pm.disableDraw('Polygon');
        return;
      }

      map?.pm.enableDraw('Polygon', {
        snappable: true,
        snapDistance: 20,
        allowSelfIntersection: false,
        finishOn: 'dblclick',
        tooltips: true,
        templineStyle: {
          color: 'green',
          dashArray: [5, 5],
        },
        hintlineStyle: {
          color: 'green',
          dashArray: [5, 5],
        },
        cursorMarker: true,
        markerStyle: {
          draggable: true,
        },
        pathOptions: {
          color: 'green',
          fillColor: 'green',
          fillOpacity: 0.5,
        },
      });
    }


    function emitEditModeToggle() {
      emit.emit('editModeToggle', editMode.value);
    }

    function getFetchedCredentials() {
      getAllCredentials().then((response: any) => {
            fetchedRows.value = []
            for (let credential of response) {
              fetchedRows.value.push(
                  {
                    "ID": credential.id,
                    "DID": <string>credential.credentialDid,
                    "name": <string>credential.name
                  }
              )
            }
          }
      )
    }

    function getSelectedIds() {
      const result = new Set()
      selectedCredentials.value.forEach((x) => result.add(x.ID))
      const asArray = Array.from(result).sort()
      return asArray.join(', ')
    }

    function filterRooms() {
      for (let room of roomList) {
        canEnterRoom(room.id, getSelectedIds()).then((response: any) => {
          if (response == "success") {
            let selectedLayer = drawnLayers.getLayer(room.leafletId) as L.Polygon;
            if (selectedLayer) {
              selectedLayer.setStyle({
                fillColor: "blue"
              });
            }
          } else {
            let selectedLayer = drawnLayers.getLayer(room.leafletId) as L.Polygon;
            if (selectedLayer) {
              selectedLayer.setStyle({
                fillColor: "grey"
              });
            }
          }
        })
      }
    }

    function resetFilter() {
      for (let room of roomList) {
        let selectedLayer = drawnLayers.getLayer(room.leafletId) as L.Polygon;
        if (selectedLayer) {
          selectedLayer.setStyle({
            fillColor: "green"
          });
        }
      }
      selectedCredentials.value = [];
      getRooms()
    }

    return {
      mapContainer,
      roomPropertiesRef,
      dialogVisible,
      onCancel,
      onSave,
      boxDrawing,
      editMode,
      polygonDrawing,
      selectionTool,
      emitEditModeToggle,
      removeLayer,
      draw,
      drawRooms,
      getRooms,
      confirm: ref(false),
      fetchedRows,
      columnPlan,
      selectedCredentials,
      getSelectedIds,
      filterRooms,
      resetFilter
    };
  },
});
</script>

<style scoped>

.map-container {
  position: relative;
  display: flex;
  height: 70vh;
  width: 75vw;
  margin: 0 auto;
  box-shadow: 0 0 20px 5px rgba(0, 0, 0, 0.2);
  background-color: var(--background)
}

.editor-button {
  position: fixed;
  height: 60px;
  width: 60px;
  left: 30px;
  bottom: 150px;
}

.save-button {
  position: fixed;
  height: 60px;
  width: 60px;
  left: 30px;
  bottom: 70px;
  z-index: 400;
}

.my-filter-button {
  position: fixed;
  height: 60px;
  width: 60px;
  left: 30px;
  bottom: 70px;
  z-index: 400;
}

.dialog-bar {
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.5);
  background-color: var(--surface);
  height: 40px;
}

.button-components {
  display: flex;
  justify-content: space-between;
  margin-left: 10px;
  margin-right: 10px;
}

.button-components button {
  flex-grow: 1;
  margin-right: 10px;
  margin-bottom: 10px;
}

</style>