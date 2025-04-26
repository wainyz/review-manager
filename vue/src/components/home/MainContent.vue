<template>
  <main class="main-content">
    <div v-if="currentView === 'welcome'" class="welcome">
      <el-empty>
        <template #description>
          <p>欢迎使用个人学习助手</p>
          <p>点击上方按钮开始学习之旅</p>
        </template>
      </el-empty>
    </div>
    <div v-else-if="currentView === 'review'" class="review">
      <div class="card-container" v-if="questions.length > 0">
        <el-card 
          v-for="(question, index) in questions" 
          :key="question.id"
          class="question-card"
          :style="{ zIndex: questions.length - index, display: currentQuestionIndex === index ? 'block' : 'none' }"
        >
          <template #header>
            <div class="card-header">
              <span>题目 {{ index + 1 }}</span>
            </div>
          </template>
          <div class="card-content">
            <p>{{ question.content }}</p>
          </div>
          <div class="card-footer">
            <el-button type="primary" @click="nextQuestion">下一题</el-button>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无题目" />
    </div>
    <div v-else-if="currentView === 'profile'" class="profile">
      <!-- 个人信息内容 -->
    </div>
    <div v-else-if="currentView === 'document'" class="document-detail">
      <div class="content-section original-text" :style="{ width: leftPanelWidth + 'px', display: minimizedPanels.left ? 'none' : 'flex' }">
        <div class="section-header">
        <h3>原文</h3>
          <div class="header-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click="generateQuestions"
              :loading="generatingQuestions"
            >
              一键生成题目
            </el-button>
            <el-button 
              type="text" 
              class="minimize-btn"
              @click="toggleMinimize('left')"
            >
              <el-icon><Minus /></el-icon>
            </el-button>
          </div>
        </div>
        <el-scrollbar>
          <div class="scroll-content" v-loading="loading">
            <div class="content-text" v-html="parsedContent" @mouseup="handleTextSelection"></div>
          </div>
        </el-scrollbar>
      </div>
      <el-divider direction="vertical" class="resizer left-resizer" @mousedown="startResize('left', $event)" v-show="!minimizedPanels.left" />
      <div class="content-section history" :style="{ width: middlePanelWidth + 'px', display: minimizedPanels.middle ? 'none' : 'flex' }">
        <div class="section-header">
        <h3>历史记录</h3>
          <el-button 
            type="text" 
            class="minimize-btn"
            @click="toggleMinimize('middle')"
          >
            <el-icon><Minus /></el-icon>
          </el-button>
        </div>
        <el-scrollbar>
          <div class="scroll-content">
            <el-empty v-if="!historyLoading && historyRecords.length === 0" description="暂无历史记录" />
            <el-skeleton :rows="5" animated v-if="historyLoading" />
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
                      <div class="history-date">{{ record.timestamp }}</div>
                      <div class="history-score">
                        <span>得分: {{ record.score }}%</span>
                        <el-tag :type="getScoreType(record.score)" size="small">
                          {{ getScoreText(record.score) }}
                        </el-tag>
                      </div>
                    </div>
                  </template>
                  <div class="history-card-content">
                    <div class="question-stats">
                      <el-descriptions :column="3" border size="small">
                        <el-descriptions-item label="总题数">{{ record.questionCount.total }}</el-descriptions-item>
                        <el-descriptions-item label="已答题">{{ record.questionCount.answered }}</el-descriptions-item>
                        <el-descriptions-item label="正确数">{{ record.questionCount.correct }}</el-descriptions-item>
                      </el-descriptions>
                    </div>
                    <div class="history-questions">
                      <div v-for="(question, qIndex) in record.questions" :key="qIndex" 
                           class="history-question" @click="toggleQuestionExpand(record.timestamp + qIndex)" >
                        <div class="question-header">
                          <div class="question-basic-info"   >
                            <span class="question-number" >题目 {{qIndex + 1}}</span>
                            <span :class="['question-status', question.score > 60 ? 'correct' : 'incorrect']">
                              {{question.score > 60 ? '正确' : '错误'}}
                            </span>
                            <span class="question-score">得分: {{question.score || 0}}分</span>
                          </div>
                          <el-icon class="expand-icon" :class="{ 'is-expanded': expandedQuestions.has(record.timestamp + qIndex) }">
                            <ArrowDown />
                          </el-icon>
                        </div>
                        
                        <div class="question-details" v-show="expandedQuestions.has(record.timestamp + qIndex)" @click.stop="handleHistoryQuestionClick(question.raw)">
                          <div class="question-content">{{question.question}}</div>
                          <div class="question-answer">
                            <div class="correct-answer">正确答案: {{question.correct}}</div>
                            <div class="user-answer">你的答案: {{question.userAnswer}}</div>
                          </div>
                          <div class="question-explanation">{{question.explanation}}</div>
                          <div class="question-actions">
                            <el-button 
                              type="primary" 
                              size="small" 
                              @click.stop="handleHistoryQuestionClick(question.raw)"
                            >
                              查看原文
                            </el-button>
                          </div>
                        </div>
                      </div>
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
      <el-divider direction="vertical" class="resizer middle-resizer" @mousedown="startResize('middle', $event)" v-show="!minimizedPanels.middle" />
      <div class="content-section analysis" :style="{ width: rightPanelWidth + 'px', display: minimizedPanels.right ? 'none' : 'flex' }">
        <div class="section-header">
        <h3>分析内容</h3>
          <el-button 
            type="text" 
            class="minimize-btn"
            @click="toggleMinimize('right')"
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
                    <el-tag type="success">{{ (parsedAnalysis.coverage).toFixed(1) }}%</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="总体掌握度">
                    <el-tag type="primary">{{ (parsedAnalysis.allMastery).toFixed(1) }}%</el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <div class="mastery-records">
                <el-card 
                  v-for="(record, index) in parsedAnalysis.masteryRecords" 
                  :key="index" 
                  class="record-card"
                >
                  <template #header>
                    <div class="card-header" @click="handleHistoryQuestionClick(record.raw)">
                      <span>{{ record.raw }} #mark#</span>
                      <div class="button-group">
                        <el-button 
                          type="primary" 
                          size="small" 
                          @click="addAnalysisToReviewQueue(record.raw)"
                        >
                          加入复习队列
                        </el-button>
                      </div>
                    </div>
                  </template>
                  <div class="record-content">
                    <el-descriptions :column="1" border>
                      <el-descriptions-item label="掌握度">
                        <el-tooltip 
                          :content="`掌握度: ${(record.mastery).toFixed(1)}%`" 
                          placement="top" 
                          effect="light"
                        >
                          <el-progress :percentage="record.mastery" :status="getMasteryStatus(record.mastery)" />
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
                          :content="`衰减等级更新时间: ${formatEpochSeconds(record.firstTime)}`" 
                          placement="top" 
                          effect="light"
                        >
                          <span class="time-text">{{ formatEpochSeconds(record.firstTime) }}</span>
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
      <el-divider direction="vertical" class="resizer right-resizer" @mousedown="startResize('right', $event)" v-show="!minimizedPanels.right" />
      <div class="content-section quiz" :style="{ width: quizPanelWidth + 'px', display: minimizedPanels.quiz ? 'none' : 'flex' }">
        <div class="section-header">
          <h3>答题与题目生成</h3>
          <div class="header-actions">
            <el-button 
              type="text" 
              class="minimize-btn"
              @click="toggleMinimize('quiz')"
            >
              <el-icon><Minus /></el-icon>
            </el-button>
          </div>
        </div>
        <el-scrollbar>
          <div class="scroll-content">
            <div class="quiz-content">
              <!-- 答题栏 -->
              <div class="quiz-panel" v-if="isAnswerVisible">
                <div class="panel-header">
                  <h4>答题区</h4>
                </div>
                <div class="panel-content">
                  <el-empty v-if="!currentQuiz && !quizLoading" description="暂无题目" />
                  <el-skeleton :rows="5" animated v-if="quizLoading" />
                  <div v-else-if="currentQuiz" class="quiz-card">
                    <div class="quiz-header">
                      <div class="quiz-title">
                        <h4>问题：</h4>
                        <p>{{ currentQuiz.question }}</p>
                      </div>
                      <div class="quiz-progress">
                        <span>题目 {{ currentQuizIndex + 1 }}/{{ quizList.length }}</span>
                      </div>
                    </div>
                    
                    <div class="quiz-navigation">
                      <el-button 
                        type="primary" 
                        plain
                        :icon="ArrowLeft"
                        @click="prevQuiz" 
                        :disabled="currentQuizIndex === 0"
                      >
                        上一题
                      </el-button>
                      <el-button 
                        type="primary" 
                        plain
                        :icon="ArrowRight"
                        @click="nextQuiz" 
                        :disabled="currentQuizIndex === quizList.length - 1"
                      >
                        下一题
                      </el-button>
                    </div>
                    
                    <div class="quiz-answer">
                      <el-input
                        v-model="userAnswer"
                        type="textarea"
                        :rows="3"
                        placeholder="请输入你的答案..."
                        :disabled="showAnswer"
                      />
                    </div>
                    
                    <div class="quiz-actions">
                      <el-button 
                        type="info" 
                        @click="toggleShowAnswer" 
                      >
                        {{ showAnswer ? '隐藏答案' : '查看答案' }}
                      </el-button>
                      <el-button 
                        type="primary" 
                        @click="checkAnswer" 
                        :disabled="!userAnswer || showAnswer"
                      >
                        检查对错
                      </el-button>
                      <el-button 
                        type="success" 
                        @click="submitAllAnswers" 
                        :disabled="isSubmitting || !props.documentId"
                      >
                        {{ isSubmitting ? '提交中...' : '提交做题记录' }}
                      </el-button>
                    </div>
                    
                    <div class="quiz-result" v-if="showAnswer">
                      <div class="result-header">
                        <el-tag :type="isCorrect ? 'success' : 'danger'" size="small">
                          {{ isCorrect ? '回答正确' : '回答错误' }}
                        </el-tag>
                        <span class="correct-answer">正确答案：{{ currentQuiz.correct }}</span>
                      </div>
                      <div class="result-explanation" v-if="currentQuiz.explanation">
                        <h5>解释：</h5>
                        <p>{{ currentQuiz.explanation }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 题目生成栏 -->
              <div class="quiz-panel">
                <div class="panel-header">
                  <h4>题目生成区</h4>
                </div>
                <div class="panel-content">
                  <div class="generation-form">
                    <el-form :model="generationForm" label-width="100px">
                      <el-form-item label="自定义提示词">
                        <el-input
                          v-model="generationForm.customPrompt"
                          type="textarea"
                          :rows="3"
                          placeholder="请输入自定义提示词..."
                        />
                      </el-form-item>
                      <el-form-item label="生成题目数量">
                        <el-input-number
                          v-model="generationForm.questionCount"
                          :min="1"
                          :max="10"
                          :step="1"
                        />
                      </el-form-item>
                    </el-form>
                    <div class="generation-actions">
                      <el-button 
                        type="primary" 
                        @click="generateQuestions"
                        :loading="generatingQuestions"
                      >
                        生成题目
                      </el-button>
                    </div>
                  </div>
                  
                  <!-- 复习队列 -->
                  <div class="review-queue-section">
                    <div class="queue-header">
                      <h4>复习队列</h4>
    
                    </div>
                    <el-card v-for="(item, index) in reviewQueue" :key="index" class="queue-item">
                      <div class="queue-item-content">
                        <p>{{ item.text }}</p>
                        <el-button 
                          type="danger" 
                          size="small" 
                          circle
                          @click="removeFromQueue(index)"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </el-card>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>
    <!-- 修改最小化面板的选项栏 -->
    <div class="minimized-panels">
      <div 
        v-for="(minimized, panel) in minimizedPanels" 
        :key="panel"
        class="minimized-panel-item"
        :class="{ 'is-minimized': minimized }"
        @click="toggleMinimize(panel)"
      >
        {{ getPanelTitle(panel) }}
      </div>
    </div>
    
    <!-- 添加选中文本的弹出菜单 -->
    <div 
      v-if="showSelectionMenu" 
      class="selection-menu"
      :style="{ top: selectionMenuPosition.top + 'px', left: selectionMenuPosition.left + 'px' }"
    >
      <el-button 
        type="primary" 
        size="small" 
        @click="addToReviewQueue"
        class="review-queue-btn"
      >
        <el-icon class="menu-icon"><Plus /></el-icon>
        加入复习队列
      </el-button>
    </div>
  </main>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onUnmounted, computed } from 'vue'
