<template>
  <a-modal v-model:visible="visible" @ok="openModal" @cancel="cancelModal">
    <template #title> 分享 </template>
    <div>
      <h4 style="margin: auto 0">复制分享链接</h4>
      <a-typography-paragraph copyable>
        {{ link }}
      </a-typography-paragraph>
      <h4 style="margin: auto 0">手机扫码查看</h4>
      <img :src="qrCode" alt="QR Code" />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
//@ts-ignore
import QRCode from "qrcode";
import { ref, withDefaults, defineExpose, defineProps } from "vue";

/**
 * 定义组件属性类型
 */
interface Props {
  title: string;
  link: string;
}

/**
 * 指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  title: () => "分享",
  link: () => "https://www.badiu.com",
});
const qrCode = ref();

const visible = ref(false);

const openModal = () => {
  visible.value = true;
};
const cancelModal = () => {
  visible.value = false;
};

/**
 * 将打开弹窗函数暴露给父组件
 */
defineExpose({
  openModal,
});

QRCode.toDataURL(props.link)
  .then((url) => {
    console.log(url);
    qrCode.value = url;
  })
  .catch((err) => {
    console.log(err);
  });
</script>

<style scoped></style>
