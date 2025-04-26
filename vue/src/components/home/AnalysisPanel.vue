<template>
  <div class="content-section analysis" :style="{ width: width + 'px', display: minimized ? 'none' : 'flex' }">
    <div class="section-header">
      <h3>分析内容</h3>
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
        <div class="analysis-content" v-if="parsedAnalysis">
          <div class="sort-buttons">
            <el-button-group>
              <el-button
                :type="sortType === 'mastery' ? 'primary' : 'default'"
                @click="handleSort('mastery')"
                size="small"
              >
                <el-icon><Sort /></el-icon>
                按掌握度排序
                <el-icon v-if="sortType === 'mastery'">
                  <CaretTop v-if="sortOrder === 'asc'" />
                  <CaretBottom v-else />
                </el-icon>
              </el-button>
              <el-button
                :type="sortType === 'attenuation' ? 'primary' : 'default'"
                @click="handleSort('attenuation')"
                size="small"
              >
                <el-icon><Sort /></el-icon>
                按衰减等级排序
                <el-icon v-if="sortType === 'attenuation'">
                  <CaretTop v-if="sortOrder === 'asc'" />
                  <CaretBottom v-else />
                </el-icon>
              </el-button>
            </el-button-group>
          </div>
          <div class="analysis-summary">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="知识点覆盖率">
                <el-tag type="success">{{ (parsedAnalysis.coverage * 100).toFixed(1) }}%</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="总体掌握度">
                <el-tag type="primary">{{ (parsedAnalysis.allMastery * 100).toFixed(1) }}%</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div class="mastery-records">
            <el-card v-for="(record, index) in sortedRecords" :key="index" class="record-card" @click="$emit('scroll-to-original', record.raw)">
              <template #header>
                <div class="card-header" >
                  <span >{{ record.raw }} @mark@</span>
                </div>
              </template>
              <div class="record-content">
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="掌握度">
                    <el-tooltip 
                      :content="`掌握度: ${(record.mastery * 100).toFixed(1)}%`" 
                      placement="top" 
                      effect="light"
                    >
                      <el-progress :percentage="record.mastery * 100" :status="getMasteryStatus(record.mastery)" />
                    </el-tooltip>
                  </el-descriptions-item>
                  <el-descriptions-item label="衰减等级">
                    <el-tooltip 
                      :content="`衰减等级: ${record.attenuationLevel}`" 
                      placement="top" 
                      effect="light"
                    >
                      <el-tag :type="getAttenuationType(record.attenuationLevel)">{{ record.attenuationLevel }}</el-tag>
                    </el-tooltip>
                  </el-descriptions-item>
                  <el-descriptions-item label="更新时间">
                    <el-tooltip 
                      :content="`衰减等级更新时间: ${new Date(record.firstTime).toLocaleString()}`" 
                      placement="top" 
                      effect="light"
                    >
                      <span class="time-text">{{ new Date(record.firstTime).toLocaleString() }}</span>
                    </el-tooltip>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Minus, Sort, CaretTop, CaretBottom } from '@element-plus/icons-vue'

const props = defineProps({
  analysis: {
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
  }
})

defineEmits(['toggle-minimize', 'scroll-to-original'])

// 解析分析内容
const parsedAnalysis = computed(() => {
  if (!props.analysis) return null
  try {
    return JSON.parse(props.analysis)
  } catch (error) {
    console.error('解析分析数据失败:', error)
    return null
  }
})

// 排序状态
const sortType = ref('')
const sortOrder = ref('desc')

// 排序后的记录
const sortedRecords = computed(() => {
  if (!parsedAnalysis.value?.masteryRecords) return []
  
  const records = [...parsedAnalysis.value.masteryRecords]
  
  if (sortType.value) {
    records.sort((a, b) => {
      let valueA, valueB
      if (sortType.value === 'mastery') {
        valueA = a.mastery || 0
        valueB = b.mastery || 0
      } else {
        valueA = a.attenuationLevel || 0
        valueB = b.attenuationLevel || 0
      }
      return sortOrder.value === 'asc' ? valueA - valueB : valueB - valueA
    })
  }
  
  return records
})

// 处理排序
const handleSort = (type) => {
  if (sortType.value === type) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortType.value = type
    sortOrder.value = 'desc'
  }
}

// 获取掌握度状态
const getMasteryStatus = (mastery) => {
  const percentage = mastery * 100
  if (percentage >= 80) return 'success'
  if (percentage >= 60) return 'warning'
  return 'exception'
}

// 获取衰减等级类型
const getAttenuationType = (level) => {
  const types = {
    0: 'success',
    1: 'warning',
    2: 'danger'
  }
  return types[level] || 'info'
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

.analysis-content {
  line-height: 1.6;
  user-select: text;
}

.sort-buttons {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
}

.sort-buttons :deep(.el-button-group) {
  display: flex;
  gap: 8px;
}

.sort-buttons :deep(.el-button) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.analysis-content ::selection {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary-dark-2);
}

.analysis-summary {
  margin-bottom: 20px;
}

.mastery-records {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-card {
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.record-card.highlight-target {
  box-shadow: 0 0 0 2px var(--el-color-primary);
  transform: translateY(-2px);
}

.record-content {
  padding: 8px 0;
}

.record-content :deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

.record-content :deep(.el-progress) {
  margin: 8px 0;
}

.record-content :deep(.el-tag) {
  font-size: 14px;
  padding: 4px 8px;
}

.time-text {
  cursor: pointer;
  transition: color 0.3s ease;
}

.time-text:hover {
  color: var(--el-color-primary);
}
#controller_element{
  text-align: left;
}
</style> 