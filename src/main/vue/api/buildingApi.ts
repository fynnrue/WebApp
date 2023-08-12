import axios from "axios";
import {Building} from "../types/building";
import {RoomCreationData} from "@/main/vue/types/room";

/**
 * Fetch all buildings stored in the backend.
 */
export async function fetchBuildings(): Promise<Building[]> {
    const response = await axios.get("/api/buildings")
    if (response.status !== 200) {
        throw new Error("Error while fetching buildings")
    }
    return response.data as Building[]
}

/**
 * Fetch a single building.
 * @param id The id of the desired building.
 */
export async function fetchBuilding(id: bigint): Promise<Building> {
    const response = await axios.get(`/api/buildings/${id}`)
    if (response.status !== 200) {
        throw new Error("Error while fetching buildings")
    }
    return response.data as Building
}

/**
 * Add a building in the backend.
 * @param name The desired name of the building.
 * @param image The desired image of the building.
 */
export async function addBuilding(name: string, image: File): Promise<void> {
    const data = new FormData()
    data.append("name", name)
    data.append("image", image)
    await axios.post("/api/buildings", data)
}

export async function deleteBuildingF(id: bigint): Promise<void> {
    const response = await axios.delete(`/api/deleteBuildings/${id}`)
    if (response.status != 200) {
        throw new Error("Could not delete building")
    }
}