import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './resources/routes'
import { createI18n } from "vue-i18n";
import { defaultLocale, languages } from './resources/locales'


const pinia = createPinia();
const messages = Object.assign(languages)

const i18n = createI18n({
    legacy: false,
    locale: defaultLocale,
    fallbackLocale: 'en',
    messages
})

const router = createRouter({
    history: createWebHistory(),
    routes
});

createApp(App)
    .use(pinia)
    .use(router)
    .use(i18n)
.mount('#app')
