import {Room} from "@/main/vue/types/room";

export interface RoomGroupInfoData {
    id: bigint
    name: string
    color: number
}

export interface RoomGroup extends RoomGroupInfoData {
    rooms: Room[]
}