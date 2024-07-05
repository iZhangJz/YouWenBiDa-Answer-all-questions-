<template>
  <div id="AddScoringResultView">
    <h2>结果编辑</h2>
    <div class="content">
      <a-form
        :model="form"
        style="width: 600px; justify-content: left"
        @submit="handleSubmit"
      >
        <a-form-item label="结果名称：">
          <a-input v-model="form.resultName" placeholder="请输入你的结果名称" />
        </a-form-item>
        <a-form-item label="结果描述：">
          <a-input v-model="form.resultDesc" placeholder="请输入你的结果描述" />
        </a-form-item>
        <a-form-item label="评分结果图标：">
          <!--        <picture-upload :biz="FILE_UPLOAD_BIZ_ENUM.APP_ICON" />-->
          <a-input
            v-model="form.resultPicture"
            placeholder="请输入你的评分结果图标URL地址"
          />
        </a-form-item>
        <a-form-item label="结果属性集：">
          <a-input-tag
            style="width: 320px"
            v-model="form.resultProp"
            placeholder="请输入你的结果属性集，回车确认"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="结果分数范围：">
          <a-input-number
            v-model="form.resultScoreRange"
            placeholder="请输入你的结果分数范围"
            :min="0"
            :max="100"
          />
        </a-form-item>
        <div style="display: flex; justify-content: center; margin-top: 20px">
          <a-button
            style="width: 100px; margin-bottom: 32px"
            type="primary"
            html-type="submit"
          >
            提交
          </a-button>
        </div>
      </a-form>
    </div>
    <a-divider />
    <h2>评分结果管理</h2>
    <div class="tableContent">
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
        <template #resultScoreRange="{ record }">
          <div v-if="record.resultScoreRange">
            {{ record.resultScoreRange }}
          </div>
          <div v-else>/</div>
        </template>
        <template #resultPicture="{ record }">
          <a-image width="64" :src="record.resultPicture"></a-image>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref, watchEffect, withDefaults } from "vue";
import message from "@arco-design/web-vue/es/message";
import API from "@/api";
import {
  addScoringResultUsingPost,
  deleteScoringResultUsingPost,
  listScoringResultVoByPageUsingPost,
  updateScoringResultUsingPost,
} from "@/api/scoringResultController";
import { SORT_RULE_ENUM } from "@/enum/CommonEnum";
/**
 * 应用Id参数
 */
interface Props {
  appId: number;
}
const props = withDefaults(defineProps<Props>(), {
  appId: () => -1,
});

/**
 * 记录是否是更新操作 以及 被更新的id
 */
const updateId = ref<any>(-1);

/**
 * 记录是否是数据初始化
 */
const initData = ref<boolean>(false);

/**
 * 应用创建参数
 */
const form = ref({
  resultName: "",
  resultDesc: "",
  resultPicture: "",
  resultProp: [],
  resultScoreRange: null,
});

/**
 * 初始化搜索参数
 */
const initSearchParams = {
  appId: props.appId,
  pageSize: 10,
  current: 1,
  sortField: "createTime",
  sortOrder: SORT_RULE_ENUM.DESC,
} as API.ScoringResultQueryRequest;

/**
 * 搜索参数
 */
const searchParams = ref<API.ScoringResultQueryRequest>({
  ...initSearchParams,
});

/**
 * 列表数据
 */
const dataList = ref<API.ScoringResultVO[]>([]);
const total = ref(0);

/**
 * 数据加载
 */
const loadData = async () => {
  if (initData.value) {
    // 初始化数据
    searchParams.value = {
      ...initSearchParams,
    };
  }
  const res = await listScoringResultVoByPageUsingPost(searchParams.value);
  const data = res.data;
  if (data.code === 0) {
    dataList.value = data.data?.records || [];
    total.value = data.data?.total || 0;
  } else {
    message.error("获取评分结果数据失败," + data.message);
  }
};
/**
 * 提交评分创建
 */
const handleSubmit = async () => {
  if (!props.appId) {
    return;
  }
  let res;
  if (updateId.value > 0) {
    // 更新操作
    initData.value = false;
    res = await updateScoringResultUsingPost({
      id: updateId.value,
      ...form.value,
    });
  } else {
    // 新增操作
    initData.value = true;
    res = await addScoringResultUsingPost({
      appId: props.appId,
      ...form.value,
    });
  }
  const data = res.data;
  if (data.code === 0) {
    message.success("操作成功");
    updateId.value = -1;
    form.value = {
      resultName: "",
      resultDesc: "",
      resultPicture: "",
      resultProp: [],
      resultScoreRange: null,
    };
    loadData();
  } else {
    message.error("操作失败," + data.message);
  }
};

/**
 * 监听变化，重新加载数据
 */
watchEffect(() => {
  loadData();
});

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
 * 修改评分结果 将数据传递给表单
 * @param record
 */
const doUpdate = (record: API.ScoringResultVO) => {
  updateId.value = record.id;
  //@ts-ignore
  form.value = {
    ...record,
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
    slotName: "resultScoreRange",
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
    align: "center",
  },
];
</script>

<style scoped>
.list-demo-action-layout .image-area img {
  width: 100%;
}

#AppDetailView .content > * {
  margin-bottom: 24px;
}

.content {
  margin: auto 300px;
}
</style>
