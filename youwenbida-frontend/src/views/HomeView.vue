<template>
  <div class="home">
    {{ JSON.stringify(loginUserStore.loginUser) }}
  </div>
  <div>Hello</div>
</template>

<script setup lang="ts">
import { useLoginUserStore } from "@/store/UserStore";

// 首次进入首页时获取用户信息
const loginUserStore = useLoginUserStore();
loginUserStore.fetchLoginUser();
</script>
