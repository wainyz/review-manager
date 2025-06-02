<template>
  <div class="paper-view">
    <div class="header">
      <el-button @click="goToHome">
        <el-icon><Back /></el-icon>
        返回
      </el-button>
    </div>

    <div v-if="paperMode === 'config'">
      <h2>试卷生成</h2>
      <el-form :model="paperConfig" label-width="120px">
        <el-form-item label="试卷名称">
          <el-input v-model="paperConfig.name" placeholder="请输入试卷名称"></el-input>
        </el-form-item>
        
        <el-form-item label="难度等级">
          <el-rate 
            v-model="paperConfig.difficulty" 
            :texts="difficultyTexts" 
            show-text>
          </el-rate>
        </el-form-item>
        
        <el-form-item label="考试内容">
          <el-input
            type="textarea"
            v-model="paperConfig.content"
            :rows="2"
            placeholder="请输入考试内容描述，例如：Web前端开发基础、JavaScript编程等"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="考试范围">
          <el-input
            type="textarea"
            v-model="paperConfig.scope"
            :rows="2"
            placeholder="请输入考试范围描述，例如：教材第1-5章、实验课程1-3等"
          ></el-input>
        </el-form-item>
        
        <!-- 题目类型与数量配置 -->
        <el-form-item 
          label="题目类型与数量" 
          :class="{ 'is-error': !isTotalPointsValid }"
        >
          <div class="total-points-indicator">
            当前总分: <span :class="{ 'valid-points': isTotalPointsValid, 'invalid-points': !isTotalPointsValid }">{{ totalPoints }}</span>/100分
            <el-tooltip 
              content="试卷总分必须为100分，请调整各题型的数量或分值" 
              placement="top"
              v-if="!isTotalPointsValid"
            >
              <el-icon class="warning-icon"><Warning /></el-icon>
            </el-tooltip>
          </div>
          
          <div class="question-types-config">
            <div class="question-type-item">
              <span>选择题：</span>
              <el-input-number 
                v-model="paperConfig.questionCounts.choice" 
                :min="0" 
                :max="100" 
                size="small"
                @change="onQuestionConfigChange"
              ></el-input-number>
              <div class="point-config">
                <span>分值：</span>
                <el-input-number 
                  v-model="paperConfig.questionPoints.choice" 
                  :min="1" 
                  :max="20" 
                  size="small"
                  @change="onQuestionConfigChange"
                ></el-input-number>
              </div>
              <div class="subtotal-points">
                小计: {{ paperConfig.questionCounts.choice * paperConfig.questionPoints.choice }}分
              </div>
            </div>
            
            <div class="question-type-item">
              <span>填空题：</span>
              <el-input-number 
                v-model="paperConfig.questionCounts.fillBlank" 
                :min="0" 
                :max="50" 
                size="small"
                @change="onQuestionConfigChange"
              ></el-input-number>
              <div class="point-config">
                <span>分值：</span>
                <el-input-number 
                  v-model="paperConfig.questionPoints.fillBlank" 
                  :min="1" 
                  :max="20" 
                  size="small"
                  @change="onQuestionConfigChange"
                ></el-input-number>
              </div>
              <div class="subtotal-points">
                小计: {{ paperConfig.questionCounts.fillBlank * paperConfig.questionPoints.fillBlank }}分
              </div>
            </div>
            
            <div class="question-type-item">
              <span>解答题：</span>
              <el-input-number 
                v-model="paperConfig.questionCounts.solution" 
                :min="0" 
                :max="50" 
                size="small"
                @change="onQuestionConfigChange"
              ></el-input-number>
              <div class="point-config">
                <span>分值：</span>
                <el-input-number 
                  v-model="paperConfig.questionPoints.solution" 
                  :min="1" 
                  :max="20" 
                  size="small"
                  @change="onQuestionConfigChange"
                ></el-input-number>
              </div>
              <div class="subtotal-points">
                小计: {{ paperConfig.questionCounts.solution * paperConfig.questionPoints.solution }}分
              </div>
            </div>
          </div>
          
          <div class="point-adjustment-tools" v-if="!isTotalPointsValid">
            <el-button type="primary" size="small" @click="autoAdjustPoints">
              自动调整
            </el-button>
            <span class="adjustment-tip">点击自动调整可快速将总分调整为100分</span>
          </div>
        </el-form-item>
        
        <!-- 额外提示词配置 -->
        <el-form-item label="额外提示词">
          <el-input
            type="textarea"
            v-model="paperConfig.promptWords"
            :rows="4"
            placeholder="请输入生成题目的额外提示词，例如：主题范围、难度要求、特定知识点等"
          ></el-input>
        </el-form-item>
        
        <!-- 上传出题参考文件 -->
        <el-form-item label="出题参考文件">
          <el-upload
            class="upload-reference"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            :file-list="fileList"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传TXT文件作为出题参考材料，系统将根据该材料内容出题
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <div class="paper-actions">
        <el-button 
          type="primary" 
          @click="generatePaper" 
          :disabled="!isConfigValid"
        >生成试卷</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Back, Warning } from '@element-plus/icons-vue'
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { API_PATHS,getFullUrl } from '../config/api'
import request from '../config/axios'
const router = useRouter()

