<template>
  <div id="homeView">
    <div class="search">
      <a-input-search
        :style="{ width: '320px' }"
        placeholder="搜索你感兴趣的应用"
        search-button
      >
        <template #button-icon>
          <icon-search />
        </template>
        <template #button-default> 搜索 </template>
      </a-input-search>
    </div>
    <div class="appList">
      <a-list
        :grid-props="{ gutter: [20, 20], sm: 24, md: 12, lg: 8, xl: 6 }"
        class="list-demo-action-layout"
        :bordered="false"
        :data="dataList"
        :pagination-props="{
          pageSize: searchParams.pageSize,
          total: total,
          current: searchParams.current,
          showTotal: true,
        }"
        @page-change="onPageChange"
      >
        <template #item="{ item }">
          <AppCard :app="item" />
        </template>
      </a-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from "vue";
import AppCard from "@/components/AppCard.vue";
import message from "@arco-design/web-vue/es/message";
import { listAppVoByPageUsingPost } from "@/api/appController";
import API from "@/api";
import { REVIEW_STATUS_ENUM } from "@/enum/CommonEnum";

/**
 * 初始化搜索参数
 */
const initSearchParams = {
  pageSize: 12,
  current: 1,
  reviewStatus: REVIEW_STATUS_ENUM.PASS,
};

/**
 * 搜索参数
 */
const searchParams = ref<API.AppQueryRequest>({
  ...initSearchParams,
});

const dataList = ref<API.AppVO[]>([]);
const total = ref(0);

/**
 * 数据加载
 */
const loadData = async () => {
  const res = await listAppVoByPageUsingPost(searchParams.value);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取应用数据失败," + data.message);
  }
};

// const initData = async () => {
//   const res = await listAppByPageUsingPost(initSearchParams);
//   const data = res.data;
//   if (data.code === 0) {
//     dataList.value = data.data?.records || [];
//     total.value = data.data?.total || 0;
//   } else {
//     message.error("获取应用数据失败," + data.message);
//   }
// };

/**
 * 监听 searchParams 变化，重新加载数据
 */
watchEffect(() => {
  loadData();
});

/**
 * 分页变化
 * @param page
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};
</script>

<style scoped>
.list-demo-action-layout .image-area {
  width: 183px;
  height: 119px;
  overflow: hidden;
  border-radius: 2px;
}

.list-demo-action-layout .list-demo-item {
  padding: 20px 0;
  border-bottom: 1px solid var(--color-fill-3);
}

.list-demo-action-layout .image-area img {
  width: 100%;
}

.list-demo-action-layout .arco-list-item-action .arco-icon {
  margin: 0 4px;
}

.appList {
  margin-top: 20px;
}

.search {
  text-align: center;
}
</style>