import { Sort, CaretTop, CaretBottom, Minus, ArrowLeft, ArrowRight, Delete } from '@element-plus/icons-vue'
import { getFullUrl, API_PATHS } from '../../config/api'
import request from '../../config/axios'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  currentView: {
    type: String,
    default: 'welcome'
  },
  questions: {
    type: Array,
    default: () => []
  },
  documentId: {
    type: Number,
    default: 0
  }
})

// 监听documentId变化
watch(() => props.documentId, (newId) => {
  if (newId) {
    handleDocumentClick(newId)
  }
})

// 修改面板宽度状态
const totalWidth = ref(window.innerWidth - 32)
const panelWidth = ref(Math.floor(totalWidth.value / 4)) // 将总宽度四等分
const leftPanelWidth = ref(panelWidth.value)
const middlePanelWidth = ref(panelWidth.value)
const rightPanelWidth = ref(panelWidth.value)
const quizPanelWidth = ref(totalWidth.value - panelWidth.value * 3)

// 监听窗口大小变化，动态调整面板宽度
window.addEventListener('resize', () => {
  totalWidth.value = window.innerWidth - 32
  updatePanelWidths()
})

// 更新面板宽度的函数
const updatePanelWidths = () => {
  const visiblePanels = Object.values(minimizedPanels.value).filter(minimized => !minimized).length
  if (visiblePanels === 0) return // 如果所有面板都最小化了，不更新宽度
  
  // 计算基础宽度和剩余宽度
  const baseWidth = Math.floor(totalWidth.value / visiblePanels)
  const remainingWidth = totalWidth.value - (baseWidth * visiblePanels)
  
  // 重置所有面板宽度
  leftPanelWidth.value = 0
  middlePanelWidth.value = 0
  rightPanelWidth.value = 0
  quizPanelWidth.value = 0
  
  // 根据可见性分配宽度
  if (!minimizedPanels.value.left) {
    leftPanelWidth.value = baseWidth + (remainingWidth > 0 ? 1 : 0)
  }
  if (!minimizedPanels.value.middle) {
    middlePanelWidth.value = baseWidth + (remainingWidth > 1 ? 1 : 0)
  }
  if (!minimizedPanels.value.right) {
    rightPanelWidth.value = baseWidth + (remainingWidth > 2 ? 1 : 0)
  }
  if (!minimizedPanels.value.quiz) {
    quizPanelWidth.value = totalWidth.value - (leftPanelWidth.value + middlePanelWidth.value + rightPanelWidth.value)
  }
  
  // 确保每个面板的最小宽度
  const minWidth = 200
  if (leftPanelWidth.value > 0 && leftPanelWidth.value < minWidth) leftPanelWidth.value = minWidth
  if (middlePanelWidth.value > 0 && middlePanelWidth.value < minWidth) middlePanelWidth.value = minWidth
  if (rightPanelWidth.value > 0 && rightPanelWidth.value < minWidth) rightPanelWidth.value = minWidth
  if (quizPanelWidth.value > 0 && quizPanelWidth.value < minWidth) quizPanelWidth.value = minWidth
  
  // 重新分配剩余空间
  const totalAssignedWidth = leftPanelWidth.value + middlePanelWidth.value + rightPanelWidth.value + quizPanelWidth.value
  if (totalAssignedWidth < totalWidth.value) {
    const extraWidth = totalWidth.value - totalAssignedWidth
    const visibleCount = [leftPanelWidth, middlePanelWidth, rightPanelWidth, quizPanelWidth]
      .filter(width => width.value > 0).length
    if (visibleCount > 0) {
      const extraPerPanel = Math.floor(extraWidth / visibleCount)
      const extraRemaining = extraWidth % visibleCount
      
      if (leftPanelWidth.value > 0) leftPanelWidth.value += extraPerPanel + (extraRemaining > 0 ? 1 : 0)
      if (middlePanelWidth.value > 0) middlePanelWidth.value += extraPerPanel + (extraRemaining > 1 ? 1 : 0)
      if (rightPanelWidth.value > 0) rightPanelWidth.value += extraPerPanel + (extraRemaining > 2 ? 1 : 0)
      if (quizPanelWidth.value > 0) quizPanelWidth.value += extraPerPanel + (extraRemaining > 3 ? 1 : 0)
    }
  }
  
  // 特殊处理：当只剩下原文和分析内容时
  if (!minimizedPanels.value.left && !minimizedPanels.value.right && 
      minimizedPanels.value.middle && minimizedPanels.value.quiz) {
    // 平均分配宽度
    const halfWidth = Math.floor(totalWidth.value / 2)
    leftPanelWidth.value = halfWidth
    rightPanelWidth.value = totalWidth.value - halfWidth
  }
}

