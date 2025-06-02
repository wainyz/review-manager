<template>
  <el-config-provider>
    <router-view></router-view>
  </el-config-provider>
</template>

<script setup>




</script>

<style>
@import 'element-plus/dist/index.css';
@media (max-width: 768px) {
  .el-message {
    width: calc(100% - 40px) !important;
    right: 20px !important;
  }
}
:root {
  /* 浅色主题 */
  --bg-color: #f5f5f5;
  --text-color: #333;
  --card-bg: #fff;
  --border-color: #dcdfe6;
  --hover-color: #f2f6fc;
  --header-bg: #fff;
  --shadow-color: rgba(0, 0, 0, 0.1);
}

:root[data-theme='dark'] {
  /* 深色主题 */
  --bg-color: #1a1a1a;
  --text-color: #e5e5e5;
  --card-bg: #2c2c2c;
  --border-color: #4c4c4c;
  --hover-color: #363636;
  --header-bg: #2c2c2c;
  --shadow-color: rgba(0, 0, 0, 0.3);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: var(--bg-color);
  color: var(--text-color);
  transition: background-color 0.3s, color 0.3s;
}
</style>
