import axios from "axios";
import {Floor, FloorNoRooms} from "../types/floor";
import {Room, RoomCreationData, RoomGroupInfo, RoomUpdateData} from "../types/room";
import {RoomGroup, RoomGroupInfoData} from "@/main/vue/types/roomGroup";


export function intFromHexColor(hexColor: string): number {
    return parseInt(hexColor.substring(1), 16)
}

export function hexColorFromInt(intColor: number): string {
    return `#${intColor.toString(16).padStart(6, "0")}`
}

/**
 * Get all floors from a building.
 * @param buildingId The id of the building.
 */
export async function fetchFloor(buildingId: number): Promise<Floor[]> {
    const response = await axios.get("/api/buildings/" + buildingId + "/floors");
    if (response.status !== 200) {
        throw new Error("Error while fetching floors");
    }
    return response.data as Floor[];
}

/**
 * Add a floor to a building.
 * @param name The name of the floor.
 * @param image The image of the floor to add.
 * @param buildingId The building the floor should be added to.
 */
export async function addFloor(name: string, image: File, buildingId: number): Promise<void> {
    const data = new FormData()
    data.append("name", name)
    data.append("image", image)
    await axios.post(`/api/buildings/${buildingId}/floors`, data);
}

export async function newImageAndName(floorId: number, name: string, image: File): Promise<void> {
    const data = new FormData()
    data.append("name", name)
    data.append("image", image)
    await axios.put(`/api/floors/${floorId}/nameImage`, data, {headers: {'Content-Type': 'multipart/form-data', 'Authorization': localStorage.getItem('token')}});
}

export async function updateImage(floorId: number, image: File): Promise<void> {
    const data = new FormData()
    data.append("image", image)
    await axios.put(`/api/floors/${floorId}/image`, data, {headers: {'Content-Type': 'multipart/form-data', 'Authorization': localStorage.getItem('token')}});
}

export async function updateName(floorId: number, name: string): Promise<void> {
    const data = new FormData()
    data.append("name", name)
    await axios.put(`/api/floors/${floorId}/name`, data, {headers: {'Content-Type': 'multipart/form-data', Authorization: localStorage.getItem('token')}});
}

/**
 * Fetch a single floor from the backend.
 * @param floorId The id of the desired floor.
 */
export async function fetchFloorById(floorId: number): Promise<FloorNoRooms> {
    const response = await axios.get("/api/floors/" + floorId);
    if (response.status !== 200) {
        throw new Error("Error while fetching floor");
    }
    return response.data as FloorNoRooms;
}

/**
 * Fetch all room groups from a floor.
 * @param floorId The id of the floor.
 */
export async function fetchRoomGroups(floorId: number): Promise<RoomGroupInfoData[]> {
    const response = await axios.get("/api/floors/" + floorId + "/roomGroups")
    if (response.status !== 200) {
        throw new Error("Error while fetching room groups");
    }
    return response.data as RoomGroupInfoData[];
}

/**
 * Fetch all room in a floor.
 * @param floorId the id of the floor.
 */
export async function fetchRooms(floorId: number): Promise<Room[]> {
    const response = await axios.get("/api/floors/" + floorId + "/rooms")
    if (response.status !== 200) {
        throw new Error("Error while fetching room groups");
    }
    return response.data as Room[];
}

/**
 * Fetch the room groups with the room information from the backend.
 * @param floorId The floor which contains the room groups.
 */
export async function fetchRoomsAndGroups(floorId: number): Promise<RoomGroup[]> {
    const response = await axios.get("/api/floors/" + floorId + "/roomGroupsWithRooms")
    if (response.status !== 200) {
        throw new Error("Error while fetching room groups with rooms");
    }
    return response.data as RoomGroup[];
}

/**
 * Update a single room in the backend.
 * @param roomId The id of the room.
 * @param data The updated data.
 */
export async function updateRoom(roomId: bigint, data: RoomUpdateData): Promise<void> {
    const response = await axios.put(`/api/rooms/${roomId}`, data)
    if (response.status != 201 && response.status != 200) {
        throw new Error("Error while updating room")
    }
}

export async function getRoomById(roomId: bigint): Promise<Room> {
    const response = await axios.get(`/api/rooms/${roomId}`)
    if (response.status != 200) {
        throw new Error("Error while fetching room")
    }
    return response.data as Room
}

/**
 * Fetch all doors from the backend.
 */
export async function getDoors(): Promise<string[]> {
    const response = await axios.get("/api/doors")
    if (response.status != 200) {
        throw new Error("Unable to fetch doors")
    }
    return response.data
}

/**
 * Add a room to a room group.
 * @param groupId The id of the group.
 * @param data Data necessary for creating the room.
 */
export async function addRoom(groupId: bigint, data: RoomCreationData): Promise<Room> {
    const response = await axios.post(`/api/roomGroups/${groupId}/rooms`, data)
    if (response.status != 200) {
        throw new Error("Could not add room")
    }
    return response.data
}

/**
 * Removes a room by ID.
 * @param roomId The ID of the room to remove
 */
export async function removeRoom(roomId: bigint): Promise<void> {
    const response = await axios.delete(`/api/rooms/${roomId}`)
    if (response.status != 200) {
        throw new Error("Could not remove room")
    }
}

/**
 * Add a room group to a floor.
 * @param floorId The id of the floor.
 * @param name The name of the room group.
 * @param color The color of the room group.
 */
export async function addRoomGroup(floorId: bigint, name: string, color: string): Promise<RoomGroupInfoData> {
    const response = await axios.post(`/api/floors/${floorId}/roomGroups`, {name: name, color: intFromHexColor(color)})
    if (response.status != 200) {
        throw new Error("Could not add room group")
    }
    return response.data
}

export async function updateRoomGroup(data: RoomGroupInfo, id: bigint): Promise<RoomGroupInfo> {
    const response = await axios.put(`/api/roomGroups/${id}`, data)
    if (response.status != 200) {
        throw new Error("Could not update room group")
    }
    return response.data
}

/**
 * Delete a room.
 * @param id The id of the room to delete.
 */
export async function deleteRoom(id: bigint): Promise<void> {
    const response = await axios.delete(`/api/deleteFloors/${id}`)
    if (response.status != 200) {
        throw new Error("Could not delete room")
    }
}

export async function canEnterRoom(roomId: bigint, credentials: string) {
    const checkValid = new URLSearchParams();
    checkValid.append("credentials", credentials);
    const response = await axios.post(`/api/rooms/${roomId}/checkValid`, checkValid);
    return response.data as string;
}