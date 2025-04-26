import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import router from './router'
import App from './App.vue'
import './style.css'

const app = createApp(App)

app.use(ElementPlus)
app.use(router)

app.mount('#app')
