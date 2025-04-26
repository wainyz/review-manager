<template>
  <div class="content-section history" :style="{ width: width + 'px', display: minimized ? 'none' : 'flex' }">
    <div class="section-header">
      <h3>历史记录</h3>
      <el-button 
        type="text" 
        class="minimize-btn"
        @click="$emit('toggle-minimize')"
      >
        <el-icon><Minus /></el-icon>
      </el-button>
    </div>
    <el-scrollbar>
      <div class="scroll-content">
        <el-empty v-if="!loading && historyRecords.length === 0" description="暂无历史记录" />
        <el-skeleton :rows="5" animated v-if="loading" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="(record, index) in historyRecords"
            :key="index"
            :timestamp="record.timestamp"
            :type="getScoreType(record.score)"
          >
            <el-card class="history-card">
              <template #header>
                <div class="history-card-header">
                  <span>得分: {{ record.score }}%</span>
                  <el-tag :type="getScoreType(record.score)" size="small">
                    {{ getScoreText(record.score) }}
                  </el-tag>
                </div>
              </template>
              <div class="history-card-content">
                <div class="question-stats">
                  <el-descriptions :column="3" border size="small">
                    <el-descriptions-item label="总题数">{{ record.questionCount.total }}</el-descriptions-item>
                    <el-descriptions-item label="已答题">{{ record.questionCount.total }}</el-descriptions-item>
                    <el-descriptions-item label="正确数">{{ record.questionCount.correct }}</el-descriptions-item>
                  </el-descriptions>
                </div>
                <div class="questions-list" v-if="record.questions && record.questions.length > 0">
                  <h4>题目列表：</h4>
                  <el-collapse>
                    <el-collapse-item v-for="(q, qIndex) in record.questions" :key="qIndex">
                      <template #title>
                        <span class="question-title">题目 {{ qIndex + 1 }}</span>
                        <el-tag size="small" :type="q.correct ? 'success' : 'danger'" class="question-tag">
                          {{ q.score > 60 ? '正确' : '错误' }}
                        </el-tag>
                      </template>
                      <div class="question-content">
                        <p>{{ q.question }}</p>
                        <p class="correct-answer">正确答案：{{ q.correct }}</p>
                      </div>
                    </el-collapse-item>
                  </el-collapse>
                </div>
                <div class="advice-section" v-if="record.advice">
                  <h4>改进建议：</h4>
                  <p>{{ record.advice }}</p>
                </div>
                <div class="similar-section" v-if="record.similar">
                  <h4>相关知识：</h4>
                  <p>{{ record.similar }}</p>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { Minus } from '@element-plus/icons-vue'

const props = defineProps({
  historyRecords: {
    type: Array,
    default: () => []
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
  }
})

defineEmits(['toggle-minimize'])

// 获取得分类型
const getScoreType = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

// 获取得分文本
const getScoreText = (score) => {
  if (score >= 80) return '优秀'
  if (score >= 60) return '及格'
  return '不及格'
}
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

.history-card {
  margin-bottom: 16px;
}

.history-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-card-content {
  padding: 8px 0;
}

.question-stats {
  margin-bottom: 16px;
}

.advice-section,
.similar-section {
  margin-top: 12px;
}

.advice-section h4,
.similar-section h4 {
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.advice-section p,
.similar-section p {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.questions-list {
  margin: 16px 0;
}

.question-title {
  margin-right: 8px;
}

.question-tag {
  margin-left: 8px;
}

.question-content {
  padding: 8px 0;
}

.question-content p {
  margin: 4px 0;
}

.correct-answer {
  color: var(--el-color-success);
  font-weight: 500;
}

:deep(.el-collapse-item__header) {
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-collapse-item__content) {
  padding: 8px 16px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

/* 添加得分相关的样式 */
:deep(.el-timeline-item__node) {
  &.is-success {
    background-color: var(--el-color-success);
  }
  &.is-warning {
    background-color: var(--el-color-warning);
  }
  &.is-danger {
    background-color: var(--el-color-danger);
  }
}
</style> 