<template>
  <div class="content-section original-text" :style="{ width: width + 'px', display: minimized ? 'none' : 'flex' }">
    <div class="section-header">
      <h3>原文</h3>
      <el-button 
        type="text" 
        class="minimize-btn"
        @click="$emit('toggle-minimize')"
      >
        <el-icon><Minus /></el-icon>
      </el-button>
    </div>
    <el-scrollbar>
      <div class="scroll-content" v-loading="loading">
        <div class="content-text" v-html="parsedContent"></div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import { Minus } from '@element-plus/icons-vue'

const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  width: {
    type: Number,
    default: 300
  },
  minimized: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  highlights: {
    type: Array,
    default: () => []
  }
})

defineEmits(['toggle-minimize'])

const parsedContent = computed(() => {
  if (!props.content) return ''
  
  // 解析 Markdown 内容
  let content = marked.parse(props.content, {
    breaks: true,
    gfm: true,
    headerIds: true,
    mangle: false,
    sanitize: false
  })
  
  // 为每个知识点添加高亮
  props.highlights.forEach((highlight, index) => {
    if (highlight) {
      // 使用正则表达式匹配文本，但排除已经在 HTML 标签内的内容
      const regex = new RegExp(`(${highlight.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})(?![^<]*>)`, 'g')
      content = content.replace(regex, `<span class="highlight" data-raw="$1" data-index="${index}">$1</span>`)
    }
  })
  
  return content
})
</script>

<style scoped>
.content-section {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 var(--el-box-shadow);
  box-sizing: border-box;
  min-width: 0; /* 防止内容溢出 */
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: var(--el-font-size-large);
}

.minimize-btn {
  padding: 4px;
  font-size: 16px;
  color: var(--el-text-color-secondary);
  transition: all 0.3s ease;
}

.minimize-btn:hover {
  color: var(--el-color-primary);
}

.scroll-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background-color: var(--el-bg-color-page);
}

.content-text {
  text-align: left;
  word-wrap: break-word;
  font-family: "Source Han Sans", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  line-height: 1.8;
  font-size: 16px;
  color: var(--el-text-color-primary);
  padding: 32px;
  white-space: pre-wrap;
  background-color: var(--el-bg-color-page);
  border-radius: 8px;
  user-select: text;
}

.content-text ::selection {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary-dark-2);
}

.content-text :deep(p) {
  margin: 1em 0;
  line-height: 1.8;
  padding: 0.5em 1em;
  border-radius: 8px;
  transition: all 0.3s ease;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
}

.content-text :deep(h1) {
  font-size: 2em;
  margin: 1.2em 0 0.8em;
  padding-bottom: 0.3em;
  border-bottom: 2px solid var(--el-border-color);
  color: var(--el-color-primary);
  font-weight: 600;
}

.content-text :deep(h2) {
  font-size: 1.6em;
  margin: 1em 0 0.6em;
  padding-bottom: 0.2em;
  border-bottom: 1px solid var(--el-border-color-lighter);
  color: var(--el-color-primary-light-3);
  font-weight: 600;
}

.content-text :deep(h3) {
  font-size: 1.4em;
  margin: 0.8em 0 0.5em;
  color: var(--el-color-primary-light-5);
  font-weight: 600;
}

.content-text :deep(h4) {
  font-size: 1.2em;
  margin: 0.6em 0 0.4em;
  color: var(--el-text-color-primary);
  font-weight: 600;
}

.content-text :deep(h5) {
  font-size: 1.1em;
  margin: 0.5em 0 0.3em;
  color: var(--el-text-color-regular);
  font-weight: 600;
}

.content-text :deep(h6) {
  font-size: 1em;
  margin: 0.4em 0 0.2em;
  color: var(--el-text-color-secondary);
  font-weight: 600;
}

.content-text :deep(ul),
.content-text :deep(ol) {
  padding-left: 1.5em;
  margin: 0.8em 0;
}

.content-text :deep(li) {
  margin: 0.4em 0;
  line-height: 1.6;
}

.content-text :deep(blockquote) {
  margin: 1em 0;
  padding: 0.8em 1.2em;
  border-left: 4px solid var(--el-color-primary-light-5);
  background-color: var(--el-fill-color-lighter);
  color: var(--el-text-color-regular);
  border-radius: 4px;
  font-style: italic;
}

.content-text :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0 0.2em;
  font-size: 0.9em;
  background-color: var(--el-fill-color-light);
  border-radius: 3px;
  font-family: "Fira Code", Consolas, Monaco, "Andale Mono", monospace;
}

.content-text :deep(pre) {
  margin: 1em 0;
  padding: 1em;
  background-color: var(--el-fill-color-light);
  border-radius: 8px;
  overflow-x: auto;
}

.content-text :deep(pre code) {
  padding: 0;
  margin: 0;
  background-color: transparent;
  border-radius: 0;
  font-size: 0.9em;
  line-height: 1.5;
}

.content-text :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 1.5em 0;
  border-radius: 8px;
  box-shadow: 0 4px 12px var(--el-box-shadow-light);
}

.content-text :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.5em 0;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
}

.content-text :deep(th),
.content-text :deep(td) {
  padding: 12px 16px;
  border: 1px solid var(--el-border-color);
  text-align: left;
}

.content-text :deep(th) {
  background-color: var(--el-fill-color);
  font-weight: 600;
}

.content-text :deep(tr:nth-child(even)) {
  background-color: var(--el-fill-color-lighter);
}

.content-text :deep(tr:hover) {
  background-color: var(--el-fill-color-light);
}

.content-text :deep(a) {
  color: var(--el-color-primary);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s ease;
}

.content-text :deep(a:hover) {
  border-bottom-color: var(--el-color-primary);
}

.content-text :deep(.highlight) {
  background-color: var(--el-color-primary-light-9);
  border-radius: 2px;
  padding: 0 2px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.content-text :deep(.highlight:hover) {
  background-color: var(--el-color-primary-light-7);
}

.content-text :deep(.highlight::after) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: var(--el-color-primary);
  opacity: 0.5;
}

.content-text :deep(.highlight:hover::after) {
  opacity: 1;
}

.content-text :deep(.highlight-active) {
  background-color: var(--el-color-primary-light-5);
  box-shadow: 0 0 0 2px var(--el-color-primary);
  transform: translateY(-1px);
}
</style> 