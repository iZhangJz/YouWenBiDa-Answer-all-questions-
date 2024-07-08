<template>
  <h2>热门应用统计</h2>
  <v-chart :option="AppAnswerCountOptions" style="height: 300px" />
  <a-divider></a-divider>
  <h2>应用答题情况分布</h2>
  <a-input-search
    :style="{ width: '320px' }"
    placeholder="搜索应用ID"
    search-button
    @search="(value) => loadAppAnswerResultData(value)"
  >
    <template #button-icon>
      <icon-search />
    </template>
    <template #button-default> 搜索 </template>
  </a-input-search>
  <v-chart :option="AppAnswerResultCountOptions" style="height: 300px" />
</template>

<script setup lang="ts">
import VChart from "vue-echarts";
import "echarts";
import { computed, ref, watchEffect } from "vue";
import API from "@/api";
import message from "@arco-design/web-vue/es/message";
import {
  getAppAnswerResultCountUsingGet,
  getAppTop12UsingGet,
} from "@/api/statisticController";

const appAnswerCountData = ref<API.AppAnswerCountDTO[]>([]);
const appAnswerResultCountData = ref<API.AppAnswerResultCountDTO[]>([]);

const AppAnswerCountOptions = computed(() => {
  return {
    xAxis: {
      type: "category",
      data: appAnswerCountData.value.map((item) => item.appId),
      name: "应用ID",
    },
    yAxis: {
      type: "value",
      name: "回答次数",
    },
    series: [
      {
        data: appAnswerCountData.value.map((item) => item.count),
        type: "bar",
      },
    ],
  };
});

const AppAnswerResultCountOptions = computed(() => {
  return {
    tooltip: {
      trigger: "item",
    },
    legend: {
      orient: "vertical",
      left: "left",
    },
    series: [
      {
        name: "Access From",
        type: "pie",
        radius: "50%",
        data: appAnswerResultCountData.value.map((item) => {
          return {
            value: item.count,
            name: item.resultName,
          };
        }),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
      },
    ],
  };
});

/**
 * 数据加载
 */
const loadAppAnswerData = async () => {
  const res = await getAppTop12UsingGet();
  const data = res.data;
  if (data.code === 0) {
    appAnswerCountData.value = data.data;
  } else {
    message.error("获取数据失败," + data.message);
  }
};

/**
 * 数据加载
 */
const loadAppAnswerResultData = async (appId: string) => {
  if (!appId) {
    return;
  }
  const res = await getAppAnswerResultCountUsingGet({
    appId: appId as any,
  });
  const data = res.data;
  if (data.code === 0) {
    if (data.data.length === 0) {
      message.warning("该应用没有回答结果数据");
    }
    appAnswerResultCountData.value = data.data;
  } else {
    message.error("获取数据失败," + data.message);
  }
};

watchEffect(() => {
  loadAppAnswerData();
  loadAppAnswerResultData("");
});
</script>

<style scoped></style>
