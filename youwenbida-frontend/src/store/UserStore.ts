import { ref } from "vue";
import { defineStore } from "pinia";
import { getLoginUserUsingGet } from "@/api/userController";

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
      setTimeout(() => {
        loginUser.value = { userName: "测试用户", id: 1 };
      }, 3000);
    }
  }

  // 设置登录用户信息
  function setLoginUser(user: API.LoginUserVO) {
    loginUser.value = user;
  }

  return { loginUser, fetchLoginUser, setLoginUser };
});
