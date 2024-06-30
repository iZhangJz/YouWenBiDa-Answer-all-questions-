<template>
  <div id="basicLayout">
    <a-layout style="height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a-space>
          <icon-github size="16px" />
          <a
            href="https://github.com/iZhangJz"
            target="_blank"
            style="text-decoration: none; color: black"
          >
            GitHub : iZhangJZ
          </a>
        </a-space>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
</script>

<style scoped>
.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  background-color: #f0f2f5;
}

.content {
  box-sizing: border-box;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto 28px;
  padding: 24px;
  background: linear-gradient(to right, #fefefe, #ffffff);
}

.header {
  margin-bottom: 16px;
  box-shadow: #eee 1px 1px 5px;
}
</style>
