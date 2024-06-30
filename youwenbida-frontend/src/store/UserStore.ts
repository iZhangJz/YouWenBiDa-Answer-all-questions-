import { ref } from "vue";
import { defineStore } from "pinia";
import { getLoginUserUsingGet } from "@/api/userController";
import ACCESS_ROLE_ENUM from "@/access/accessRoleEnum";

export const useLoginUserStore = defineStore("loginUser", () => {
  const loginUser = ref<API.LoginUserVO>({
    userName: "未登录",
  });

  // 获取登录用户信息
  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet();
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data;
    } else {
      // 没有获取到用户,设置用户状态为未登录
      loginUser.value = { userRole: ACCESS_ROLE_ENUM.NOT_LOGIN };
    }
  }

  // 设置登录用户信息
  function setLoginUser(user: API.LoginUserVO) {
    loginUser.value = user;
  }

  return { loginUser, fetchLoginUser, setLoginUser };
});
