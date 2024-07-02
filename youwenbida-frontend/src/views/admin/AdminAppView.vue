<template>
  <a-form
    style="margin-bottom: 20px"
    label-align="left"
    layout="inline"
    auto-label-width
    :model="formSearchParams"
    @submit="searchSubmit"
  >
    <a-form-item field="appName" label="应用名">
      <a-input
        allow-clear
        v-model="formSearchParams.appName"
        placeholder="请输入应用名"
        @clear="initData"
      />
    </a-form-item>
    <a-form-item field="appDesc" label="应用描述">
      <a-input
        allow-clear
        v-model="formSearchParams.appDesc"
        placeholder="请输入应用描述"
        @clear="initData"
      />
    </a-form-item>
    <a-form-item>
      <a-button type="primary" html-type="submit" style="width: 100px">
        搜索
      </a-button>
    </a-form-item>
  </a-form>
  <a-table
    :bordered="{ cell: true }"
    :columns="columns"
    :data="dataList"
    @page-change="onPageChange"
    :pagination="{
      showTotal: true,
      pageSize: searchParams.pageSize,
      current: searchParams.current,
      total: total,
    }"
  >
    <template #appType="{ record }">
      {{ APP_TYPE_MAP[record.appType] }}
    </template>
    <template #scoringStrategy="{ record }">
      {{ SCORING_STRATEGY_MAP[record.scoringStrategy] }}
    </template>
    <template #reviewStatus="{ record }">
      {{ REVIEW_STATUS_MAP[record.reviewStatus] }}
    </template>
    <template #createTime="{ record }">
      {{ formatDate(record.createTime) }}
    </template>
    <template #updateTime="{ record }">
      {{ formatDate(record.updateTime) }}
    </template>
    <template #reviewTime="{ record }">
      {{ formatDate(record.reviewTime) }}
    </template>
    <template #appIcon="{ record }">
      <a-image :width="58" :src="record.appIcon" />
    </template>
    <template #otherOptional="{ record }">
      <a-space size="mini">
        <a-button type="primary" @click="doUpdate(record)" size="mini">
          修改
        </a-button>
        <a-button status="danger" @click="doDelete(record)" size="mini">
          删除
        </a-button>
      </a-space>
    </template>
    <template #reviewOptional="{ record }">
      <a-space size="mini">
        <a-button
          v-if="record.reviewStatus !== REVIEW_STATUS_ENUM.PASS"
          type="primary"
          @click="doReview(record, REVIEW_STATUS_ENUM.PASS, '')"
          size="mini"
        >
          通过
        </a-button>
        <a-button
          v-if="record.reviewStatus !== REVIEW_STATUS_ENUM.REFUSE"
          status="danger"
          @click="doReview(record, REVIEW_STATUS_ENUM.REFUSE, '不符合上架要求')"
          size="mini"
        >
          拒绝
        </a-button>
      </a-space>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import { ref, watchEffect } from "vue";
import message from "@arco-design/web-vue/es/message";
import API from "@/api";
import {
  deleteAppUsingPost,
  doAppReviewUsingPost,
  listAppByPageUsingPost,
} from "@/api/appController";
import {
  APP_TYPE_MAP,
  REVIEW_STATUS_ENUM,
  REVIEW_STATUS_MAP,
  SCORING_STRATEGY_MAP,
} from "@/enum/CommonEnum";

/**
 * 初始化搜索参数
 */
const initSearchParams = {
  pageSize: 10,
  current: 1,
};

/**
 * 搜索参数
 */
const searchParams = ref<API.AppQueryRequest>({
  ...initSearchParams,
});

const formSearchParams = ref<API.AppQueryRequest>({});

const dataList = ref<API.App[]>([]);
const total = ref(0);

/**
 * 数据加载
 */
const loadData = async () => {
  const res = await listAppByPageUsingPost(searchParams.value);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取应用数据失败," + data.message);
  }
};

const initData = async () => {
  const res = await listAppByPageUsingPost(initSearchParams);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取应用数据失败," + data.message);
  }
};

/**
 * 监听 searchParams 变化，重新加载数据
 */
watchEffect(() => {
  loadData();
});

const searchSubmit = () => {
  searchParams.value = {
    ...initSearchParams,
    ...formSearchParams.value,
  };
};

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

/**
 * 删除应用
 * @param record
 */
const doDelete = async (record: API.App) => {
  const res = await deleteAppUsingPost({
    id: record.id,
  });
  const data = res.data;
  // 删除成功
  if (data.code === 0) {
    loadData();
  } else {
    message.error("删除应用数据失败," + data.message);
  }
};

/**
 * TODO 修改应用
 * @param record
 */
const doUpdate = (record: API.App) => {
  console.log(record);
};

/**
 * 审核应用
 * @param record
 * @param reviewStatus
 * @param msg
 */
const doReview = async (
  record: API.App,
  reviewStatus: number,
  msg?: string
) => {
  console.log(record);
  if (!record.id) {
    return;
  }
  const res = await doAppReviewUsingPost({
    id: record.id,
    reviewMessage: msg,
    reviewStatus: reviewStatus,
  });
  if (res.data.code === 0) {
    // 审核成功
    loadData();
  } else {
    message.error("审核应用数据失败," + res.data.message);
  }
};

/**
 * 格式化日期
 * @param data
 */
const formatDate = (data: string) => {
  // 处理 data 为空的情况
  if (!data) {
    return "";
  }
  const date = new Date(data);
  // 手动提取年月日 时分
  const year = date.getFullYear();
  const month = ("0" + (date.getMonth() + 1)).slice(-2);
  const day = ("0" + date.getDate()).slice(-2);
  const hours = ("0" + date.getHours()).slice(-2);
  const minutes = ("0" + date.getMinutes()).slice(-2);
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

const columns = [
  {
    title: "Id",
    dataIndex: "id",
    ellipsis: true,
    tooltip: true,
    width: 58,
    fixed: "left",
  },
  {
    title: "应用名",
    dataIndex: "appName",
    ellipsis: true,
    tooltip: true,
    width: 88,
  },
  {
    title: "应用描述",
    dataIndex: "appDesc",
    ellipsis: true,
    tooltip: true,
    width: 88,
  },
  {
    title: "应用类型",
    dataIndex: "appType",
    slotName: "appType",
    width: 88,
  },
  {
    title: "评分策略",
    dataIndex: "scoringStrategy",
    slotName: "scoringStrategy",
    width: 88,
  },
  {
    title: "图标",
    dataIndex: "appIcon",
    slotName: "appIcon",
    align: "center",
    width: 88,
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    slotName: "createTime",
    align: "center",
    width: 160,
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    slotName: "updateTime",
    align: "center",
    width: 160,
  },
  {
    title: "审核状态",
    dataIndex: "reviewStatus",
    slotName: "reviewStatus",
    width: 88,
    align: "center",
  },
  {
    title: "审核信息",
    dataIndex: "reviewMessage",
    ellipsis: true,
    tooltip: true,
    width: 88,
  },
  {
    title: "审核人",
    dataIndex: "reviewerId",
    width: 72,
    ellipsis: true,
    tooltip: true,
    align: "center",
  },
  {
    title: "审核时间",
    dataIndex: "reviewTime",
    slotName: "reviewTime",
    align: "center",
    width: 160,
  },
  {
    title: "审核操作",
    slotName: "reviewOptional",
    align: "center",
    width: 120,
    fixed: "right",
  },
  {
    title: "其他操作",
    slotName: "otherOptional",
    align: "center",
    width: 120,
    fixed: "right",
  },
] as const;
</script>
