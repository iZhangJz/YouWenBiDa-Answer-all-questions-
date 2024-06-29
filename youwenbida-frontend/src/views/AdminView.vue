<template>
  <div>Admin 页面</div>
  <div>{{ JSON.stringify(user) }}</div>
</template>

<script setup lang="ts">
import { useLoginUserStore } from "@/store/UserStore";

const user = useLoginUserStore();
</script>

<style scoped></style>
