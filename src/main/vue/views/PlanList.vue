<script lang="ts">
import {computed, defineComponent, ref} from "vue";
import axios from "axios";
import {useI18n} from "vue-i18n";

interface Room {
  building: string;
  floor: string;
  group: string;
  name: string;
  requiredCredentials: string;
  doors: number;
}

export type Column = {
  name: string;
  label: string;
  align: string;
  field: string | ((row: Room) => any);
  sortable?: boolean;
  headerStyle?: string;
}

export default defineComponent({
  name: "PlanList",

  setup() {
    const {t} = useI18n()
    const buildingOptions = ref([] as String[])
    const buildingFilterOptions = ref(buildingOptions.value)
    //const selectedBuildings = ref([]);
    const selectedBuildings = ref<string[]>([]);

    const floors = ref([] as String[])
    const floorOptions = ref([] as String[])
    const floorFilterOptions = ref(floorOptions.value)
    //const selectedFloors = ref([]);
    const selectedFloors = ref<string[]>([]);

    const credentialOptions = ref([] as String[])
    const credentialFilterOptions = ref(credentialOptions.value)
    const selectedCredentials = ref([]);

    let filteredRows = ref([] as Room[]);
    let rows = [] as Room[]

    const columnPlan = computed(() => {
      return [
        {
          name: 'group',
          label: t('plan_list.group'),
          align: 'left',
          field: 'group',
          sortable: true,
          headerStyle: "width: 50px"
        },
        {
          name: 'building',
          label: t('plan_list.building'),
          align: 'left',
          field: 'building',
          sortable: true,
          headerStyle: "width: auto"
        },
        {
          name: 'floor',
          required: true,
          label: t('plan_list.floor'),
          field: 'floor',
          align: 'left',
          sortable: true,
          headerStyle: "width: auto"
        },
        {
          name: 'index',
          required: true,
          label: t('plan_list.room'),
          field: (row: { name: any; }) => row.name,
          align: 'left',
          sortable: true,
          headerStyle: "width: auto"
        },
        {
          name: 'doors',
          label: t('plan_list.doors'),
          align: 'left',
          field: 'doors',
          sortable: true,
          headerStyle: "width: auto"
        },
        {
          name: 'requiredCredentials',
          align: 'left',
          label: t('plan_list.requiredCredentials'),
          field: 'requiredCredentials',
          sortable: true,
          headerStyle: "width: auto"
        }
      ] as Column[]
    })

    return {
      card: ref(false),
      columnPlan, rows,
      selectedBuildings,
      selectedFloors,
      selectedCredentials,
      buildingOptions,
      buildingFilterOptions,
      floors,
      floorOptions,
      floorFilterOptions,
      credentialOptions,
      credentialFilterOptions,
      filteredRows,
      pagination: ref({
        rowsPerPage: 0
      }),
    }
  },
  data() {
    return {
      tableKey: 0,
      newBuilding: {
        name: "",
      } as Room,
      dialogVisible: false,
    };
  },
  created() {
    axios.get('/api/rooms/plan')
        .then(response => {
          response.data.forEach((room: any) => {
            this.rows.push(room)
          })
          this.filteredRows = [...this.rows];
          this.rows.forEach((room: any) => {
            let building = room.building
            let floor = room.floor
            const credentials = room.requiredCredentials
            if (!this.buildingOptions.includes(building)) {
              this.buildingOptions.push(building)
            }
            if (!this.floors.includes(floor)) {
              this.floors.push(floor)
            }
            credentials.forEach((KNF: any) => {
              KNF.forEach((credential: any) => {
                if (!this.credentialOptions.includes(credential)) {
                  this.credentialOptions.push(credential)
                }
              })
            })
          })
          this.floorFilterOptions = this.floors
          this.floorOptions = this.floors
          this.tableKey += 1
        })
        .catch(error => {
          console.error(error)
        })
  },
  watch: {
    selectedBuildings(newValue) {
      this.getFloorFilterOptions();
    }
  },
  methods: {
    applyFilter() {
      if (this.selectedBuildings && this.selectedBuildings.length > 0) {
        this.applyBuildingFilter()
      } else {
        this.filteredRows = this.rows
        this.applyCredentialFilter()
      }
      if (this.selectedFloors && this.selectedFloors.length > 0) {
        this.applyFloorFilter()
      } else {
        this.filteredRows = this.rows
        this.applyBuildingFilter()
        this.applyCredentialFilter()
      }
      if (this.selectedCredentials && this.selectedCredentials.length > 0) {
        this.applyCredentialFilter()
      } else {
        this.filteredRows = this.rows
        this.applyBuildingFilter()
        this.applyFloorFilter()
      }
    },
    applyBuildingFilter() {
      if (this.selectedBuildings && this.selectedBuildings.length > 0) {
        this.filteredRows = this.rows.filter((row) => {
          return this.selectedBuildings.includes(row.building);
        })
      }
    },
    applyFloorFilter() {
      if (this.selectedFloors && this.selectedFloors.length > 0) {
        this.filteredRows = this.rows.filter((row) => {
          return this.selectedFloors.includes(row.floor);
        })
      }
    },
    applyCredentialFilter() {
      if (this.selectedCredentials && this.selectedCredentials.length > 0) {
        this.filteredRows = this.filteredRows.filter((Row: any) => {
          let requirements = true
          for (let i = 0; i < Row.requiredCredentials.length; i++) {
            const KNF = Row.requiredCredentials[i]
            if (!this.selectedCredentials.some((credential: string) => KNF.includes(credential))) {
              requirements = false
              break;
            }
          }
          return requirements
        })
      }
    },
    getFloorFilterOptions() {
      if (this.selectedBuildings && this.selectedBuildings.length > 0) {
        this.selectedFloors = [];
        this.floorOptions = [];
        this.rows.forEach((room: any) => {
          let floor = room.floor;
          if (this.selectedBuildings.includes(room.building)) {
            if (!this.floorOptions.includes(floor)) {
              this.floorOptions.push(floor);
            }
          }
        })
        this.floorFilterOptions = this.floorOptions
      } else {
        this.floorOptions = this.floors
        this.floorFilterOptions = this.floorOptions
        this.selectedFloors = []
      }
    },
    addBuilding(val: any, done: any) {
      this.buildingFilterOptions = this.addVal(val, done, this.buildingFilterOptions, this.buildingOptions)
    },
    filterBuildingEntries(val: any, update: any) {
      this.buildingFilterOptions = this.filterEntries(val, update, this.buildingFilterOptions, this.buildingOptions)
    },
    addFloor(val: any, done: any) {
      this.floorFilterOptions = this.addVal(val, done, this.floorFilterOptions, this.floorOptions)
    },
    filterFloorEntries(val: any, update: any) {
      this.floorFilterOptions = this.filterEntries(val, update, this.floorFilterOptions, this.floorOptions)
    },
    addCred(val: any, done: any) {
      this.credentialFilterOptions = this.addVal(val, done, this.credentialFilterOptions, this.credentialOptions)
    },
    filterCredEntries(val: any, update: any) {
      this.credentialFilterOptions = this.filterEntries(val, update, this.credentialFilterOptions, this.credentialOptions)
    },
    addVal(val: any, done: any, filterOptions: any, options: any) {
      if (val.length > 2) {
        if (!filterOptions.includes(val)) {
          done(val, 'add-unique')
          filterOptions = options
        }
      }
      return filterOptions;
    },
    filterEntries(val: any, update: any, filterOptions: any, options: any) {
      update(() => {
        if (val === '') {
          filterOptions = options
        } else {
          const needle = val.toLowerCase()
          filterOptions = options
          filterOptions = filterOptions.filter(
              (value: any) => value.toString().toLowerCase().indexOf(needle) > -1
          )
        }
      })
      return filterOptions;
    },
    navigateToCredView(roomId: number) {
      this.$router.push(`/credentialView/${roomId}`);
    },
    navigateToFloor(buildingId: number, floorId: number) {
      this.$router.push(`/buildings/${buildingId}/floors/${floorId}`);
    },
  },
});