// 修改拖拽状态
const isResizing = ref(false)
const currentResizer = ref(null)
const startX = ref(0)
const startLeftWidth = ref(0)
const startMiddleWidth = ref(0)
const startRightWidth = ref(0)
const startQuizWidth = ref(0)

// 修改开始拖拽函数
const startResize = (resizer, event) => {
  isResizing.value = true
  currentResizer.value = resizer
  startX.value = event.clientX
  startLeftWidth.value = leftPanelWidth.value
  startMiddleWidth.value = middlePanelWidth.value
  startRightWidth.value = rightPanelWidth.value
  startQuizWidth.value = quizPanelWidth.value
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', stopResize)
}

// 修改处理拖拽函数
const handleMouseMove = (event) => {
  if (!isResizing.value) return
  
  const diff = event.clientX - startX.value
  const minWidth = 200
  
  if (currentResizer.value === 'left') {
    const newLeftWidth = startLeftWidth.value + diff
    const newMiddleWidth = startMiddleWidth.value - diff
    if (newLeftWidth >= minWidth && newMiddleWidth >= minWidth) {
      leftPanelWidth.value = newLeftWidth
      middlePanelWidth.value = newMiddleWidth
      // 更新其他面板的宽度
      if (!minimizedPanels.value.right && !minimizedPanels.value.quiz) {
        const remainingWidth = totalWidth.value - (leftPanelWidth.value + middlePanelWidth.value)
        rightPanelWidth.value = Math.floor(remainingWidth / 2)
        quizPanelWidth.value = remainingWidth - rightPanelWidth.value
      } else if (!minimizedPanels.value.right && minimizedPanels.value.quiz) {
        // 当只剩下原文、历史记录和分析内容时
        rightPanelWidth.value = totalWidth.value - (leftPanelWidth.value + middlePanelWidth.value)
      } else if (minimizedPanels.value.middle && !minimizedPanels.value.right && !minimizedPanels.value.quiz) {
        // 当只剩下原文、分析内容和答题时
        const remainingWidth = totalWidth.value - leftPanelWidth.value
        rightPanelWidth.value = Math.floor(remainingWidth / 2)
        quizPanelWidth.value = remainingWidth - rightPanelWidth.value
      } else if (minimizedPanels.value.middle && minimizedPanels.value.quiz && !minimizedPanels.value.right) {
        // 当只剩下原文和分析内容时
        rightPanelWidth.value = totalWidth.value - leftPanelWidth.value
      }
    }
  } else if (currentResizer.value === 'middle') {
    const newMiddleWidth = startMiddleWidth.value + diff
    const newRightWidth = startRightWidth.value - diff
    if (newMiddleWidth >= minWidth && newRightWidth >= minWidth) {
      middlePanelWidth.value = newMiddleWidth
      rightPanelWidth.value = newRightWidth
      // 更新其他面板的宽度
      if (!minimizedPanels.value.left && !minimizedPanels.value.quiz) {
        const remainingWidth = totalWidth.value - (middlePanelWidth.value + rightPanelWidth.value)
        leftPanelWidth.value = Math.floor(remainingWidth / 2)
        quizPanelWidth.value = remainingWidth - leftPanelWidth.value
      } else if (!minimizedPanels.value.left && minimizedPanels.value.quiz) {
        // 当只剩下原文、历史记录和分析内容时
        leftPanelWidth.value = totalWidth.value - (middlePanelWidth.value + rightPanelWidth.value)
      } else if (minimizedPanels.value.left && !minimizedPanels.value.quiz && !minimizedPanels.value.right) {
        // 当只剩下历史记录、分析内容和答题时
        const remainingWidth = totalWidth.value - middlePanelWidth.value
        rightPanelWidth.value = Math.floor(remainingWidth / 2)
        quizPanelWidth.value = remainingWidth - rightPanelWidth.value
      } else if (minimizedPanels.value.left && minimizedPanels.value.quiz && !minimizedPanels.value.right) {
        // 当只剩下历史记录和分析内容时
        rightPanelWidth.value = totalWidth.value - middlePanelWidth.value
      }
    }
  } else if (currentResizer.value === 'right') {
    const newRightWidth = startRightWidth.value + diff
    const newQuizWidth = startQuizWidth.value - diff
    if (newRightWidth >= minWidth && newQuizWidth >= minWidth) {
      rightPanelWidth.value = newRightWidth
      quizPanelWidth.value = newQuizWidth
      // 更新其他面板的宽度
      if (!minimizedPanels.value.left && !minimizedPanels.value.middle) {
        const remainingWidth = totalWidth.value - (rightPanelWidth.value + quizPanelWidth.value)
        leftPanelWidth.value = Math.floor(remainingWidth / 2)
        middlePanelWidth.value = remainingWidth - leftPanelWidth.value
      } else if (!minimizedPanels.value.left && minimizedPanels.value.middle) {
        // 当只剩下原文、分析内容和答题时
        leftPanelWidth.value = totalWidth.value - (rightPanelWidth.value + quizPanelWidth.value)
      } else if (minimizedPanels.value.left && !minimizedPanels.value.middle) {
        // 当只剩下历史记录、分析内容和答题时
        const remainingWidth = totalWidth.value - (rightPanelWidth.value + quizPanelWidth.value)
        middlePanelWidth.value = remainingWidth
      } else if (minimizedPanels.value.left && minimizedPanels.value.middle) {
        // 当只剩下分析内容和答题时
        const remainingWidth = totalWidth.value - (rightPanelWidth.value + quizPanelWidth.value)
        // 这里不需要分配，因为已经是最小化了
      }
    }
  }
}

