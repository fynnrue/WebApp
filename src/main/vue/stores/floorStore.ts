import {defineStore} from "pinia";

export const useFloorStore = defineStore("floorStore", {
    state: () => ({
        imagePath: "",
        floorId: 0,
        buildingId: 0
    }),
    actions: {
        setFloorId(floorId: number): void {
            this.floorId = floorId
        },
        setBuildingId(buildingId: number): void {
            this.buildingId = buildingId
        },
        setImagePath(imagePath: string): void {
            this.imagePath = imagePath
        }
    },
    getters: {
        getFloorId(): number {
            return this.floorId
        },
        getBuildingId(): number {
            return this.buildingId
        },
        getImagePath(): string {
            return this.imagePath
        }
    }
})