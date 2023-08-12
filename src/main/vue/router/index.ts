import {createRouter, createWebHashHistory} from 'vue-router'
import StartView from '../views/Start.vue'
import LoginView from '../views/Login.vue'
import {useLoginStore} from "@/main/vue/stores/loginStore";
import RegistrationView from "@/main/vue/views/Registration.vue";
import PlanList from "@/main/vue/views/PlanList.vue";
import AdminUserManagement from "@/main/vue/views/UserManagement.vue"
import AdminCredentialManagement from "@/main/vue/views/CredentialManagement.vue"
import AdminCredentialEdit from "@/main/vue/views/CredentialEdit.vue";
import AdminCredentialGroupManagement from "@/main/vue/views/CredentialGroupManagement.vue"
import AdminCredentialGroupAdd from "@/main/vue/views/CredentialGroupAdd.vue"
import AdminCredentialGroupEdit from "@/main/vue/views/CredentialGroupEdit.vue"
import AdminView from '../views/Admin_Main.vue'
import EditUser from "@/main/vue/views/EditUser.vue";
import IssuerView from "@/main/vue/views/Issuer.vue";
import CredentialChecklistEditor from "@/main/vue/components/CredentialChecklistEditor.vue";
import FloorSelectionView from "@/main/vue/components/FloorSelection.vue";
import FloorplanVisual from "@/main/vue/views/FloorplanVisual.vue";
import Admin_websiteDesign from "@/main/vue/views/Admin_websiteDesign.vue";
import PasswordReset from "@/main/vue/views/PasswordReset.vue";
import ProfileView from "@/main/vue/views/Profile.vue";
import ChangePassword from "@/main/vue/views/ChangePassword.vue";
import RedirectRegistration from "@/main/vue/views/RedirectRegistration.vue";
import {userStore} from "@/main/vue/stores/userStore"
import Imprint from "@/main/vue/views/Imprint.vue";
import UserCredentialView from "@/main/vue/views/UserCredentialView.vue";



const router = createRouter({
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: StartView
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView
    },
    {
      path: '/buildings/',
      children: [
        {
          name: 'FloorSelection',
          path: ':buildingId',
          component: FloorSelectionView,
          props: true
        }
      ]
    },
    {
      path: '/buildings/:buildingId/floors/:floorId',
      name: 'FloorplanVisual',
      component: FloorplanVisual,
      props: true
    },
    {
      path: '/credentials',
      name: 'UserCredentialView',
      component: UserCredentialView,
    },
    {
      path: '/registration',
      name: 'registration',
      component: RegistrationView
    },
    {
      path: '/registration/:email/:token',
      name: 'redirectRegistration',
      component: RedirectRegistration,
      props: true
    },
    {
      path: '/list',
      name: 'PlanList',
      component: PlanList
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminView,
      meta: { isAdminRoute: true }
    },
    {
      path: '/admin/users',
      name: 'userManagement',
      component: AdminUserManagement,
      meta: { isAdminRoute: true }
    },
    {
      path: '/admin/users/:email',
      name: 'userDetail',
      component: EditUser,
      meta: { isAdminRoute: true }
    },
    {
      path: '/admin/credentials',
      name: 'credentialManagement',
      component: AdminCredentialManagement,
      meta: { isAdminRoute: true }
    },
    {
      path: '/admin/credentials/edit/:id',
      name: 'credentialEdit',
      component: AdminCredentialEdit
    },
    {
      path: '/admin/credentialgroups',
      name: 'credentialGroupManagement',
      component: AdminCredentialGroupManagement,
      meta: { isAdminRoute: true }
    },
    {
      path: '/admin/credentialgroups/add',
      name: 'credentialGroupAdd',
      component: AdminCredentialGroupAdd,
      meta: { isAdminRoute: true }
    },
    {
      path: '/admin/credentialgroups/edit/:name',
      name: 'credentialGroupEdit',
      component: AdminCredentialGroupEdit,
      meta: { isAdminRoute: true }
    },
    {
      path: '/issue',
      name: 'issuer',
      component: IssuerView
    }, {
      path: '/admin/edit/:credentialId',
      name: 'editChecklist',
      component: CredentialChecklistEditor,
      props: true,
      meta: { isAdminRoute: true }
    },
    {
      path: '/admin/users/:email',
      name: 'userDetail',
      component: EditUser,
      meta: { requiresAuth: true }
    } ,
    {
      path: '/admin/designsettings',
      name: 'adminWebsiteDesign',
      component: Admin_websiteDesign,
      meta: { requiresAuth: true }
    },
    {
      path: '/reset/:email/:token',
      name: 'passwordReset',
      component: PasswordReset,
      props: true
    },
    {
      path: '/changePassword',
      name: 'changePassword',
      component: ChangePassword,
    },
    {
      path: '/imprint',
      name: 'imprint',
      component: Imprint
    },
    {
      path: '/credentialView/:roomId',
      name: 'credentialView',
      component: () => import('../views/CredentialView.vue'),
      props: true
    }
  ]
})

const loginPath = '/login';
const homePath = '/';

router.beforeEach(async (to, from, next) => {
  const loginStore = useLoginStore();
  try {
    if (loginStore.authenticated == null) {
      await loginStore.loadPreviousSession();
    }
    const user = userStore();

    // check if the route requires authentication and the user is not authenticated
    if (to.meta.requiresAuth && !loginStore.authenticated) {
      next(loginPath);
      // check if the route requires admin rights and the user is not an admin
    } else if (to.meta.isAdminRoute && !(user?.isAdmin())) {
      next(homePath);
    } else {
      next(); // no argument when no redirection is needed
    }
  } catch (err) {
    console.error(err);
    // maybe show error message
    next(loginPath);
  }
});

export default router
