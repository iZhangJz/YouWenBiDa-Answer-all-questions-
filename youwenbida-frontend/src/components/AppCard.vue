<template>
  <a-card class="appCard" hoverable @click="doCardClick">
    <template #actions>
      <span class="icon-hover"> <IconThumbUp /> </span>
      <span class="icon-hover">
        <IconShareInternal @click="doShare" />
        <ShareModal ref="shareModalRef" :link="shareLink"></ShareModal>
      </span>
    </template>
    <template #cover>
      <div
        :style="{
          height: '204px',
          overflow: 'hidden',
        }"
      >
        <img
          :style="{ width: '100%', transform: 'translateY(-20px)' }"
          :alt="app.appName"
          :src="app.appIcon"
        />
      </div>
    </template>
    <a-card-meta
      :title="app.appName"
      :description="app.appDesc || '暂时没有简介'"
    >
      <template #avatar>
        <div
          :style="{ display: 'flex', alignItems: 'center', color: '#1D2129' }"
        >
          <a-avatar
            :size="24"
            :image-url="app.user?.userAvatar"
            :style="{ marginRight: '8px' }"
          />
          <a-typography-text>{{
            app.user?.userName ?? "匿名用户"
          }}</a-typography-text>
        </div>
      </template>
    </a-card-meta>
  </a-card>
</template>

<script setup lang="ts">
import { IconThumbUp, IconShareInternal } from "@arco-design/web-vue/es/icon";
import { withDefaults, defineProps, ref } from "vue";
import API from "@/api";
import { useRouter } from "vue-router";
import ShareModal from "@/components/ShareModal.vue";

interface Props {
  app: API.AppVO;
}

const props = withDefaults(defineProps<Props>(), {
  // app 默认为空对象
  app: () => {
    return {};
  },
});

/**
 * 分享弹窗引用
 */
const shareModalRef = ref();

/**
 * 分享链接
 */
const shareLink = `${window.location.protocol}//${window.location.host}/app/detail/${props.app.id}`;

const doShare = (event: Event) => {
  if (shareModalRef.value) {
    shareModalRef.value.openModal();
  }
  event.stopPropagation();
};

/**
 * 卡片点击事件
 */
const router = useRouter();
const doCardClick = () => {
  router.push(`/app/detail/${props.app.id}`);
};
</script>
<style scoped>
.icon-hover {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  transition: all 0.1s;
}
.icon-hover:hover {
  background-color: rgb(var(--gray-2));
}
.appCard {
  cursor: pointer;
}
</style>
