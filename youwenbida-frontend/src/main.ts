import { createApp } from "vue";
import App from "./App.vue";
import { createPinia } from "pinia";
import router from "./router";
import ArcoVue from "@arco-design/web-vue";
// 额外引入图标库
import ArcoVueIcon from "@arco-design/web-vue/es/icon";
import "@arco-design/web-vue/dist/arco.css";

const pinia = createPinia();
createApp(App)
  .use(pinia)
  .use(ArcoVue)
  .use(router)
  .use(ArcoVueIcon)
  .mount("#app");
