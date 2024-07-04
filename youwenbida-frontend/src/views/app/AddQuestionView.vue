<template>
  <div id="AddQuestionView">
    <div class="content">
      <div>
        <h2>题目编辑</h2>
        <p>应用Id：{{ props.appId }}</p>
      </div>
      <a-form
        label-align="left"
        :model="questionContent"
        :style="{ width: '600px' }"
        @submit="handleSubmit"
      >
        <a-form-item
          label="题目列表"
          :content-flex="false"
          :merge-props="false"
        >
          <a-space>
            <a-button @click="handleAddQuestion(questionContent.length)">
              底部添加题目
            </a-button>
            <AIGenerateQuestionView
              :appId="appId"
              :onSuccess="generateQuesByAiSuccess"
            />
          </a-space>
          <div
            v-for="(question, questionIndex) in questionContent"
            :key="questionIndex"
          >
            <a-space size="large">
              <h3>题目 {{ questionIndex + 1 }}</h3>
              <a-button
                size="mini"
                status="success"
                @click="handleAddQuestion(questionIndex + 1)"
              >
                添加题目
              </a-button>
              <a-button
                size="mini"
                status="danger"
                @click="handleDeleteQuestion(questionIndex)"
              >
                删除题目
              </a-button>
            </a-space>
            <a-form-item :label="`题目 ${questionIndex + 1} 标题`">
              <a-input v-model="question.title" placeholder="请输入题目标题" />
            </a-form-item>
            <!--题目选项-->
            <a-space>
              <h4>题目 {{ questionIndex + 1 }} 选项列表</h4>
              <a-button
                @click="handleAddOption(question, question.options.length)"
              >
                底部添加选项
              </a-button>
            </a-space>
            <a-form-item
              v-for="(option, optionIndex) in question.options"
              :key="optionIndex"
              :label="`选项 ${optionIndex + 1}`"
              :content-flex="false"
              :merge-props="false"
            >
              <a-form-item label="选项 Key">
                <a-input v-model="option.key" placeholder="请输入选项 Key" />
              </a-form-item>
              <a-form-item label="选项值">
                <a-input v-model="option.value" placeholder="请输入选项值" />
              </a-form-item>
              <a-form-item label="选项结果">
                <a-input v-model="option.result" placeholder="请输入选项结果" />
              </a-form-item>
              <a-form-item label="选项得分">
                <a-input-number
                  v-model="option.score"
                  placeholder="请输入选项得分"
                />
              </a-form-item>
              <a-space size="large">
                <h4>选项 {{ optionIndex + 1 }}</h4>
                <a-button
                  size="mini"
                  status="success"
                  @click="handleAddOption(question, optionIndex + 1)"
                >
                  添加选项
                </a-button>
                <a-button
                  size="mini"
                  status="danger"
                  @click="handleDeleteOption(question, optionIndex)"
                >
                  删除选项
                </a-button>
              </a-space>
            </a-form-item>
          </div>
        </a-form-item>
      </a-form>
      <div style="display: flex; justify-content: center; margin-top: 20px">
        <a-button style="width: 100px" type="primary" html-type="submit">
          提交
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref, watchEffect, withDefaults } from "vue";
import message from "@arco-design/web-vue/es/message";
import API from "@/api";
import {
  addQuestionUsingPost,
  editQuestionUsingPost,
  listQuestionVoByPageUsingPost,
} from "@/api/questionController";
import { useRouter } from "vue-router";
import { SORT_RULE_ENUM } from "@/enum/CommonEnum";
import AIGenerateQuestionView from "@/views/app/components/AIGenerateQuestionView.vue";

const router = useRouter();

/**
 * 存储题目内容
 */
const questionContent = ref([] as API.QuestionContentDTO[]);

/**
 * 添加题目
 * @param index
 */
const handleAddQuestion = (index: number) => {
  questionContent.value.splice(index, 0, {
    title: "",
    options: [],
  });
};
/**
 * 删除题目
 * @param index
 */
const handleDeleteQuestion = (index: number) => {
  questionContent.value.splice(index, 1);
};

/**
 * 添加题目选项
 * @param question
 * @param index
 */
const handleAddOption = (question: API.QuestionContentDTO, index: number) => {
  question.options.splice(index, 0, {
    key: "",
    value: "",
    result: "",
    score: 0,
  });
};
/**
 * 删除题目选项
 * @param question
 * @param index
 */
const handleDeleteOption = (
  question: API.QuestionContentDTO,
  index: number
) => {
  question.options.splice(index, 1);
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
/**
 * 旧的应用题目数据
 */
const oldQuestion = ref<API.QuestionVO>();

/**
 * 数据加载
 */
const loadData = async () => {
  if (!props.appId || props.appId < 0) {
    return;
  }
  const res = await listQuestionVoByPageUsingPost({
    appId: props.appId,
    current: 1,
    pageSize: 1,
    sortField: "createTime",
    sortOrder: SORT_RULE_ENUM.DESC,
  });
  if (res.data.code === 0) {
    oldQuestion.value = res.data?.data.records[0];
    if (oldQuestion.value) {
      questionContent.value = oldQuestion.value.questionContent;
    }
  } else {
    message.error("获取应用题目数据失败," + res.data.message);
  }
};

/**
 * 监听变化，重新加载数据
 */
watchEffect(() => {
  loadData();
});

const handleSubmit = async () => {
  let res;
  if (!oldQuestion.value || !oldQuestion.value.questionContent) {
    // 说明是新建题目
    res = await addQuestionUsingPost({
      appId: props.appId,
      questionContent: questionContent.value,
    });
  } else {
    // 说明是编辑题目
    res = await editQuestionUsingPost({
      id: oldQuestion.value.id,
      questionContent: questionContent.value,
    });
  }
  const data = res.data;
  if (data.code === 0) {
    message.success("编辑应用题目成功, 正在跳转到应用详情页...");
    setTimeout(() => {
      router.push(`/app/detail/${props.appId}`);
    }, 3000);
  } else {
    message.error("编辑应用题目失败," + data.message);
  }
};

const generateQuesByAiSuccess = (result: API.QuestionContentDTO[]) => {
  console.log(result);
  questionContent.value = [...questionContent.value, ...result];
};
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