// 停止拖拽
const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', stopResize)
}

// 组件卸载时移除事件监听
onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', stopResize)
})

const documentData = ref({
  content: '',
  history: [],
  analysis: ''
})

// 添加历史记录相关的状态
const historyLoading = ref(false)
const historyRecords = ref([])

const parsedAnalysis = computed(() => {
  if (!documentData.value.analysis) return null
  try {
    console.log('解析前的分析内容:', documentData.value.analysis)
    const parsed = JSON.parse(documentData.value.analysis)
    console.log('解析后的分析内容:', parsed)
    return parsed
  } catch (error) {
    console.error('解析分析数据失败:', error)
    return null
  }
})

const getMasteryStatus = (mastery) => {
  if (mastery >= 80) return 'success'
  if (mastery >= 60) return 'warning'
  return 'exception'
}

const getAttenuationType = (level) => {
  const types = {
    0: 'success',
    1: 'warning',
    2: 'danger'
  }
  return types[level] || 'info'
}

const parsedContent = computed(() => {
  if (!documentData.value.content) return ''
  
  try {
    // 获取所有需要高亮的知识点
    const highlights = parsedAnalysis.value?.masteryRecords?.map(record => record.raw) || []
    
    // 直接使用原文内容，不需要解析为 Markdown
    let content = documentData.value.content
    
    // 为每个知识点添加高亮
    highlights.forEach((highlight, index) => {
      if (highlight) {
        // 使用正则表达式匹配文本，但排除已经在 HTML 标签内的内容
        const regex = new RegExp(`(${highlight.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})(?![^<]*>)`, 'g')
        content = content.replace(regex, `<span class="highlight" data-raw="$1" data-index="${index}">$1</span>`)
      }
    })
    
    return content
  } catch (error) {
    console.error('处理原文内容失败:', error)
    return documentData.value.content
  }
})

