<template>
  <div id="AppDetailView">
    <a-card class="detail" :style="{ width: '1080px' }">
      <a-row class="grid-demo" style="margin-bottom: 16px">
        <a-col flex="auto" class="content">
          <h2>{{ appData.appName }}</h2>
          <p>{{ appData.appDesc }}</p>
          <p>应用类型：{{ APP_TYPE_MAP[appData.appType] }}</p>
          <p>评分策略：{{ SCORING_STRATEGY_MAP[appData.scoringStrategy] }}</p>
          <p>
            作者：
            <a-avatar
              :size="24"
              :image-url="appData.user?.userAvatar"
              :style="{ marginRight: '8px' }"
            />
            <a-typography-text>{{
              appData.user?.userName ?? "匿名用户"
            }}</a-typography-text>
          </p>
          <p style="margin-top: 64px">
            <a-space class="button">
              <a-button type="primary" :href="`/answer/do/${appData.id}`">
                开始答题
              </a-button>
              <a-button>分享应用</a-button>
              <a-button v-if="isMine" :href="`/add/question/${appData.id}`">
                修改题目
              </a-button>
              <a-button
                v-if="isMine"
                :href="`/add/scoringResult/${appData.id}`"
              >
                修改评分策略
              </a-button>
              <a-button v-if="isMine" :href="`/edit/app/${appData.id}`"
                >修改应用</a-button
              >
            </a-space>
          </p>
        </a-col>
        <a-col flex="300px">
          <a-image height="320px" :src="appData.appIcon" />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps, ref, watchEffect, withDefaults } from "vue";
import message from "@arco-design/web-vue/es/message";
import { getAppVoByIdUsingGet } from "@/api/appController";
import API from "@/api";
import { APP_TYPE_MAP, SCORING_STRATEGY_MAP } from "@/enum/CommonEnum";
import { useLoginUserStore } from "@/store/UserStore";

const appData = ref<API.AppVO>({});

/**
 * 应用Id参数
 */
interface Props {
  id: number;
}
const props = withDefaults(defineProps<Props>(), {
  id: () => -1,
});

/**
 * 数据加载
 */
const loadData = async () => {
  if (!props.id || props.id < 0) {
    return;
  }
  const res = await getAppVoByIdUsingGet({
    id: props.id,
  });
  if (res.data.code === 0) {
    appData.value = res.data?.data;
  } else {
    message.error("获取应用数据失败," + res.data.message);
  }
};

/**
 * 监听变化，重新加载数据
 */
watchEffect(() => {
  loadData();
});

/**
 * 获取登录用户
 */
const userStore = useLoginUserStore();
let user = userStore.loginUser;

const isMine = computed(() => {
  return user.id && user.id === appData.value.userId;
});
</script>

<style scoped>
.list-demo-action-layout .image-area img {
  width: 100%;
}

#AppDetailView .content > * {
  margin-bottom: 24px;
}

.detail {
  margin: 32px auto 0;
}
</style>