// 试卷模式: config(配置), preview(预览), saved(已保存)
const paperMode = ref('config')
const fileList = ref([])

// 难度级别文字
const difficultyTexts = ['简单', '较简单', '中等', '较难', '困难']

// 试卷配置
const paperConfig = ref({
  name: '模拟试卷',
  difficulty: 3, // 默认中等难度
  content: '', // 考试内容
  scope: '', // 考试范围
  questionCounts: {
    choice: 10,
    fillBlank: 5,
    solution: 5
  },
  questionPoints: {
    choice: 1,
    fillBlank: 2,
    solution: 1
  },
  promptWords: '', // 额外提示词
  referenceFile: null  // 出题参考文件内容
})

// 计算属性
const isConfigValid = computed(() => {
  return paperConfig.value.name.trim() !== '' && 
         (paperConfig.value.questionCounts.choice > 0 || 
          paperConfig.value.questionCounts.fillBlank > 0 || 
          paperConfig.value.questionCounts.solution > 0) &&
         isTotalPointsValid.value
})

const totalPoints = computed(() => {
  return (
    paperConfig.value.questionCounts.choice * paperConfig.value.questionPoints.choice +
    paperConfig.value.questionCounts.fillBlank * paperConfig.value.questionPoints.fillBlank +
    paperConfig.value.questionCounts.solution * paperConfig.value.questionPoints.solution
  )
})

const isTotalPointsValid = computed(() => {
  return totalPoints.value === 100
})

// 方法
const handleFileChange = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    paperConfig.value.referenceFile = e.target.result
  }
  reader.readAsText(file.raw)
}

const goToHome = () => {
  router.push('/home').then(() => {
    window.location.reload()
  })
}

const generatePaper = async () => {
  // 创建 FormData 对象
  const formData = new FormData()

  // 将试卷配置作为 JSON 字符串添加到 FormData 中
  formData.append('paperConfig', JSON.stringify(paperConfig.value))

  try {
    // 发送请求到后端
    // 打印 FormData 内容
  console.log('FormData 内容:')
  for (const pair of formData.entries()) {
    console.log(pair[0] + ': ' + pair[1])
  }
    const response =  request.post(
      getFullUrl(API_PATHS.generatePaper),
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )
    console.log((await response).data)
    // // 后端返回生成的试卷 ID

    // 页面滚动到顶部
    window.scrollTo(0, 0)
    ElMessage.success((await response).data.message)
      router.push('/home')

  } catch (error) {
    // 请求失败时的错误处理
    ElMessage.error('试卷生成失败，请重试')
    console.error(error)
  }
}


// 题目配置变更处理
const onQuestionConfigChange = () => {
  // 防止负数或非数字输入
  const config = paperConfig.value
  
  // 确保数量为非负整数
  config.questionCounts.choice = Math.max(0, Math.floor(config.questionCounts.choice))
  config.questionCounts.fillBlank = Math.max(0, Math.floor(config.questionCounts.fillBlank))
  config.questionCounts.solution = Math.max(0, Math.floor(config.questionCounts.solution))
  
  // 确保分值为正整数
  config.questionPoints.choice = Math.max(1, Math.floor(config.questionPoints.choice))
  config.questionPoints.fillBlank = Math.max(1, Math.floor(config.questionPoints.fillBlank))
  config.questionPoints.solution = Math.max(1, Math.floor(config.questionPoints.solution))
}

