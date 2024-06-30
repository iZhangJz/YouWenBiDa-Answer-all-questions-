import { RouteRecordRaw } from "vue-router";
import HomeView from "@/views/HomeView.vue";
import UserLoginView from "@/views/user/UserLoginView.vue";
import UserLayout from "@/layouts/UserLayout.vue";
import ACCESS_ROLE_ENUM from "@/access/accessRoleEnum";
import UserRegisterView from "@/views/user/UserRegisterView.vue";
import AdminUserView from "@/views/admin/AdminUserView.vue";
import AdminQuestionView from "@/views/admin/AdminQuestionView.vue";
import AdminScoringResultView from "@/views/admin/AdminScoringResultView.vue";
import AdminAnswerView from "@/views/admin/AdminAnswerView.vue";
import AdminAppView from "@/views/admin/AdminAppView.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "主页",
    component: HomeView,
  },
  {
    path: "/about",
    name: "about",
    component: () =>
      import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
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
    path: "/admin/user",
    name: "用户管理",
    component: AdminUserView,
    meta: {
      access: ACCESS_ROLE_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/app",
    name: "应用管理",
    component: AdminAppView,
    meta: {
      access: ACCESS_ROLE_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/question",
    name: "题目管理",
    component: AdminQuestionView,
    meta: {
      access: ACCESS_ROLE_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/scoringResult",
    name: "评分结果管理",
    component: AdminScoringResultView,
    meta: {
      access: ACCESS_ROLE_ENUM.ADMIN,
    },
  },
  {
    path: "/admin/answer",
    name: "回答管理",
    component: AdminAnswerView,
    meta: {
      access: ACCESS_ROLE_ENUM.ADMIN,
    },
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
        path: "login",
        name: "用户登录",
        component: UserLoginView,
      },
      {
        path: "register",
        name: "用户注册",
        component: UserRegisterView,
      },
    ],
  },
];
