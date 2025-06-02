<template>
    <div class="paper-edit-view">
      <div class="header">
        <el-button @click="goBack" :disabled="isGenerating">
          <el-icon><Back /></el-icon>
          返回配置
        </el-button>
      </div>
  
      <el-container class="edit-container">
        <!-- 左侧：题目修改区域 -->
        <el-aside width="50%">
          <div class="edit-panel">
            <h3>题目修改</h3>
            
            <div v-if="isGenerating" class="generation-loading">
              <el-skeleton :rows="10" animated />
            </div>
            
            <template v-else>
              <!-- 单选题编辑部分 -->
              <div v-if="singleChoiceQuestions.length > 0" class="question-edit-section">
                <div class="question-type-header">
                  <h4>单选题 (每题{{paperConfig.questionPoints.singleChoice}}分)</h4>
                  <el-button type="primary" size="small" @click="batchEditQuestions('singleChoice')">
                    批量编辑
                  </el-button>
                </div>
                
                <el-collapse>
                  <el-collapse-item v-for="(question, index) in singleChoiceQuestions" 
                    :key="question.id" 
                    :title="`${index + 1}. ${question.text.substring(0, 30)}...`"
                    :name="question.id">
                    <div class="question-edit-form">
                      <el-form label-position="top">
                        <el-form-item label="题目">
                          <el-input 
                            type="textarea" 
                            v-model="question.text"
                            :rows="2"
                          ></el-input>
                        </el-form-item>
                        
                        <el-form-item label="选项">
                          <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-edit">
                            <span class="option-label">{{String.fromCharCode(65 + optIndex)}}.</span>
                            <el-input v-model="question.options[optIndex]"></el-input>
                          </div>
                        </el-form-item>
                        
                        <el-form-item label="正确答案">
                          <el-select v-model="question.correctAnswer">
                            <el-option 
                              v-for="(_, optIndex) in question.options" 
                              :key="optIndex"
                              :label="String.fromCharCode(65 + optIndex)"
                              :value="optIndex"
                            ></el-option>
                          </el-select>
                        </el-form-item>
                      </el-form>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
              
              <!-- 多选题编辑部分 -->
              <div v-if="multipleChoiceQuestions.length > 0" class="question-edit-section">
                <div class="question-type-header">
                  <h4>多选题 (每题{{paperConfig.questionPoints.multipleChoice}}分)</h4>
                  <el-button type="primary" size="small" @click="batchEditQuestions('multipleChoice')">
                    批量编辑
                  </el-button>
                </div>
                
                <el-collapse>
                  <el-collapse-item v-for="(question, index) in multipleChoiceQuestions" 
                    :key="question.id" 
                    :title="`${index + 1}. ${question.text.substring(0, 30)}...`"
                    :name="question.id">
                    <div class="question-edit-form">
                      <el-form label-position="top">
                        <el-form-item label="题目">
                          <el-input 
                            type="textarea" 
                            v-model="question.text"
                            :rows="2"
                          ></el-input>
                        </el-form-item>
                        
                        <el-form-item label="选项">
                          <div v-for="(option, optIndex) in question.options" :key="optIndex" class="option-edit">
                            <span class="option-label">{{String.fromCharCode(65 + optIndex)}}.</span>
                            <el-input v-model="question.options[optIndex]"></el-input>
                          </div>
                        </el-form-item>
                        
                        <el-form-item label="正确答案">
                          <el-select v-model="question.correctAnswer" multiple>
                            <el-option 
                              v-for="(_, optIndex) in question.options" 
                              :key="optIndex"
                              :label="String.fromCharCode(65 + optIndex)"
                              :value="optIndex"
                            ></el-option>
                          </el-select>
                        </el-form-item>
                      </el-form>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
              
              <!-- 判断题编辑部分 -->
              <div v-if="trueFalseQuestions.length > 0" class="question-edit-section">
                <div class="question-type-header">
                  <h4>判断题 (每题{{paperConfig.questionPoints.trueFalse}}分)</h4>
                  <el-button type="primary" size="small" @click="batchEditQuestions('trueFalse')">
                    批量编辑
                  </el-button>
                </div>
                
                <el-collapse>
                  <el-collapse-item v-for="(question, index) in trueFalseQuestions" 
                    :key="question.id" 
                    :title="`${index + 1}. ${question.text.substring(0, 30)}...`"
                    :name="question.id">
                    <div class="question-edit-form">
                      <el-form label-position="top">
                        <el-form-item label="题目">
                          <el-input 
                            type="textarea" 
                            v-model="question.text"
                            :rows="2"
                          ></el-input>
                        </el-form-item>
                        
                        <el-form-item label="正确答案">
                          <el-radio-group v-model="question.correctAnswer">
                            <el-radio :label="true">正确</el-radio>
                            <el-radio :label="false">错误</el-radio>
                          </el-radio-group>
                        </el-form-item>
                      </el-form>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </template>
          </div>
        </el-aside>
        
        <!-- 右侧：试卷预览区域 -->
        <el-main>
          <div class="preview-panel">
            <h3>试卷预览</h3>
            
            <div v-if="isGenerating" class="generation-loading">
              <el-skeleton animated>
                <template #template>
                  <div style="padding: 20px;">
                    <h4><el-skeleton-item variant="h3" style="width: 50%" /></h4>
                    <div style="margin: 20px 0">
                      <el-skeleton-item variant="text" style="width: 100%" />
                      <el-skeleton-item variant="text" style="width: 90%" />
                    </div>
                    <el-skeleton :rows="10" />
                    <div style="margin-top: 20px; text-align: center;">
                      <el-skeleton-item variant="button" style="width: 200px" />
                    </div>
                  </div>
                </template>
              </el-skeleton>
              
              <div class="generation-status">
                <el-progress 
                  type="circle" 
                  :percentage="generateProgressPercentage"
                  :status="generateProgressPercentage >= 100 ? 'success' : ''"
                ></el-progress>
                <div class="generation-time-info">
                  <p>正在生成试卷，请稍候...</p>
                  <p v-if="estimatedTime > 0">预计等待时间：{{ formatTime(estimatedTime) }}</p>
                </div>
              </div>
            </div>
            
            <div v-else class="paper-preview">
              <div class="paper-header">
                <h2>{{ paperConfig.name }}</h2>
                <div class="paper-meta">
                  <div class="paper-meta-item">
                    <span>难度：</span>
                    <el-rate v-model="paperConfig.difficulty" disabled></el-rate>
                  </div>
                  <div class="paper-meta-item">
                    <span>总分：{{ totalPoints }}分</span>
                  </div>
                </div>
                <div class="paper-details" v-if="paperConfig.content || paperConfig.scope">
                  <div v-if="paperConfig.content" class="paper-detail-item">
                    <span class="paper-detail-label">考试内容：</span>
                    <span>{{ paperConfig.content }}</span>
                  </div>
                  <div v-if="paperConfig.scope" class="paper-detail-item">
                    <span class="paper-detail-label">考试范围：</span>
                    <span>{{ paperConfig.scope }}</span>
                  </div>
                </div>
              </div>
              
              <div class="question-types">
                <div class="question-type-section" v-if="singleChoiceQuestions.length > 0">
                  <h3>一、单选题 (每题{{ paperConfig.questionPoints.singleChoice }}分，共{{ singleChoiceQuestions.length }}题)</h3>
                  <div v-for="(question, index) in singleChoiceQuestions" :key="question.id" class="question-item">
                    <div class="question-text">{{ index + 1 }}. {{ question.text }}</div>
                    <div class="options">
                      <div v-for="(option, idx) in question.options" :key="idx" class="option">
                        <span class="option-label">{{ String.fromCharCode(65 + idx) }}.</span>
                        {{ option }}
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="question-type-section" v-if="multipleChoiceQuestions.length > 0">
                  <h3>二、多选题 (每题{{ paperConfig.questionPoints.multipleChoice }}分，共{{ multipleChoiceQuestions.length }}题)</h3>
                  <div v-for="(question, index) in multipleChoiceQuestions" :key="question.id" class="question-item">
                    <div class="question-text">{{ index + 1 }}. {{ question.text }}</div>
                    <div class="options">
                      <div v-for="(option, idx) in question.options" :key="idx" class="option">
                        <span class="option-label">{{ String.fromCharCode(65 + idx) }}.</span>
                        {{ option }}
                      </div>
                    </div>
                  </div>
                </div>
                
                <div class="question-type-section" v-if="trueFalseQuestions.length > 0">
                  <h3>三、判断题 (每题{{ paperConfig.questionPoints.trueFalse }}分，共{{ trueFalseQuestions.length }}题)</h3>
                  <div v-for="(question, index) in trueFalseQuestions" :key="question.id" class="question-item">
                    <div class="question-text">{{ index + 1 }}. {{ question.text }}</div>
                    <div class="options">
                      <div class="option">
                        <span class="option-label">A.</span>
                        正确
                      </div>
                      <div class="option">
                        <span class="option-label">B.</span>
                        错误
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="paper-actions preview-actions" v-if="!isGenerating">
              <el-button type="primary" @click="savePaper">保存试卷</el-button>
              <el-button type="success" @click="exportPaper">导出试卷</el-button>
            </div>
          </div>
        </el-main>
      </el-container>
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router'
  import { Back } from '@element-plus/icons-vue'
  import { ref, computed, onMounted, onUnmounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import axios from 'axios'
  
  const router = useRouter()
  
  // 状态变量
  const isGenerating = ref(true)
  const questions = ref([])
  const estimatedTime = ref(0) // 预计等待时间（秒）
  const generateStartTime = ref(0) // 生成开始时间
  const generateProgressPercentage = ref(0) // 生成进度百分比
  
  // 定时更新进度的计时器
  let progressTimer = null
  
  
  // 按题型分类的问题
  const singleChoiceQuestions = computed(() => {
    return questions.value.filter(q => q.type === 'singleChoice')
  })
  
  const multipleChoiceQuestions = computed(() => {
    return questions.value.filter(q => q.type === 'multipleChoice')
  })
  
  const trueFalseQuestions = computed(() => {
    return questions.value.filter(q => q.type === 'trueFalse')
  })
  
  // 返回主页面
  const goBack = () => {
    if (isGenerating.value) return
    
    ElMessageBox.confirm(
      '返回主页面将丢失当前编辑的内容，是否继续？',
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(() => {
      router.push('/home')
    }).catch(() => {})
  }
  
  // 启动进度更新计时器
  const startProgressUpdate = () => {
    stopProgressUpdate() // 确保没有多个计时器
    
    progressTimer = setInterval(() => {
      const elapsed = (Date.now() - generateStartTime.value) / 1000 // 经过的秒数
      
      if (estimatedTime.value > 0) {
        // 如果有预计时间，按比例更新进度
        generateProgressPercentage.value = Math.min(95, (elapsed / estimatedTime.value) * 100)
      } else {
        // 没有预计时间，使用默认策略更新进度
        // 前10秒快速到30%，然后慢慢增加到95%
        if (elapsed <= 10) {
          generateProgressPercentage.value = Math.min(30, elapsed * 3)
        } else {
          const remaining = Math.max(0, 95 - generateProgressPercentage.value)
          generateProgressPercentage.value += remaining * 0.05
        }
      }
    }, 500)
  }
  
  // 停止进度更新计时器
  const stopProgressUpdate = () => {
    if (progressTimer) {
      clearInterval(progressTimer)
      progressTimer = null
    }
  }
  
  // 格式化时间函数
  const formatTime = (seconds) => {
    if (seconds < 60) {
      return `${Math.ceil(seconds)}秒`
    } else {
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = Math.ceil(seconds % 60)
      return `${minutes}分${remainingSeconds}秒`
    }
  }
  
  // 批量编辑题目
  const batchEditQuestions = (questionType) => {
    ElMessage.info(`批量编辑${getQuestionTypeText(questionType)}功能开发中`)
  }
  
  // 获取题型中文名称
  const getQuestionTypeText = (type) => {
    const typeMap = {
      singleChoice: '单选题',
      multipleChoice: '多选题',
      trueFalse: '判断题'
    }
    return typeMap[type] || '未知题型'
  }
  
  // 保存试卷
  const savePaper = () => {
    ElMessage.success('试卷保存成功')
    // 实际保存逻辑...

  }
  
  // 导出试卷
  const exportPaper = () => {
    ElMessage.info('试卷导出功能开发中')
    // 实际导出逻辑...

  }

  // 页面加载时从localStorage获取配置，并发起API请求
  onMounted(() => {
    try {
      // 从localStorage获取配置信息
      const savedConfig = localStorage.getItem('paperConfig')
      if (savedConfig) {
        paperConfig.value = JSON.parse(savedConfig)
        // 立即开始生成试卷
        generatePaper()
      } else {
        // 如果没有找
      }
    } catch (error) {
      console.error('获取配置失败:', error)
      ElMessage.error('获取配置失败，请重试')
      router.push('/paper/config')
    }
  }) ;
  
</script>