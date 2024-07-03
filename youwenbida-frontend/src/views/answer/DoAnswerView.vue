<template>
  <div id="doAnswerView">
    <a-card style="width: 500px; margin: 0 auto">
      <div class="name">
        <h2>{{ appData.appName }}</h2>
      </div>
      <div class="desc">{{ appData.appDesc }}</div>
      <h2>
        {{ currentQuestion?.title }}
      </h2>
      <a-radio-group
        direction="vertical"
        v-model="currentAnswer"
        :options="questionOptions"
        @change="
          () => {
            answerList[currentQuestionIndex - 1] = currentAnswer;
          }
        "
      />
      <div style="margin-top: 24px; text-align: center">
        <a-space>
          <a-button
            type="primary"
            circle
            v-if="currentQuestionIndex < questionContent.length"
            :disabled="!currentAnswer"
            @click="currentQuestionIndex += 1"
            >下一题</a-button
          >
          <a-button
            type="primary"
            v-if="currentQuestionIndex === questionContent.length"
            circle
            :disabled="!currentAnswer"
            @click="doSubmit"
          >
            查看结果
          </a-button>
          <a-button
            v-if="currentQuestionIndex > 1"
            circle
            @click="currentQuestionIndex -= 1"
            >上一题</a-button
          >
        </a-space>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  defineProps,
  reactive,
  ref,
  watchEffect,
  withDefaults,
} from "vue";
import message from "@arco-design/web-vue/es/message";
import { getAppVoByIdUsingGet } from "@/api/appController";
import API from "@/api";
import { listQuestionVoByPageUsingPost } from "@/api/questionController";
import { addUserAnswerUsingPost } from "@/api/userAnswerController";
import { useRouter } from "vue-router";

const router = useRouter();

/**
 * 接收 app 数据
 */
const appData = ref<API.AppVO>({});

/**
 * 接收题目数据
 */
const questionContent = ref<API.QuestionContentDTO[]>([]);
const currentQuestionIndex = ref(1);
const currentQuestion = ref<API.QuestionContentDTO>({});
const questionOptions = computed(() => {
  return currentQuestion.value?.options
    ? currentQuestion.value.options.map((item) => {
        return {
          label: `${item.key}.${item.value}`,
          value: item.key,
        };
      })
    : [];
});
const currentAnswer = ref<string>();
const answerList = reactive<string[]>([]);
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
 * 数据加载
 */
const loadData = async () => {
  // 获取应用
  if (!props.appId || props.appId < 0) {
    return;
  }
  let res = await getAppVoByIdUsingGet({
    id: props.appId,
  });
  let data = res.data;
  if (data.code === 0) {
    appData.value = data?.data;
  } else {
    message.error("获取应用题目数据失败," + res.data.message);
  }
  // 获取题目
  //@ts-ignore
  res = await listQuestionVoByPageUsingPost({
    appId: data.data.id,
    pageSize: 1,
    current: 1,
  });
  data = res.data;
  if (data.code === 0) {
    //@ts-ignore
    questionContent.value = data?.data.records[0].questionContent;
  } else {
    message.error("获取应用题目数据失败," + res.data.message);
  }
};
const doSubmit = async () => {
  const res = await addUserAnswerUsingPost({
    appId: props.appId,
    choices: answerList,
  });
  const data = res.data;
  if (data.code === 0) {
    message.success("提交成功");
    await router.push(`/answer/result/${data.data}`);
  } else {
    message.error("提交失败," + res.data.message);
  }
};

/**
 * 监听变化，重新加载数据
 */
watchEffect(() => {
  loadData();
});

watchEffect(() => {
  currentQuestion.value = questionContent.value[currentQuestionIndex.value - 1];
  currentAnswer.value = answerList[currentQuestionIndex.value - 1];
});
</script>

<style scoped></style>
