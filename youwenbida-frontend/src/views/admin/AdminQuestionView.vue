<template>
  <a-form
    style="margin-bottom: 20px"
    label-align="left"
    layout="inline"
    auto-label-width
    :model="formSearchParams"
    @submit="searchSubmit"
  >
    <a-form-item field="id" label="问题Id">
      <a-input
        allow-clear
        v-model="formSearchParams.id"
        placeholder="请输入问题Id"
        @clear="initData"
      />
    </a-form-item>
    <a-form-item field="appId" label="应用Id">
      <a-input
        allow-clear
        v-model="formSearchParams.appId"
        placeholder="请输入应用Id"
        @clear="initData"
      />
    </a-form-item>
    <a-form-item field="userId" label="所属用户Id">
      <a-input
        allow-clear
        v-model="formSearchParams.userId"
        placeholder="请输入所属用户Id"
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
    <template #createTime="{ record }">
      {{ formatDate(record.createTime) }}
    </template>
    <template #updateTime="{ record }">
      {{ formatDate(record.createTime) }}
    </template>
    <template #questionContent="{ record }">
      <div
        v-for="question in JSON.parse(record.questionContent)"
        :key="question.title"
      >
        {{ question }}
      </div>
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
import message from "@arco-design/web-vue/es/message";
import API from "@/api";
import {
  deleteQuestionUsingPost,
  listQuestionByPageUsingPost,
} from "@/api/questionController";

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
const searchParams = ref<API.QuestionQueryRequest>({
  ...initSearchParams,
});

const formSearchParams = ref<API.QuestionQueryRequest>({});

const dataList = ref<API.Question[]>([]);
const total = ref(0);

/**
 * 数据加载
 */
const loadData = async () => {
  const res = await listQuestionByPageUsingPost(searchParams.value);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取问题数据失败," + data.message);
  }
};

const initData = async () => {
  const res = await listQuestionByPageUsingPost(initSearchParams);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取问题数据失败," + data.message);
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
 * 删除问题
 * @param record
 */
const doDelete = async (record: API.Question) => {
  const res = await deleteQuestionUsingPost({
    id: record.id,
  });
  const data = res.data;
  // 删除成功
  if (data.code === 0) {
    loadData();
  } else {
    message.error("删除问题数据失败," + data.message);
  }
};

/**
 * TODO 修改问题
 * @param record
 */
const doUpdate = (record: API.Question) => {
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
    width: 48,
    fixed: "left",
  },
  {
    title: "内容",
    dataIndex: "questionContent",
    slotName: "questionContent",
    width: 1200,
    innerHeight: 200,
  },
  {
    title: "所属应用",
    dataIndex: "appId",
    width: 88,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "所属用户",
    dataIndex: "userId",
    width: 88,
    ellipsis: true,
    tooltip: true,
  },
  {
    title: "创建时间",
    dataIndex: "creatTime",
    slotName: "creatTime",
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
    width: 160,
    fixed: "right",
  },
];
</script>