// 添加点击事件处理函数
const handleHighlightClick = (event) => {
  // 检查点击的元素是否是高亮元素或其子元素
  let target = event.target
  while (target && !target.classList.contains('highlight')) {
    target = target.parentElement
  }
  
  if (!target) return
  
  const rawValue = target.getAttribute('data-raw')
  const index = parseInt(target.getAttribute('data-index'))
  
  // 找到分析内容栏的滚动容器
  const analysisScrollbar = document.querySelector('.analysis .el-scrollbar__wrap')
  if (!analysisScrollbar) return
  
  // 找到对应的分析内容卡片
  const analysisCards = document.querySelectorAll('.record-card')
  if (analysisCards && analysisCards[index]) {
    // 计算目标卡片的位置
    const targetCard = analysisCards[index]
    const cardTop = targetCard.offsetTop
    const scrollbarHeight = analysisScrollbar.clientHeight
    
    // 滚动到对应的卡片，使其居中显示
    analysisScrollbar.scrollTo({
      top: cardTop - (scrollbarHeight / 2) + (targetCard.clientHeight / 2),
      behavior: 'smooth'
    })
    
    // 添加临时高亮效果
    targetCard.classList.add('highlight-target')
    setTimeout(() => {
      targetCard.classList.remove('highlight-target')
    }, 2000)
  }
}

// 在组件挂载后添加事件监听
onMounted(() => {
  // 添加点击事件监听
  document.addEventListener('click', handleHighlightClick)
  document.addEventListener('click', closeSelectionMenu)
  
  // 如果已经有documentId，则自动加载文档数据
  if (props.documentId && props.currentView === 'document') {
    handleDocumentClick(props.documentId)
  }
})

// 在组件卸载前移除事件监听
onUnmounted(() => {
  document.removeEventListener('click', handleHighlightClick)
  document.removeEventListener('click', closeSelectionMenu)
})

const loading = ref(false)
const sortType = ref('')
const currentQuestionIndex = ref(0)

const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
  } else {
    currentQuestionIndex.value = 0;
  }
};
const sortOrder = ref('desc')

const handleSort = (type) => {
  if (!parsedAnalysis.value?.masteryRecords) return
  
  if (sortType.value === type) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortType.value = type
    sortOrder.value = 'desc'
  }
  
  const records = [...parsedAnalysis.value.masteryRecords]
  records.sort((a, b) => {
    let valueA, valueB
    if (type === 'mastery') {
      valueA = a.mastery || 0
      valueB = b.mastery || 0
    } else {
      valueA = a.attenuationLevel || 0
      valueB = b.attenuationLevel || 0
    }
    return sortOrder.value === 'asc' ? valueA - valueB : valueB - valueA
  })
  
  // 更新排序后的数据
  documentData.value = {
    ...documentData.value,
    analysis: JSON.stringify({
    ...parsedAnalysis.value,
      masteryRecords: records
    })
  }
}

// 添加答题相关的状态
const currentQuiz = ref(null)
const userAnswer = ref('')
const quizList = ref([])
const currentQuizIndex = ref(0)
const quizLoading = ref(false)
const showAnswer = ref(false)
const hasSubmitted = ref(false)
const isCorrect = ref(false)
const isSubmitting = ref(false)
const userAnswers = ref([]) // 存储用户的所有答案

// 计算是否有下一题
const hasNextQuiz = computed(() => {
  return currentQuizIndex.value < quizList.value.length - 1
})

// 上一题
const prevQuiz = () => {
  if (currentQuizIndex.value > 0) {
    // 保存当前答案
    saveCurrentAnswer()
    
    currentQuizIndex.value--
    currentQuiz.value = quizList.value[currentQuizIndex.value]
    
    // 恢复该题的答案状态
    restoreAnswerState()
  }
}

// 下一题
const nextQuiz = () => {
  if (currentQuizIndex.value < quizList.value.length - 1) {
    // 保存当前答案
    saveCurrentAnswer()
    
    currentQuizIndex.value++
    currentQuiz.value = quizList.value[currentQuizIndex.value]
    
    // 恢复该题的答案状态
    restoreAnswerState()
  }
}

// 保存当前答案状态
const saveCurrentAnswer = () => {
  if (currentQuizIndex.value >= 0 && currentQuizIndex.value < quizList.value.length) {
    userAnswers.value[currentQuizIndex.value] = {
      answer: userAnswer.value,
      hasViewedAnswer: showAnswer.value
    }
  }
}

// 恢复答案状态
const restoreAnswerState = () => {
  const savedState = userAnswers.value[currentQuizIndex.value] || { answer: '', hasViewedAnswer: false }
  userAnswer.value = savedState.answer
  showAnswer.value = savedState.hasViewedAnswer
  
  // 如果已经查看过答案，则标记为未作答
  if (savedState.hasViewedAnswer) {
    hasSubmitted.value = false
    isCorrect.value = false
  } else if (savedState.answer) {
    // 如果已经提交过答案，检查是否正确
    isCorrect.value = savedState.answer.trim().toLowerCase() === currentQuiz.value.correct.trim().toLowerCase()
    hasSubmitted.value = true
  } else {
    hasSubmitted.value = false
    isCorrect.value = false
  }
}

// 检查答案
const checkAnswer = () => {
  if (!userAnswer.value) return
  
  // 检查答案是否正确
  isCorrect.value = userAnswer.value.trim().toLowerCase() === currentQuiz.value.correct.trim().toLowerCase()
  
  // 显示答案结果
  ElMessage({
    type: isCorrect.value ? 'success' : 'error',
    message: isCorrect.value ? '回答正确！' : '回答错误'
  })
  
  // 标记已提交
  hasSubmitted.value = true
  
  // 显示答案
  showAnswer.value = true
  
  // 保存当前答案状态
  saveCurrentAnswer()
  
  // 如果有下一题，自动进入下一题
  if (hasNextQuiz.value && isCorrect.value) {
    setTimeout(() => {
      nextQuiz()
    }, 2000)
  }
}

// 切换显示答案
const toggleShowAnswer = () => {
  showAnswer.value = !showAnswer.value
  
  // 如果显示答案，检查用户是否已经输入了答案
  if (showAnswer.value && userAnswer.value) {
    // 检查答案是否正确
    isCorrect.value = userAnswer.value.trim().toLowerCase() === currentQuiz.value.correct.trim().toLowerCase()
  }
  
  // 如果查看答案，标记为未作答
  if (showAnswer.value) {
    hasSubmitted.value = false
  }
  
  // 保存当前状态
  saveCurrentAnswer()
}

