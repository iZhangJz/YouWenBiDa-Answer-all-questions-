<template>
  <a-form
    style="margin-bottom: 20px"
    label-align="left"
    layout="inline"
    auto-label-width
    :model="formSearchParams"
    @submit="searchSubmit"
  >
    <a-form-item field="userId" label="所属用户">
      <a-input
        allow-clear
        v-model="formSearchParams.userId"
        placeholder="请输入所属用户"
        @clear="initData"
      />
    </a-form-item>
    <a-form-item field="appId" label="所属应用">
      <a-input
        allow-clear
        v-model="formSearchParams.appId"
        placeholder="请输入所属应用"
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
    <template #createTime="{ record }">
      {{ formatDate(record.createTime) }}
    </template>
    <template #updateTime="{ record }">
      {{ formatDate(record.createTime) }}
    </template>
    <template #userAvatar="{ record }">
      <a-image :size="40" :src="record.userAvatar" />
    </template>
    <template #optional="{ record }">
      <a-space>
        <a-button size="mini" type="primary" @click="doUpdate(record)"
          >修改</a-button
        >
        <a-button size="mini" status="danger" @click="doDelete(record)"
          >删除</a-button
        >
      </a-space>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import { ref, watchEffect } from "vue";
import { deleteUserUsingPost } from "@/api/userController";
import message from "@arco-design/web-vue/es/message";
import API from "@/api";
import {
  deleteScoringResultUsingPost,
  listScoringResultByPageUsingPost,
} from "@/api/scoringResultController";

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
const searchParams = ref<API.ScoringResultQueryRequest>({
  ...initSearchParams,
});

const formSearchParams = ref<API.ScoringResultQueryRequest>({});

const dataList = ref<API.ScoringResult[]>([]);
const total = ref(0);

/**
 * 数据加载
 */
const loadData = async () => {
  const res = await listScoringResultByPageUsingPost(searchParams.value);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取评分结果数据失败," + data.message);
  }
};

const initData = async () => {
  const res = await listScoringResultByPageUsingPost(initSearchParams);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取评分结果数据失败," + data.message);
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
 * 删除评分结果
 * @param record
 */
const doDelete = async (record: API.ScoringResult) => {
  const res = await deleteScoringResultUsingPost({
    id: record.id,
  });
  const data = res.data;
  // 删除成功
  if (data.code === 0) {
    loadData();
  } else {
    message.error("删除评分结果数据失败," + data.message);
  }
};

/**
 * TODO 修改评分结果
 * @param record
 */
const doUpdate = (record: API.ScoringResult) => {
  console.log(record);
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
    width: 58,
  },
  {
    title: "结果名称",
    dataIndex: "resultName",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "结果描述",
    dataIndex: "resultDesc",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "结果图片",
    dataIndex: "resultPicture",
    slotName: "resultPicture",
  },
  {
    title: "结果属性",
    dataIndex: "resultProp",
    slotName: "resultProp",
  },
  {
    title: "得分范围",
    dataIndex: "resultScoreRange",
    align: "center",
  },
  {
    title: "所属应用",
    dataIndex: "appId",
  },
  {
    title: "所属用户",
    dataIndex: "userId",
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
    slotName: "createTime",
    width: 160,
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    slotName: "updateTime",
    width: 160,
  },
  {
    title: "操作",
    slotName: "optional",
  },
];
</script>
