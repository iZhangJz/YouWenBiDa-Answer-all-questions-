<template>
  <div id="globalHeader">
    <a-row
      class="grid"
      style="margin-bottom: 16px"
      align="center"
      :wrap="false"
    >
      <a-col flex="auto">
        <div class="menu">
          <a-menu
            mode="horizontal"
            :selected-keys="selectedKeys"
            @menu-item-click="doMenuClick"
          >
            <a-menu-item
              key="0"
              :style="{ padding: 0, marginRight: '38px' }"
              disabled
            >
              <div class="titleBar">
                <img class="logo" src="../assets/logo.png" />
              </div>
            </a-menu-item>
            <a-menu-item v-for="item in visibleRoutes" :key="item.path">
              {{ item.name }}
            </a-menu-item>
          </a-menu>
        </div>
      </a-col>
      <a-col flex="100px">
        <div>
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { ref } from "vue";

const router = useRouter();
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};

const selectedKeys = ref(["/"]);
router.afterEach((to) => {
  selectedKeys.value = [to.path];
});

// 过滤掉meta中hideInMenu为true的
const visibleRoutes = routes.filter((item) => {
  if (item.meta?.hideInMenu === true) {
    return false;
  }
  return true;
});
</script>

<style scoped></style>

<style scoped>
.titleBar {
  display: flex;
  align-items: center;
}

.logo {
  height: 48px;
  padding-top: 8px;
}
</style>