// 获取当前题目
const fetchCurrentQuestion = async (fileId) => {
  if (!fileId) return
  
  try {
    quizLoading.value = true
    const response = await request.get(getFullUrl(API_PATHS.GET_CURRENT_QUESTION), { 
      params: { file_id: fileId } 
    })
    
    if (response.data && response.data.data) {
      try {
        // 解析JSON字符串
        const questionData = JSON.parse(response.data.data)
        console.log('题目数据:', questionData)
        
        // 检查数据格式
        if (!questionData.questions || !Array.isArray(questionData.questions) || questionData.questions.length === 0) {
          throw new Error('题目数据格式错误')
        }
        
        // 更新当前题目
        currentQuiz.value = {
          id: Date.now(),
          question: questionData.questions[0].question || '',
          correct: questionData.questions[0].correct || '',
          explanation: ''
        }
        
        // 更新题目列表
        quizList.value = questionData.questions.map(q => ({
          id: Date.now() + Math.random(),
          question: q.question || '',
          correct: q.correct || '',
          explanation: ''
        }))
        
        // 初始化答案数组
        userAnswers.value = new Array(quizList.value.length).fill(null).map(() => ({
          answer: '',
          hasViewedAnswer: false
        }))
        
        currentQuizIndex.value = 0
        userAnswer.value = ''
        showAnswer.value = false
        hasSubmitted.value = false
        isCorrect.value = false
      } catch (error) {
        console.error('解析题目数据失败:', error)
        ElMessage.error('解析题目数据失败')
        currentQuiz.value = null
        quizList.value = []
        userAnswers.value = []
      }
    } else {
      console.error('题目数据格式错误:', response.data)
      ElMessage.error('题目数据格式错误')
      currentQuiz.value = null
      quizList.value = []
      userAnswers.value = []
    }
  } catch (error) {
    console.error('获取题目失败:', error)
    // 检查是否是401错误
    if (error.response && error.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error('获取题目失败')
    currentQuiz.value = null
    quizList.value = []
    userAnswers.value = []
  } finally {
    quizLoading.value = false
  }
}

// 修改文档点击处理函数
const handleDocumentClick = async (fileId) => {
  if (!fileId || props.currentView !== 'document') return
  
  // 确保fileId不为空
  if (fileId) {
    await fetchDocumentData(fileId)
  } else {
    ElMessage.error('文档ID不能为空')
  }
}

// 处理token失效的函数
const handleTokenExpired = () => {
  ElMessage.error('登录已过期，请重新登录')
  // 清除本地存储的token
  localStorage.removeItem('token')
  // 跳转到登录页面
  router.push('/login')
}

// 修改获取文档数据函数，添加token失效处理
const fetchDocumentData = async (fileId) => {
  if (!fileId) {
    ElMessage.error('文档ID不能为空')
    return
  }
  
  try {
    // 显示加载状态
    loading.value = true
    historyLoading.value = true
    quizLoading.value = true
    
    // 分别处理四个请求
    try {
      // 获取原文
      const originalRes = await request.get(getFullUrl(API_PATHS.GET_ORIGINAL_FILE), { 
        params: { file_id: fileId } 
      })
      
      // 检查token是否失效
      if (originalRes.data && originalRes.data.code === 401) {
        handleTokenExpired()
        return
      }
      
      if (originalRes.data && originalRes.data.data) {
        console.log('原文数据:', originalRes.data.data)
        // 直接使用原文内容，只去除转义字符
        documentData.value.content = originalRes.data.data.replace(/\\n/g, '\n').replace(/\\"/g, '"').replace(/\\'/g, "'").replace(/\\\\/g, '\\')
      } else {
        console.error('原文数据格式错误:', originalRes.data)
        ElMessage.error('原文数据格式错误')
        documentData.value.content = ''
    }
  } catch (error) {
      console.error('获取原文失败:', error)
      // 检查是否是401错误
      if (error.response && error.response.status === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error('获取原文失败')
      documentData.value.content = ''
    }
    
    try {
      // 获取历史记录
      const historyRes = await request.get(getFullUrl(API_PATHS.GET_HISTORY_FILE), { 
        params: { file_id: fileId } 
      })
      
      // 检查token是否失效
      if (historyRes.data && historyRes.data.code === 401) {
        handleTokenExpired()
        return
      }
      
      if (historyRes.data != null) {
        try {
          // 解析JSON字符串为数组
          console.log('历史记录原始数据:', historyRes.data.data)
          const historyArray = JSON.parse(historyRes.data.data)
          
          historyRecords.value = historyArray.map(record => {
            try {
              // 解析时间戳
              const timestamp = record.timestamp
              // 将EpochSeconds转换为毫秒
              const milliseconds = timestamp
              const date = new Date(milliseconds)
              
              // 解析content字段
              const content = record.content
              if (!content || !content.questions || !content.scoringResult ||!content.userAnswer) {
                console.error('历史记录项数据结构不完整:', record)
                return null
              }
              
              // 获取题目信息
              const questions = content.questions.questions || []
              const questionNum = content.questions.questionNum || 0
              
              // 获取评分结果
              const scoringResponse = content.scoringResult
              const scores = scoringResponse.scores || []
              
              // 计算得分
              const score = scores.reduce((total, score) => total + score, 0) / scores.length
              
              // 构建历史记录对象
              return {
                timestamp: date.toLocaleString(),
                score: score,
                advice: scoringResponse.advice || '',
                similar: scoringResponse.similar || '',
                questionCount: {
                  total: questionNum,
                  answered: scores.length,
                  correct: scores.filter(s => s > 60).length
                }
                ,
                // 将题目和评分结果一一对应
                questions: questions.map((q, index) => {
                  const score = scores[index] || 0
                  return {
                    question: q.question || '',
                    correct: q.correct || '',
                    score: score || 0,
                    userAnswer: content.userAnswer[index] || '',
                    raw: q.raw || '' // 添加raw字段，用于定位原文片段
                  }
                })
              }
            } catch (error) {
              console.error('处理历史记录项失败:', error, record)
              return null
            }
          }).filter(record => record !== null).sort((a, b) => {
            // 按时间戳降序排序
            return new Date(b.timestamp) - new Date(a.timestamp)
          })
          
          console.log('处理后的历史记录:', historyRecords.value)
        } catch (error) {
          console.error('解析历史记录JSON失败:', error)
          //ElMessage.error('解析历史记录失败')
          historyRecords.value = []
        }
      } else {
        console.error('历史记录数据格式错误:', historyRes.data)
        ElMessage.error('历史记录数据格式错误')
        historyRecords.value = []
      }
    } catch (error) {
      console.error('获取历史记录失败:', error)
      // 检查是否是401错误
      if (error.response && error.response.status === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error('获取历史记录失败')
      historyRecords.value = []
    }
    
    try {
      // 获取分析内容
      const analysisRes = await request.get(getFullUrl(API_PATHS.GET_ANALYSIS_FILE), { 
        params: { file_id: fileId } 
      })
      
      // 检查token是否失效
      if (analysisRes.data && analysisRes.data.code === 401) {
        handleTokenExpired()
        return
      }
      
      if (analysisRes.data && analysisRes.data.data) {
        console.log('分析内容原始数据:', analysisRes.data.data)
        // 去除转义字符
        documentData.value.analysis = analysisRes.data.data.replace(/\\n/g, '\n').replace(/\\"/g, '"').replace(/\\'/g, "'").replace(/\\\\/g, '\\')
      } else {
        console.error('分析内容数据格式错误:', analysisRes.data)
        ElMessage.error('分析内容数据格式错误')
        documentData.value.analysis = ''
      }
    } catch (error) {
      console.error('获取分析内容失败:', error)
      // 检查是否是401错误
      if (error.response && error.response.status === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error('获取分析内容失败')
      documentData.value.analysis = ''
    }
    
    try {
      // 获取当前问题
      const questionRes = await request.get(getFullUrl(API_PATHS.GET_CURRENT_QUESTION), { 
        params: { file_id: fileId } 
      })
      
      // 检查token是否失效
      if (questionRes.data && questionRes.data.code === 401) {
        handleTokenExpired()
        return
      }
      
      if (questionRes.data && questionRes.data.data) {
        try {
          // 解析JSON字符串
          const questionData = JSON.parse(questionRes.data.data)
          console.log('题目数据:', questionData)
          
          // 检查数据格式
          if (!questionData.questions || !Array.isArray(questionData.questions) || questionData.questions.length === 0) {
            throw new Error('题目数据格式错误')
          }
          
          // 更新当前题目
          currentQuiz.value = {
            id: Date.now(),
            question: questionData.questions[0].question || '',
            correct: questionData.questions[0].correct || '',
            explanation: ''
          }
          
          // 更新题目列表
          quizList.value = questionData.questions.map(q => ({
            id: Date.now() + Math.random(),
            question: q.question || '',
            correct: q.correct || '',
            explanation: ''
          }))
          
          // 初始化答案数组
          userAnswers.value = new Array(quizList.value.length).fill(null).map(() => ({
            answer: '',
            hasViewedAnswer: false
          }))
          
          currentQuizIndex.value = 0
          userAnswer.value = ''
          showAnswer.value = false
          hasSubmitted.value = false
          isCorrect.value = false
        } catch (error) {
          console.error('解析题目数据失败:', error)
          ElMessage.error('解析题目数据失败')
          currentQuiz.value = null
          quizList.value = []
        }
      } else {
        console.error('题目数据格式错误:', questionRes.data)
        ElMessage.error('题目数据格式错误')
        currentQuiz.value = null
        quizList.value = []
        userAnswers.value = []
      }
    } catch (error) {
      console.error('获取题目失败:', error)
      // 检查是否是401错误
      if (error.response && error.response.status === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error('获取题目失败')
      currentQuiz.value = null
      quizList.value = []
      userAnswers.value = []
    }
    
  } finally {
    // 关闭加载状态
    loading.value = false
    historyLoading.value = false
    quizLoading.value = false
  }
}

// 添加最小化面板状态
const minimizedPanels = ref({
  left: false,
  middle: false,
  right: false,
  quiz: false
})

// 切换面板最小化状态
const toggleMinimize = (panel) => {
  minimizedPanels.value[panel] = !minimizedPanels.value[panel]
  updatePanelWidths()
}

// 获取面板标题
const getPanelTitle = (panel) => {
  const titles = {
    left: '原文',
    middle: '历史记录',
    right: '分析内容',
    quiz: '答题'
  }
  return titles[panel] || ''
}

// 添加得分相关的工具函数
const getScoreType = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

const getScoreText = (score) => {
  if (score >= 80) return '优秀'
  if (score >= 60) return '及格'
  return '不及格'
}

// 修改提交所有答案函数，添加token失效处理
const submitAllAnswers = async () => {
  if (!props.documentId) {
    ElMessage.error('文档ID不存在')
    return
  }
  
  try {

    
    // 保存当前答案
    saveCurrentAnswer()
    
    // 构建提交数据
    const submitData = quizList.value.map((quiz, index) => {
      const userAnswerData = userAnswers.value[index] || { answer: '', hasViewedAnswer: false }
      return   userAnswerData.answer || '未作答'
      
    })
    
    // 将数据转换为JSON字符串
    // const answerJson = JSON.stringify(submitData)
    const answerJson = submitData
    console.log('提交的答案数据:', answerJson)
    
    // 创建FormData对象
    const formData = new FormData()
    formData.append('answer', answerJson)
    formData.append('file_id', props.documentId.toString())
    
    // 发送请求
    const response = await request.post(getFullUrl(API_PATHS.SUBMIT_ANSWERS), formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    // 检查token是否失效
    if (response.data && response.data.code === 401) {
      handleTokenExpired()
      return
    }
    
    if (response.data && response.data.code === 200) {
      // 提交成功后隐藏答题栏
      isSubmitting.value = true
      isAnswerVisible.value = false // 提交后隐藏答题区域
      
      ElMessage.success('提交成功')
      
      isAnswerVisible.value = true; // 接收到消息后重新显示答题区域
    } else {
      ElMessage.error(response.data?.message || '提交失败')
    }
  } catch (error) {
    console.error('提交做题记录失败:', error)
    // 检查是否是401错误
    if (error.response && error.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error('提交做题记录失败')
  } finally {
    isSubmitting.value = false
  }
}

// 添加EpochSeconds格式化函数
const formatEpochSeconds = (epochSeconds) => {
  if (!epochSeconds) return '未知时间'
  
  // 将EpochSeconds转换为毫秒
  const milliseconds = epochSeconds * 1000
  const date = new Date(milliseconds)
  
  // 格式化日期时间为YYYY-MM-DD HH:mm:ss格式
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 添加生成题目相关的状态
const generatingQuestions = ref(false)
// 添加复习队列相关的状态
const reviewQueue = ref([])
const showSelectionMenu = ref(false)
const selectionMenuPosition = ref({ top: 0, left: 0 })
const selectedText = ref('')

// 处理文本选择事件
const handleTextSelection = (event) => {
  const selection = window.getSelection()
  const selectedStr = selection.toString().trim()
  
  if (!selectedStr) {
    showSelectionMenu.value = false
    return
  }
  
  selectedText.value = selectedStr
  
  // 获取选中文本的位置
  const range = selection.getRangeAt(0)
  if (range) {
    const rect = range.getBoundingClientRect()
    // 计算菜单位置，确保不超出视窗边界
    const menuWidth = 160 // 增加菜单宽度以适应内容
    const menuHeight = 48 // 增加菜单高度以提升可点击区域
    
    let left = event.clientX
    let top = event.clientY + 10 // 在鼠标位置下方10px
    
    // 确保菜单不会超出右边界
    if (left + menuWidth > window.innerWidth) {
      left = window.innerWidth - menuWidth - 10 // 添加10px的边距
    }
    
    // 确保菜单不会超出底部边界
    if (top + menuHeight > window.innerHeight) {
      top = event.clientY - menuHeight - 10
    }
    
    selectionMenuPosition.value = {
      top: top + window.scrollY,
      left: left + window.scrollX
    }
    showSelectionMenu.value = true
    
    // 添加全局点击事件监听器
    document.addEventListener('click', closeSelectionMenu)
  }
}

// 关闭选择菜单
const closeSelectionMenu = (event) => {
  // 检查点击是否在菜单内
  const menu = document.querySelector('.selection-menu')
  if (menu && !menu.contains(event.target)) {
    showSelectionMenu.value = false
    // 移除事件监听器
    document.removeEventListener('click', closeSelectionMenu)
  }
}

// 添加到复习队列
const addToReviewQueue = () => {
  if (selectedText.value) {
    // 检查是否已经在队列中
    const isDuplicate = reviewQueue.value.some(item => item.text === selectedText.value)
    if (!isDuplicate) {
      reviewQueue.value.push({
        id: Date.now(),
        text: selectedText.value
      })
      ElMessage.success('已添加到复习队列')
    } else {
      ElMessage.warning('该内容已在复习队列中')
    }
  }
  showSelectionMenu.value = false
}

// 从复习队列中移除
const removeFromQueue = (index) => {
  reviewQueue.value.splice(index, 1)
  ElMessage.success('已从复习队列中移除')
}

// 生成题目
const generateQuestions = async () => {
  if (!props.documentId) {
    ElMessage.error('文档ID不存在')
    return
  }
  
  try {
    generatingQuestions.value = true
    
    // 构建请求数据
    let data = JSON.stringify({
      "mustHave": reviewQueue.value.map(item => item.text).join('\n'),
      "otherPrompt":generationForm.value.customPrompt,
    });
    
    const url = `${getFullUrl(API_PATHS.GenerateQuestions)}?file_id=${props.documentId}&question_num=${generationForm.value.questionCount}`
    var config = {
   method: 'post',
   url: url,
   headers: { 
      'Content-Type': 'application/json'
   },
   data : data
};
    const response = await request(config)
    
    if (response.data && response.data.code === 200) {
      ElMessage.success('题目请求发送成功')
      // 刷新题目列表
      
    } else {
      ElMessage.error(response.data?.message || '生成题目失败')
    }
    
  } catch (error) {
    console.error('生成题目失败:', error)
    ElMessage.error('生成题目失败')
  } finally {
    generatingQuestions.value = false
  }
}


// 在 script setup 部分添加
const generationForm = ref({
  customPrompt: '',
  questionCount: 5
})


// 修改历史记录中的题目点击事件 #check right#
const handleHistoryQuestionClick = (raw) => {
  if (raw) {
    // 找到原文栏的滚动容器
    const originalScrollbar = document.querySelector('.original-text .el-scrollbar__wrap')
    if (!originalScrollbar) return
    
    // 找到对应的高亮元素
    const highlightElements = document.querySelectorAll(`.highlight[data-raw="${raw}"]`)
    if (highlightElements.length > 0) {
      // 计算第一个高亮元素的位置
      const targetElement = highlightElements[0]
      const elementTop = targetElement.offsetTop
      const scrollbarHeight = originalScrollbar.clientHeight
      
      // 滚动到对应的元素，使其居中显示
      originalScrollbar.scrollTo({
        top: elementTop - (scrollbarHeight / 2) + (targetElement.clientHeight / 2),
        behavior: 'smooth'
      })
      
      // 添加临时高亮效果
      targetElement.classList.add('highlight-active')
      setTimeout(() => {
        targetElement.classList.remove('highlight-active')
      }, 2000)
    } else {
      ElMessage.warning('未找到对应的原文片段')
    }
  } else {
    ElMessage.warning('该题目没有关联的原文片段')
  }
}

// 添加展开/收起历史记录题目的状态
const expandedQuestions = ref(new Set())

// 切换题目展开状态
const toggleQuestionExpand = (questionId) => {
  if (expandedQuestions.value.has(questionId)) {
    expandedQuestions.value.delete(questionId)
  } else {
    expandedQuestions.value.add(questionId)
  }
}

// 添加分析内容到复习队列
const addAnalysisToReviewQueue = (rawValue) => {
  if (rawValue) {
    const isDuplicate = reviewQueue.value.some(item => item.text === rawValue)
    if (!isDuplicate) {
      reviewQueue.value.push({
        id: Date.now(),
        text: rawValue
      })
      ElMessage.success('已添加到复习队列')
    } else {
      ElMessage.warning('该内容已在复习队列中')
    }
  }
}

const isAnswerVisible = ref(true); // 控制答题区域的显示

</script>

<style scoped>
@import '../../styles/MainContent.css';
.button-group {
  display: flex;
  justify-content: flex-end; /* 使按钮右对齐 */
  margin-top: 10px; /* 添加顶部间距 */
}
</style>