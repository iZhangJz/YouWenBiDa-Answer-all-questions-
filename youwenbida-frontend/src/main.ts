import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import ArcoVue from "@arco-design/web-vue";
// 额外引入图标库
import ArcoVueIcon from "@arco-design/web-vue/es/icon";
import "@arco-design/web-vue/dist/arco.css";

createApp(App).use(ArcoVue).use(router).use(ArcoVueIcon).mount("#app");
