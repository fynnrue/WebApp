import {CredentialORConnection} from "@/main/vue/types/credentials";

export interface Room {
    id: bigint;
    name: string;
    description: string;
    vertices: Coordinate[];
    requiredCredentials: CredentialORConnection[]
    doors: string[]
    groupInfo: RoomGroupInfo
    floorId: bigint
}

export interface Coordinate {
    x: number;
    y: number;
}

export interface RoomInfoBar extends Room {

}

export interface RoomLeaflet extends RoomInfoBar {
    leafletId: number,
    polygon: any
}

export interface RoomUpdateData {
    name?: string
    description?: string
    vertices?: Coordinate[]
    doors?: string[]
    requiredCredentials?: CredentialORConnection[]
    roomGroupId?: bigint
}

export interface RoomCreationData {
    name: string
    description: string
    vertices: Coordinate[]
}

export interface RoomGroupInfo {
    name: string
    color: number
}