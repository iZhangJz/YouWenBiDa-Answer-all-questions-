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
import AppDetailView from "@/views/app/AppDetailView.vue";
import AddScoringResultView from "@/views/app/AddScoringResultView.vue";
import AddQuestionView from "@/views/app/AddQuestionView.vue";
import AddAppView from "@/views/app/AddAppView.vue";
import DoAnswerView from "@/views/answer/DoAnswerView.vue";
import AnswerResultView from "@/views/answer/AnswerResultView.vue";
import MyAnswerView from "@/views/answer/MyAnswerView.vue";
import AdminAppStatisticView from "@/views/admin/AdminAppStatisticView.vue";

export const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    name: "主页",
    component: HomeView,
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
      access: [ACCESS_ROLE_ENUM.ADMIN],
    },
  },
  {
    path: "/admin/app",
    name: "应用管理",
    component: AdminAppView,
    meta: {
      access: [ACCESS_ROLE_ENUM.ADMIN],
    },
  },
  {
    path: "/admin/question",
    name: "题目管理",
    component: AdminQuestionView,
    meta: {
      access: [ACCESS_ROLE_ENUM.ADMIN],
    },
  },
  {
    path: "/admin/scoringResult",
    name: "评分结果管理",
    component: AdminScoringResultView,
    meta: {
      access: [ACCESS_ROLE_ENUM.ADMIN],
    },
  },
  {
    path: "/admin/answer",
    name: "回答管理",
    component: AdminAnswerView,
    meta: {
      access: [ACCESS_ROLE_ENUM.ADMIN],
    },
  },
  {
    path: "/admin/app/statistic",
    name: "应用统计",
    component: AdminAppStatisticView,
    meta: {
      access: [ACCESS_ROLE_ENUM.ADMIN],
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
        path: "/user/login",
        name: "用户登录",
        component: UserLoginView,
      },
      {
        path: "/user/register",
        name: "用户注册",
        component: UserRegisterView,
      },
    ],
  },
  {
    path: "/app/detail/:id",
    name: "应用详情",
    props: true,
    component: AppDetailView,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/add/app",
    name: "应用创建",
    component: AddAppView,
  },
  {
    path: "/edit/app/:appId",
    name: "应用修改",
    component: AddAppView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/add/question/:appId",
    name: "题目创建",
    component: AddQuestionView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/add/scoringResult/:appId",
    name: "评分策略创建",
    component: AddScoringResultView,
    props: true,
    meta: {
      hideInMenu: true,
    },
  },
  {
    path: "/answer/do/:appId",
    name: "应用答题",
    component: DoAnswerView,
    props: true,
    meta: {
      hideInMenu: true,
      access: [
        ACCESS_ROLE_ENUM.USER,
        ACCESS_ROLE_ENUM.VIP,
        ACCESS_ROLE_ENUM.ADMIN,
      ],
    },
  },
  {
    path: "/answer/result/:id",
    name: "答题结果",
    component: AnswerResultView,
    props: true,
    meta: {
      hideInMenu: true,
      access: [
        ACCESS_ROLE_ENUM.USER,
        ACCESS_ROLE_ENUM.VIP,
        ACCESS_ROLE_ENUM.ADMIN,
      ],
    },
  },
  {
    path: "/answer/my",
    name: "我的答题",
    component: MyAnswerView,
    props: true,
    meta: {
      access: [
        ACCESS_ROLE_ENUM.USER,
        ACCESS_ROLE_ENUM.VIP,
        ACCESS_ROLE_ENUM.ADMIN,
      ],
    },
  },
];
