<template>
  <a-button type="primary" @click="handleClick">AI 智能生成</a-button>
  <a-modal
    v-model:visible="visible"
    :on-before-ok="handleOk"
    :okText="okText"
    :ok-loading="submitting"
    @cancel="handleCancel"
  >
    <template #title> AI 智能生成 </template>
    <div>
      <a-form :model="form" style="width: 500px; margin: 32px auto 0">
        <a-form-item label="题目数量：">
          <a-input-number
            v-model="form.questionNumber"
            placeholder="请输入题目数量"
            :min="1"
            :max="20"
            style="width: 80%"
          />
        </a-form-item>
        <a-form-item label="选项数量：">
          <a-input-number
            v-model="form.optionNumber"
            placeholder="请输入每一题的选项数量"
            :min="1"
            :max="6"
            style="width: 80%"
          />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { withDefaults, defineProps, ref } from "vue";
import API from "@/api";
import { generateQuestionByAiUsingPost } from "@/api/questionController";
import message from "@arco-design/web-vue/es/message";

/**
 * AI 生成参数
 */
const form = ref({
  optionNumber: null,
  questionNumber: null,
} as API.AIGenerateQuestionRequest);

/**
 * 应用Id参数
 */
interface Props {
  appId: number;
  onSuccess: (result: API.QuestionContentDTO[]) => void;
}
const props = withDefaults(defineProps<Props>(), {
  appId: () => -1,
});

const visible = ref(false);
const submitting = ref(false);
const okText = ref("一键生成");

const handleClick = () => {
  visible.value = true;
};

/**
 * AI 生成参数提交
 */

const handleOk = async () => {
  if (props.appId < 0) {
    message.error("参数错误");
    return;
  }
  submitting.value = true;
  okText.value = "生成中";
  const res = await generateQuestionByAiUsingPost({
    appId: props.appId,
    ...form.value,
  });
  const data = res.data;
  if (data.code === 0 && data.data.length > 0) {
    visible.value = false;
    props.onSuccess(data.data);
    message.success("一键生成成功");
  } else {
    message.error("一键生成失败," + data.message);
  }
  submitting.value = false;
  okText.value = "一键生成";
};
const handleCancel = () => {
  visible.value = false;
};
</script>
