import {Floor} from "@/main/vue/types/floor";

export interface Building {
    id: number;
    name: string;
    image: string;
    floors: Floor[];
}