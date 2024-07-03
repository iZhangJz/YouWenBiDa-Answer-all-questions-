<template>
  <div id="AnswerResultView">
    <a-card class="detail" :style="{ width: '1080px' }">
      <a-row class="grid-demo" style="margin-bottom: 16px">
        <a-col flex="auto" class="content">
          <div>
            <h2 v-if="resultData.resultScore === null">
              {{ resultData.resultName }}
            </h2>
            <h2 v-else>{{ resultData.resultScore + "分" }}</h2>
          </div>
          <p>结果描述：{{ resultData.resultDesc }}</p>
          <p>
            评分策略：{{ SCORING_STRATEGY_MAP[resultData.scoringStrategy] }}
          </p>
          <p>应用名称：{{ appData.appName }}</p>
          <p>应用描述：{{ appData.appDesc }}</p>
          <p>应用类型：{{ APP_TYPE_MAP[appData.appType] }}</p>
          <p>
            答题人：
            <a-avatar
              :size="24"
              :image-url="resultData.user?.userAvatar"
              :style="{ marginRight: '8px' }"
            />
            <a-typography-text>{{
              resultData.user?.userName ?? "匿名用户"
            }}</a-typography-text>
          </p>
          <p>我的答案：{{ resultData.choices }}</p>
          <p>答题时间：{{ formatDate(resultData.createTime) }}</p>
          <p style="margin-top: 32px">
            <a-space class="button">
              <a-button type="primary" :href="`/answer/do/${appData.id}`">
                重新回答
              </a-button>
            </a-space>
          </p>
        </a-col>
        <a-col flex="300px">
          <a-image height="320px" :src="resultData.resultPicture" />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref, watchEffect, withDefaults } from "vue";
import message from "@arco-design/web-vue/es/message";
import API from "@/api";
import { APP_TYPE_MAP, SCORING_STRATEGY_MAP } from "@/enum/CommonEnum";
import { getUserAnswerVoByIdUsingGet } from "@/api/userAnswerController";
import { getAppVoByIdUsingGet } from "@/api/appController";

const resultData = ref<API.UserAnswerVO>({});
const appData = ref<API.AppVO>({});
/**
 * 结果id参数
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
  let res;
  res = await getUserAnswerVoByIdUsingGet({
    id: props.id,
  });
  let data = res.data;
  if (data.code === 0) {
    resultData.value = data.data;
  } else {
    message.error("获取结果数据失败," + data.message);
  }
  res = await getAppVoByIdUsingGet({
    id: resultData.value.appId,
  });
  data = res.data;
  if (data.code === 0) {
    appData.value = data.data;
  } else {
    message.error("获取应用数据失败," + data.message);
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

/**
 * 监听变化，重新加载数据
 */
watchEffect(() => {
  loadData();
});
</script>

<style scoped>
.list-demo-action-layout .image-area img {
  width: 100%;
}

#AnswerResultView .content > * {
  margin-bottom: 24px;
}

.detail {
  margin: 32px auto 0;
}
</style>
