import { RouteRecordRaw } from "vue-router";
import HomeView from "@/views/HomeView.vue";
import UserLoginView from "@/views/UserLoginView.vue";
import UserLayout from "@/layouts/UserLayout.vue";
import AdminView from "@/views/AdminView.vue";
import ACCESS_ROLE_ENUM from "@/access/accessRoleEnum";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "主页",
    component: HomeView,
  },
  {
    path: "/about",
    name: "about",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
  },
  {
    path: "/admin",
    name: "管理员页面",
    component: AdminView,
    meta: {
      access: ACCESS_ROLE_ENUM.ADMIN,
    },
  },
  {
    path: "/403",
    name: "403",
    meta: {
      hideInMenu: true,
    },
    component: () =>
      import(/* webpackChunkName: "403" */ "../views/errView/403Error.vue"),
  },
  {
    path: "/user",
    name: "用户",
    meta: {
      hideInMenu: true,
    },
    component: UserLayout,
    children: [
      {
        path: "/user/login",
        name: "登录",
        component: UserLoginView,
      },
    ],
  },
];