// 优化后的自动调整函数：智能平衡分值和题目数量
const autoAdjustPoints = () => {
  const config = paperConfig.value
  
  // 保存原始配置，用于计算变化量
  const originalCounts = { ...config.questionCounts }
  const originalPoints = { ...config.questionPoints }
  
  // 计算当前总题目数
  const totalQuestions = 
    config.questionCounts.choice + 
    config.questionCounts.fillBlank + 
    config.questionCounts.solution
  
  // 如果没有设置任何题目，设置合理的默认值
  if (totalQuestions === 0) {
    config.questionCounts.choice = 40
    config.questionPoints.choice = 1
    config.questionCounts.fillBlank = 10
    config.questionPoints.fillBlank = 2
    config.questionCounts.solution = 10
    config.questionPoints.solution = 4
    ElMessage.success('已设置默认题目配置，总分为100分')
    return
  }
  
  // 获取各题型数据
  const questionTypes = [
    { 
      type: 'choice', 
      count: config.questionCounts.choice,
      originalCount: originalCounts.choice,
      points: config.questionPoints.choice,
      originalPoints: originalPoints.choice
    },
    { 
      type: 'fillBlank', 
      count: config.questionCounts.fillBlank,
      originalCount: originalCounts.fillBlank,
      points: config.questionPoints.fillBlank,
      originalPoints: originalPoints.fillBlank
    },
    { 
      type: 'solution', 
      count: config.questionCounts.solution,
      originalCount: originalCounts.solution,
      points: config.questionPoints.solution,
      originalPoints: originalPoints.solution
    }
  ].filter(t => t.count > 0) // 只保留有题目的题型
  
  if (questionTypes.length === 0) return
  
  // 当前总分
  const currentTotal = 
    config.questionCounts.choice * config.questionPoints.choice +
    config.questionCounts.fillBlank * config.questionPoints.fillBlank +
    config.questionCounts.solution * config.questionPoints.solution
  
  // 阶段1: 尝试只调整分值 (保持现有分值比例，整体缩放)
  const tryScalePoints = () => {
    // 如果当前总分为0，无法按比例缩放
    if (currentTotal === 0) return false
    
    // 按当前配置计算缩放比例
    const scaleFactor = 100 / currentTotal
    
    // 计算缩放后的分值
    const scaledPoints = {}
    let totalAfterScaling = 0
    
    for (const type of questionTypes) {
      // 计算缩放后的分值，四舍五入到最接近的整数
      scaledPoints[type.type] = Math.max(1, Math.round(type.points * scaleFactor))
      totalAfterScaling += scaledPoints[type.type] * type.count
    }
    
    // 检查缩放后的总分是否接近100
    const isAcceptable = Math.abs(totalAfterScaling - 100) <= questionTypes.length
    
    if (isAcceptable) {
      // 应用缩放后的分值
      for (const type of questionTypes) {
        config.questionPoints[type.type] = scaledPoints[type.type]
      }
      
      // 微调，确保总分正好为100
      adjustToExact100()
      return true
    }
    
    return false
  }
  
  // 阶段2: 智能设置分值
  const tryIntelligentPoints = () => {
    // 检查是否有可能通过分值调整达到100分
    const minPossibleTotal = totalQuestions // 假设每题最低1分
    const maxPossibleTotal = totalQuestions * 20 // 假设每题最高20分
    
    if (minPossibleTotal <= 100 && 100 <= maxPossibleTotal) {
      // 可以通过分值调整达到100分
      
      // 保存原始分值比例
      const originalRatios = {}
      const totalPointsValue = questionTypes.reduce((sum, t) => sum + t.points, 0)
      
      for (const type of questionTypes) {
        originalRatios[type.type] = totalPointsValue > 0 ? type.points / totalPointsValue : 1
      }
      
      // 按照题目数量从多到少排序
      questionTypes.sort((a, b) => b.count - a.count)
      
      // 初始设置所有题型为1分
      for (const type of questionTypes) {
        config.questionPoints[type.type] = 1
      }
      
      // 剩余需要分配的分数
      let remainingPoints = 100 - totalQuestions
      
      if (remainingPoints > 0) {
        // 根据题目数量和原始分值比例来分配额外分值
        
        // 第一轮：按原始分值比例分配大部分额外分值
        const pointsToDistributeFirst = Math.floor(remainingPoints * 0.8) // 先分配80%
        
        // 计算各题型应得的额外总分
        const extraPointsPerType = {}
        for (const type of questionTypes) {
          extraPointsPerType[type.type] = Math.floor(pointsToDistributeFirst * originalRatios[type.type])
        }
        
        // 按比例分配额外分值
        for (const type of questionTypes) {
          const typeExtraPoints = extraPointsPerType[type.type]
          if (type.count > 0 && typeExtraPoints > 0) {
            const pointsPerQuestion = Math.floor(typeExtraPoints / type.count)
            if (pointsPerQuestion > 0) {
              config.questionPoints[type.type] += pointsPerQuestion
              remainingPoints -= pointsPerQuestion * type.count
            }
          }
        }
        
        // 第二轮：优先将剩余分值分配给分值最低的题型
        while (remainingPoints > 0) {
          // 按当前分值排序，分值低的在前
          questionTypes.sort((a, b) => 
            config.questionPoints[a.type] - config.questionPoints[b.type] || 
            b.count - a.count // 分值相同时，数量多的在前
          )
          
          // 最低分值的题型
          const lowestType = questionTypes[0].type
          const lowestCount = config.questionCounts[lowestType]
          
          // 如果这个题型数量太多，可能一次性增加太多分值，则平均分配
          if (lowestCount > remainingPoints) {
            // 平均分配到所有题型
            let pointsDistributed = false
            
            for (const type of questionTypes) {
              if (remainingPoints <= 0) break
              
              config.questionPoints[type.type] += 1
              remainingPoints -= type.count
              pointsDistributed = true
            }
            
            // 如果无法再分配，退出循环
            if (!pointsDistributed) break
          } else {
            // 否则优先提高最低分值题型的分数
            config.questionPoints[lowestType] += 1
            remainingPoints -= lowestCount
          }
        }
      } else if (remainingPoints < 0) {
        // 总分超过100，需要降低分值
        // 这种情况应该很少见，因为我们初始设置为1分
        console.warn('Unexpected case: total > 100 with 1 point per question')
      }
      
      // 微调，确保总分正好为100
      adjustToExact100()
      return true
    }
    
    return false
  }
  
  // 阶段3: 最小化题目数量调整
  const tryMinimalQuestionChange = () => {
    // 当前题目总数过多或过少
    if (totalQuestions > 100) {
      // 题目总数过多
      // 尝试部分通过增加分值减少调整题目的需求
      
      // 找出可以增加分值的题型（优先选择分值低的）
      const adjustableTypes = [...questionTypes].sort((a, b) => 
        a.points - b.points || b.count - a.count
      )
      
      // 计算需要减少的题目总数
      let excessQuestions = totalQuestions - 100
      const originalExcess = excessQuestions
      
      // 先尝试增加分值来减少需要删除的题目数量
      for (const type of adjustableTypes) {
        // 只有当分值不是很高时才考虑增加
        if (config.questionPoints[type.type] < 10 && excessQuestions > 0) {
          const maxIncrease = Math.min(
            20 - config.questionPoints[type.type], // 最多增加到20分
            Math.ceil(excessQuestions / type.count) // 需要的最小增加量
          )
          
          if (maxIncrease > 0) {
            config.questionPoints[type.type] += maxIncrease
            excessQuestions -= Math.min(excessQuestions, maxIncrease * type.count)
          }
        }
      }
      
      // 如果通过增加分值不能完全解决，则需要减少题目
      if (excessQuestions > 0) {
        // 按占比和重要性排序（分值高的题型更重要）
        questionTypes.sort((a, b) => {
          // 分值高的更重要，减少的可能性更低
          const importanceA = a.points
          const importanceB = b.points
          
          // 数量多的贡献更大，可以减少更多
          if (importanceA !== importanceB) return importanceA - importanceB
          return b.count - a.count
        })
        
        let questionsToRemove = excessQuestions
        
        // 第一轮：确保每种题型至少保留1题
        for (const type of questionTypes) {
          if (questionsToRemove <= 0) break
          
          const typeCount = config.questionCounts[type.type]
          const minToKeep = Math.max(1, Math.floor(type.originalCount * 0.5)) // 至少保留原来的50%
          const maxToRemove = Math.max(0, typeCount - minToKeep)
          
          if (maxToRemove > 0) {
            const removeCount = Math.min(questionsToRemove, maxToRemove)
            config.questionCounts[type.type] -= removeCount
            questionsToRemove -= removeCount
          }
        }
        
        // 如果还需要减少，第二轮继续减少
        if (questionsToRemove > 0) {
          for (const type of questionTypes) {
            if (questionsToRemove <= 0) break
            
            const typeCount = config.questionCounts[type.type]
            // 保留至少1题
            const maxToRemove = Math.max(0, typeCount - 1)
            
            if (maxToRemove > 0) {
              const removeCount = Math.min(questionsToRemove, maxToRemove)
              config.questionCounts[type.type] -= removeCount
              questionsToRemove -= removeCount
            }
          }
        }
        
        // 如果经过两轮调整，题目数量发生了变化，可能需要重新调整分值
        const newTotalQuestions = 
          config.questionCounts.choice + 
          config.questionCounts.fillBlank + 
          config.questionCounts.solution
        
        if (newTotalQuestions !== totalQuestions) {
          // 重新调用函数进行分值调整
          return autoAdjustPoints()
        }
      } else {
        // 仅通过增加分值就解决了问题
        adjustToExact100()
        return true
      }
    } else {
      // 题目总数太少
      // 尝试先通过最大化分值解决
      
      // 初始设置分值，尽量达到平均比例
      const idealPointsPerQuestion = Math.floor(100 / totalQuestions)
      let remainingPoints = 100
      
      // 按题目数量排序，数量多的先分配
      questionTypes.sort((a, b) => b.count - a.count)
      
      // 第一轮：分配基础分值
      for (const type of questionTypes) {
        const pointsForType = Math.min(
          20, // 最高20分
          Math.floor(remainingPoints / type.count) // 该题型可分配的分值
        )
        
        if (pointsForType >= 1) {
          config.questionPoints[type.type] = pointsForType
          remainingPoints -= pointsForType * type.count
        } else {
          config.questionPoints[type.type] = 1 // 至少1分
          remainingPoints -= 1 * type.count
        }
      }
      
      // 检查是否达到100分
      const newTotalPoints = 
        config.questionCounts.choice * config.questionPoints.choice +
        config.questionCounts.fillBlank * config.questionPoints.fillBlank +
        config.questionCounts.solution * config.questionPoints.solution
      
      if (newTotalPoints < 100) {
        // 分值调整无法达到100分，需要增加题目
        
        // 计算还需要增加多少题目（假设每题5分）
        const pointsNeeded = 100 - newTotalPoints
        const additionalQuestionsNeeded = Math.ceil(pointsNeeded / 5)
        
        // 尽量保持原有题型的比例
        const typeRatios = {}
        const totalOriginalCount = questionTypes.reduce((sum, t) => sum + t.originalCount, 0)
        
        for (const type of questionTypes) {
          typeRatios[type.type] = totalOriginalCount > 0 ? 
            type.originalCount / totalOriginalCount : 1 / questionTypes.length
        }
        
        // 按原始分布比例增加题目
        let questionsAdded = 0
        
        // 第一轮：按比例分配大部分新题目
        for (const type of questionTypes) {
          const addCount = Math.floor(additionalQuestionsNeeded * typeRatios[type.type])
          if (addCount > 0) {
            config.questionCounts[type.type] += addCount
            questionsAdded += addCount
          }
        }
        
        // 第二轮：分配剩余未分配的题目
        let remainingToAdd = additionalQuestionsNeeded - questionsAdded
        
        // 按原始数量排序，多的优先增加
        questionTypes.sort((a, b) => b.originalCount - a.originalCount)
        
        for (const type of questionTypes) {
          if (remainingToAdd <= 0) break
          
          config.questionCounts[type.type] += 1
          remainingToAdd -= 1
        }
        
        // 再次递归调用自己优化
        return autoAdjustPoints()
      } else if (newTotalPoints > 100) {
        // 总分超过100，需要减少分值
        adjustToExact100()
        return true
      } else {
        // 正好100分
        return true
      }
    }
    
    return false
  }
  
  // 辅助函数：微调确保总分正好为100
  const adjustToExact100 = () => {
    // 计算当前总分
    const currentTotal = 
      config.questionCounts.choice * config.questionPoints.choice +
      config.questionCounts.fillBlank * config.questionPoints.fillBlank +
      config.questionCounts.solution * config.questionPoints.solution
    
    if (currentTotal === 100) return // 已经是100分
    
    const diff = 100 - currentTotal
    
    if (Math.abs(diff) > 20) {
      // 差距太大，可能需要其他调整策略
      console.warn('Large difference from target: ' + diff)
      tryMinimalQuestionChange()
      return
    }
    
    // 选择合适的题型进行微调
    if (diff > 0) {
      // 总分小于100，需要增加分值
      // 找出最容易微调的题型（数量少，调整影响小）
      questionTypes.sort((a, b) => a.count - b.count)
      
      for (const type of questionTypes) {
        if (diff <= 0) break
        
        // 计算需要增加的每题分值
        const pointsToAdd = Math.ceil(diff / type.count)
        if (config.questionPoints[type.type] + pointsToAdd <= 20) {
          config.questionPoints[type.type] += pointsToAdd
          break
        }
      }
    } else {
      // 总分大于100，需要减少分值
      // 找出最容易微调的题型（数量少，调整影响小；分值高，有减少空间）
      questionTypes.sort((a, b) => {
        // 分值高的优先考虑减少
        const pointDiffA = config.questionPoints[a.type] - 1 // 与最小值的差距
        const pointDiffB = config.questionPoints[b.type] - 1
        
        if (pointDiffA !== pointDiffB) return pointDiffB - pointDiffA
        // 数量少的优先减少
        return a.count - b.count
      })
      
      for (const type of questionTypes) {
        if (diff >= 0) break
        
        // 计算需要减少的每题分值
        const pointsToReduce = Math.ceil(Math.abs(diff) / type.count)
        const newPoints = Math.max(1, config.questionPoints[type.type] - pointsToReduce)
        
        config.questionPoints[type.type] = newPoints
        diff += (config.questionPoints[type.type] - newPoints) * type.count
      }
    }
    
    // 再次检查调整后的总分
    const adjustedTotal = 
      config.questionCounts.choice * config.questionPoints.choice +
      config.questionCounts.fillBlank * config.questionPoints.fillBlank +
      config.questionCounts.solution * config.questionPoints.solution
    
    if (adjustedTotal !== 100) {
      // 如果还不是100分，进行最后的强制调整
      // 选择数量最少的题型，直接设置分值使总分为100
      questionTypes.sort((a, b) => a.count - b.count)
      const typeToForceAdjust = questionTypes[0].type
      
      const currentSubtotal = adjustedTotal - 
        (config.questionCounts[typeToForceAdjust] * config.questionPoints[typeToForceAdjust])
      
      const neededPoints = 100 - currentSubtotal
      const pointsPerQuestion = Math.max(1, Math.floor(neededPoints / config.questionCounts[typeToForceAdjust]))
      
      if (pointsPerQuestion <= 20) {
        config.questionPoints[typeToForceAdjust] = pointsPerQuestion
      } else {
        // 如果需要的分值太高，可能需要增加题目
        tryMinimalQuestionChange()
      }
    }
  }
  
  // 尝试各种策略，按优先级顺序
  let adjusted = false
  
  // 首先，尝试按比例缩放现有分值
  adjusted = tryScalePoints()
  
  // 如果按比例缩放不成功，尝试智能设置分值
  if (!adjusted) {
    adjusted = tryIntelligentPoints()
  }
  
  // 如果仅调整分值不能达到100分，尝试最小化题目变动
  if (!adjusted) {
    adjusted = tryMinimalQuestionChange()
  }
  
  // 最终检查，确保所有分值至少为1
  config.questionPoints.choice = Math.max(1, config.questionPoints.choice || 0)
  config.questionPoints.fillBlank = Math.max(1, config.questionPoints.fillBlank || 0)
  config.questionPoints.solution = Math.max(1, config.questionPoints.solution || 0)
  
  // 验证总分是否为100分
  const finalTotal = 
    config.questionCounts.choice * config.questionPoints.choice +
    config.questionCounts.fillBlank * config.questionPoints.fillBlank +
    config.questionCounts.solution * config.questionPoints.solution
  
  // 计算题目数量变化
  const originalTotalQuestions = 
    originalCounts.choice + originalCounts.fillBlank + originalCounts.solution
    
  const finalTotalQuestions = 
    config.questionCounts.choice + config.questionCounts.fillBlank + config.questionCounts.solution
    
  const questionsChanged = Math.abs(finalTotalQuestions - originalTotalQuestions)
  const percentChanged = originalTotalQuestions > 0 ? 
    (questionsChanged / originalTotalQuestions) * 100 : 0
  
  // 根据调整结果显示不同的消息
  if (finalTotal === 100) {
    if (questionsChanged === 0) {
      ElMessage.success('已自动调整题目分值，总分为100分')
    } else if (percentChanged <= 10) {
      ElMessage.success(`已调整题目配置，题目数量变化${questionsChanged}题(${percentChanged.toFixed(1)}%)，总分为100分`)
    } else {
      ElMessage.warning(`已大幅调整题目配置，题目数量变化${questionsChanged}题(${percentChanged.toFixed(1)}%)，总分为100分`)
    }
  } else {
    // 如果经过所有尝试仍无法精确达到100分，进行最后的强制调整
    const mostNumerous = questionTypes.sort((a, b) => b.count - a.count)[0].type
    if (config.questionCounts[mostNumerous] > 0) {
      const currentSubtotal = finalTotal - 
        (config.questionCounts[mostNumerous] * config.questionPoints[mostNumerous])
      
      const neededPoints = 100 - currentSubtotal
      config.questionPoints[mostNumerous] = Math.max(1, Math.round(neededPoints / config.questionCounts[mostNumerous]))
      
      ElMessage.warning('试卷配置复杂，调整为100分')
    } else {
      ElMessage.error('无法调整为100分，请手动修改配置')
    }
  }
}