</script>

<template>
    <div class="full-width row wrap justify-start q-mt-xl filters">
      <div class="building-filter">
        <div class="text-h4">{{$t("Building")}}</div>
        <q-select clearable scrollable="false"
                  filled color="on-surface"
                  label="Selection"
                  v-model="selectedBuildings"
                  use-input
                  use-chips
                  multiple
                  input-debounce="0"
                  @filter="filterBuildingEntries"
                  @new-value="addBuilding"
                  new-value-mode="add-unique"
                  dense
                  label-color="white"
                  bg-color="primary"
                  :options="buildingFilterOptions"
                  style="max-width: 250px"
                  class = "selection-element"/>
      </div>
      <div class="floor-filter">
        <div class="text-h4">{{$t("Floor")}}</div>
        <q-select clearable scrollable="false"
                  filled color="on-surface"
                  label="Selection"
                  v-model="selectedFloors"
                  use-input
                  use-chips
                  multiple
                  input-debounce="0"
                  @filter="filterFloorEntries"
                  @new-value="addFloor"
                  new-value-mode="add-unique"
                  dense
                  label-color="white"
                  bg-color="primary"
                  :options="floorFilterOptions"
                  style="max-width: 250px"
                  class="selection-element"/>
      </div>
      <div class="credential-filter">
        <div class="text-h4">{{$t('plan_list.requiredcredentials')}}</div>
        <q-select clearable scrollable="false"
                  filled color="on-surface"
                  label="Selection"
                  v-model="selectedCredentials"
                  use-input
                  use-chips
                  multiple
                  input-debounce="0"
                  @filter="filterCredEntries"
                  @new-value="addCred"
                  new-value-mode="add-unique"
                  dense
                  label-color="white"
                  bg-color="primary"
                  :options="credentialFilterOptions"
                  style="max-width: 250px"
                  class = "selection-element cred-select"/>

          <q-btn no-caps color="primary" :label="$t('plan_list.apply')" class="apply-button" @click="applyFilter"/>
        </div>

    </div>
  <div>
      <q-table
          style="margin-top: 60px; background-color: var(--background)"
          class = "my-sticky-virtscroll-table"
          card-class="table-body-class"
          virtual-scroll
          bordered
          flat square
          v-model:pagination="pagination"
          :rows-per-page-options="[0]"
          :virtual-scroll-sticky-size-start="48"
          :rows="filteredRows"
          :columns="columnPlan as any"
          :key="tableKey">
        <template v-slot:header="props">
          <q-tr :props="props">
            <q-th v-for="col in props.cols"
                  :key="col.name"
                  :props="props"
                  style="color: white; font-weight: bold; font-size: 15px">{{col.label}}</q-th>
            <q-th/>
          </q-tr>
        </template>

      <template v-slot:body="props">
        <q-tr :props="props">
          <q-td
              v-for="(col, index) in props.cols"
              :key="col.name"
              :props="props">
            <div v-if="index == 5">
              <div class="justify-center self-center content-center">
                <q-btn label="View" color="accent" @click="navigateToCredView(props.row.roomId)">
                  <q-icon style="margin-left: 1rem" name="o_badge"/>
                </q-btn>
              </div>
            </div>
            <div v-else>
              {{ col.value }}
            </div>
          </q-td>
          <q-td auto-width>
            <q-btn size="sm" flat dense @click="navigateToFloor(props.row.buildingId, props.row.floorId)"
                   icon="visibility"/>
          </q-td>
        </q-tr>
        <q-tr/>
      </template>
    </q-table>

  </div>
