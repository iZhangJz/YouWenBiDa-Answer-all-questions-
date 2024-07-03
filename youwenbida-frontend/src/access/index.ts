import router from "@/router";
import { useLoginUserStore } from "@/store/UserStore";
import ACCESS_ROLE_ENUM from "@/access/accessRoleEnum";
import checkAccess from "@/access/checkAccess";

/**
 * 路由权限控制
 */
router.beforeEach(async (to, from, next) => {
  // 获取当前用户
  const userStore = useLoginUserStore();
  let user = userStore.loginUser;
  console.log("to.meta?.access:", to.meta?.access);
  const needAccess = to.meta?.access ?? [ACCESS_ROLE_ENUM.NOT_LOGIN];

  // 尝试自动登录
  if (!user || user.userName === "未登录") {
    await userStore.fetchLoginUser();
    user = userStore.loginUser;
  }
  if (needAccess == ACCESS_ROLE_ENUM.NOT_LOGIN) {
    // 无论用户是否登录，都允许访问
    next();
    return;
  }
  // 如果用户未登录，则跳转到登录页面
  if ((!user || user.userName === "未登录") && to.path !== "/user/login") {
    next(`/user/login?redirect=${to.fullPath}`);
  }
  // 用户已经登录，则校验权限
  if (!checkAccess(user, needAccess as string[])) {
    // 没有权限，则跳转到403页面
    next("/403");
    return;
  }
  next();
});
