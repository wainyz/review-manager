<template>
  <div class="footer-container">
    <div class="hover-area"></div>
    <footer class="footer">
      <el-card class="status-card">
        <template #header>
          <div class="card-header">
            <span>学习状态</span>
          </div>
        </template>
        <div class="status-content">
          <p>当前掌握度最低的文档：<el-tag size="small">{{ lowestMasteryDoc || '暂无数据' }}</el-tag></p>
        </div>
      </el-card>
    </footer>
  </div>
</template>

<script setup>
defineProps({
  lowestMasteryDoc: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.footer-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
}

.hover-area {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30px;
}

:root {
  --footer-height: 100px;
}

.footer {
  position: relative;
  background-color: var(--header-bg);
  padding: 10px 20px;
  box-shadow: 0 -2px 4px var(--shadow-color);
  transform: translateY(100%);
  transition: transform 0.3s ease;
  height: var(--footer-height);
}

.footer-container:hover .footer {
  transform: translateY(0);
}

.status-card {
  margin-bottom: 10px;
  background-color: var(--card-bg);
  border-color: var(--border-color);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--text-color);
}
</style>