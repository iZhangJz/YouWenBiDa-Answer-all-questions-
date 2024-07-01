<template>
  <div id="AddAppView">
    <a-form
      :model="form"
      style="width: 600px; margin: 32px auto 0"
      @submit="handleSubmit"
    >
      <a-form-item label="应用名称：">
        <a-input v-model="form.appName" placeholder="请输入你的应用名称" />
      </a-form-item>
      <a-form-item label="应用描述：">
        <a-input v-model="form.appDesc" placeholder="请输入你的应用描述" />
      </a-form-item>
      <a-form-item label="应用类型：">
        <a-space direction="vertical" size="large">
          <a-select
            v-model="form.appType"
            :style="{ width: '320px' }"
            placeholder="请选择应用类型 ..."
          >
            <a-option :value="APP_TYPE_ENUM.SCORE">得分类</a-option>
            <a-option :value="APP_TYPE_ENUM.TEST">测评类</a-option>
          </a-select>
        </a-space>
      </a-form-item>
      <a-form-item label="评分策略：">
        <a-space direction="vertical" size="large">
          <a-select
            v-model="form.scoringStrategy"
            :style="{ width: '320px' }"
            placeholder="请选择评分策略 ..."
          >
            <a-option :value="SCORING_STRATEGY_ENUM.CUSTOMIZE">自定义</a-option>
            <a-option :value="SCORING_STRATEGY_ENUM.AI">AI</a-option>
          </a-select>
        </a-space>
      </a-form-item>
      <a-form-item label="应用图标：">
        <!--        <picture-upload :biz="FILE_UPLOAD_BIZ_ENUM.APP_ICON" />-->
        <a-input
          v-model="form.appIcon"
          placeholder="请输入你的应用图标URL地址"
        />
      </a-form-item>
      <div style="display: flex; justify-content: center; margin-top: 20px">
        <a-button style="width: 100px" type="primary" html-type="submit"
          >提交</a-button
        >
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { withDefaults, defineProps, ref, watchEffect } from "vue";
import message from "@arco-design/web-vue/es/message";
import {
  addAppUsingPost,
  editAppUsingPost,
  getAppVoByIdUsingGet,
} from "@/api/appController";
import API from "@/api";
import { APP_TYPE_ENUM, SCORING_STRATEGY_ENUM } from "@/enum/CommonEnum";
import { useRouter } from "vue-router";

const router = useRouter();

/**
 * 应用创建参数
 */
const form = ref({
  appName: "",
  appDesc: "",
  appIcon: "",
  appType: 0,
  scoringStrategy: 0,
} as API.AppAddRequest);

/**
 * 创建和编辑应用
 */
const handleSubmit = async () => {
  let res;
  if (props.appId < 0) {
    // 创建应用
    res = await addAppUsingPost(form.value);
    const data = res.data;
    if (data.code === 0) {
      message.success("创建应用成功, 正在跳转到应用详情页...");
      setTimeout(() => {
        router.push(`/app/detail/${data.data}`);
      }, 3000);
    } else {
      message.error("创建应用失败," + data.message);
    }
  } else {
    res = await editAppUsingPost(form.value);
    const data = res.data;
    if (data.code === 0) {
      message.success("编辑应用成功, 正在跳转到应用详情页...");
      setTimeout(() => {
        router.push(`/app/detail/${props.appId}`);
      }, 3000);
    } else {
      message.error("编辑应用失败," + data.message);
    }
  }
};

/**
 * 应用Id参数
 */
interface Props {
  appId: number;
}
const props = withDefaults(defineProps<Props>(), {
  appId: () => -1,
});

const oldApp = ref<API.AppVO>({});

/**
 * 数据加载
 */
const loadData = async () => {
  if (!props.appId || props.appId < 0) {
    return;
  }
  const res = await getAppVoByIdUsingGet({
    id: props.appId,
  });
  const data = res.data;
  if (data.code === 0) {
    oldApp.value = data.data;
    form.value = data.data;
  } else {
    message.error("获取应用数据失败," + data.message);
  }
};

watchEffect(() => {
  loadData();
});
</script>

<style scoped>
.list-demo-action-layout .image-area img {
  width: 100%;
}

#AppDetailView .content > * {
  margin-bottom: 24px;
}
</style>
