<template>
  <a-form
    style="margin-bottom: 20px"
    label-align="left"
    layout="inline"
    auto-label-width
    :model="formSearchParams"
    @submit="searchSubmit"
  >
    <a-form-item field="userName" label="用户昵称">
      <a-input
        allow-clear
        v-model="formSearchParams.userName"
        placeholder="请输入用户昵称"
        @clear="initData"
      />
    </a-form-item>
    <a-form-item field="userRole" label="用户角色">
      <a-input
        allow-clear
        v-model="formSearchParams.userRole"
        placeholder="请输入用户角色"
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
      {{ formatDate(record.updateTime) }}
    </template>
    <template #userAvatar="{ record }">
      <a-image :width="64" :src="record.userAvatar" />
    </template>
    <template #optional="{ record }">
      <a-space>
        <a-button size="mini" type="primary" @click="doUpdate(record)">
          修改
        </a-button>
        <a-button size="mini" status="danger" @click="doDelete(record)">
          删除
        </a-button>
      </a-space>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import { ref, watchEffect } from "vue";
import {
  deleteUserUsingPost,
  listUserByPageUsingPost,
} from "@/api/userController";
import message from "@arco-design/web-vue/es/message";
import API from "@/api";

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
const searchParams = ref<API.UserQueryRequest>({
  ...initSearchParams,
});

const formSearchParams = ref<API.UserQueryRequest>({});

const dataList = ref<API.User[]>([]);
const total = ref(0);

/**
 * 数据加载
 */
const loadData = async () => {
  const res = await listUserByPageUsingPost(searchParams.value);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取用户数据失败," + data.message);
  }
};

const initData = async () => {
  const res = await listUserByPageUsingPost(initSearchParams);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取用户数据失败," + data.message);
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
 * 删除用户
 * @param record
 */
const doDelete = async (record: API.User) => {
  const res = await deleteUserUsingPost({
    id: record.id,
  });
  const data = res.data;
  // 删除成功
  if (data.code === 0) {
    loadData();
  } else {
    message.error("删除用户数据失败," + data.message);
  }
};

/**
 * TODO 修改用户
 * @param record
 */
const doUpdate = (record: API.User) => {
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
  },
  {
    title: "账号",
    dataIndex: "userAccount",
  },
  {
    title: "用户昵称",
    dataIndex: "userName",
  },
  {
    title: "用户简介",
    dataIndex: "userProfile",
  },
  {
    title: "用户头像",
    dataIndex: "userAvatar",
    slotName: "userAvatar",
  },
  {
    title: "用户角色",
    dataIndex: "userRole",
  },
  {
    title: "注册时间",
    dataIndex: "createTime",
    slotName: "createTime",
  },
  {
    title: "更新时间",
    dataIndex: "updateTime",
    slotName: "updateTime",
  },
  {
    title: "操作",
    slotName: "optional",
    align: "center",
  },
];
</script>