// 监听题目配置变化
watch(() => [
  paperConfig.value.questionCounts.singleChoice,
  paperConfig.value.questionCounts.multipleChoice,
  paperConfig.value.questionCounts.trueFalse,
  paperConfig.value.questionPoints.singleChoice,
  paperConfig.value.questionPoints.multipleChoice,
  paperConfig.value.questionPoints.trueFalse
], onQuestionConfigChange, { deep: true })

onMounted(() => {
  // 初始化逻辑
})
</script>

<style scoped>
.paper-view {
  padding: 32px;
  min-height: 100vh;
  width: 100%;
  background: var(--el-bg-color);
  box-sizing: border-box;
}

.header {
  margin-bottom: 20px;
}

.total-points-indicator {
  margin-bottom: 15px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 5px;
}

.valid-points {
  color: var(--el-color-success);
}

.invalid-points {
  color: var(--el-color-danger);
}

.warning-icon {
  color: var(--el-color-danger);
  margin-left: 5px;
}

.question-types-config {
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding: 15px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background-color: var(--el-fill-color-light);
}

.question-type-item {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.point-config {
  display: flex;
  align-items: center;
  gap: 5px;
}

.subtotal-points {
  color: var(--el-text-color-secondary);
  min-width: 80px;
}

.point-adjustment-tools {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.adjustment-tip {
  color: var(--el-text-color-secondary);
  font-size: 0.9em;
}

.upload-reference {
  margin-top: 10px;
}

.paper-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.paper-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 15px;
}

.paper-meta-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.paper-details {
  margin-top: 15px;
  padding: 10px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.paper-result {
  background: var(--el-bg-color-overlay);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.correct {
  color: var(--el-color-success);
}

.incorrect {
  color: var(--el-color-danger);
}

.answer-detail {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-top: 10px;
  font-size: 0.9em;
  color: var(--el-text-color-secondary);
}

.answer-detail span {
  margin-right: 20px;
}
</style>