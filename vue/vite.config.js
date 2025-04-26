import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  base: '/review/', // 关键配置：所有静态资源路径自动添加 /app/,
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: true
  },
  build: {
    sourcemap: true
  },
  server: {
    proxy: {
      '/public': {
        target: 'http://localhost:80',
        changeOrigin: true
      }
    }
  }
})
