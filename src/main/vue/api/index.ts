import authApi from "@/main/vue/api/auth";
import register from "./register";
import admin_users from "./admin_users"
import credential_groups from "./credential_groups"

// We should definitely consider using this index.ts file instead of the other component directly
export default {
    authApi,
    register,
    admin_users,
    credential_groups
}