</template>

<style scoped>

.selection-element {
  position: relative;
  margin-top: -1px;
  margin-left: 10px;
  height: 36px;
  width: auto;
  min-width: 180px;
  max-width: 500px;
}

.apply-button {
  margin-left: 40px;
  height: 40px;
  width: 96px;
}

.building-filter, .floor-filter, .credential-filter {
  display: flex;
}

.filters {
  display: grid;
  grid-template-columns: 24rem 20rem 1fr;
  grid-gap: 1rem;
}

.building-filter {
  grid-column: 1;
}

.floor-filter {
  grid-column: 2;
}

.credential-filter {
  grid-column: 3;
  overflow-x: visible;
}

@media (max-width: 1420px) {
  .filters {
    grid-template-columns: 36rem 1fr;
    overflow: visible;
  }

  .credential-filter {
    grid-column: span 2;
    grid-row: 2;
    overflow-x: visible;
  }

  .floor-filter {
    margin-left: -12rem;
  }
}

@media (max-width: 900px) {
  .filters {
    grid-template-columns: 100%;
  }

  .floor-filter {
    grid-column: 1;
    grid-row: 2;
    margin-left: 0;
  }

  .credential-filter {
    grid-row: 3;
  }
}

@media (max-width: 760px) {
  .credential-filter {
    flex-direction: column;
  }

  .cred-select {
    margin-top: 0.5rem;
    margin-left: 0;
  }

  .apply-button {
    margin-left: 0;
    margin-top: 1rem;
  }

  .building-filter {
    flex-direction: column;
  }

  .floor-filter {
    flex-direction: column;
  }

  .selection-element {
    margin-left: 0;
    margin-top: 0.5rem;
  }
}

@media (max-width: 425px) {
  .credential-filter {
    overflow-wrap: break-word;
  }
}



</style>

<style>
.my-sticky-virtscroll-table {
  /* height or max-height is important */
  height: 100%;
  max-height: 32rem;
}

.my-sticky-virtscroll-table .q-table__top,
.my-sticky-virtscroll-table thead tr:first-child th {
  /* bg color is important for th; just specify one */
  background-color: var(--q-primary) !important;
}

.my-sticky-virtscroll-table thead tr th {
  position: sticky;
  z-index: 1;
}

.my-sticky-virtscroll-table thead tr:last-child th {
  top: 48px;
}

.my-sticky-virtscroll-table thead tr:first-child th {
  top: 0;
}

.my-sticky-virtscroll-table tbody {
  scroll-margin-top: 48px;
}

.table-body-class {
  background-color: var(--background);
}

</style>
