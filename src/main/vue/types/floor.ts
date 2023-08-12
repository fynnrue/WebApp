import {RoomGroupInfoData} from "@/main/vue/types/roomGroup";

export interface Floor {
    id: number
    name: string
    roomGroups: [{
        id: number
        name: string
        color: string
    }];
    image: string
    buildingName: string
}

export interface FloorNoRooms {
    id: number
    name: string
    image: string
    defaultRoomGroup: RoomGroupInfoData
    buildingName: string
}