<template>
  <el-dialog
    v-model="visible"
    title="上传文档"
    width="30%"
    :close-on-click-modal="false"
    @close="handleClose"
    @update:modelValue="$emit('update:modelValue', $event)"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="文件" prop="file">
        <el-upload
          class="upload-demo"
          action="#"
          :auto-upload="false"
          :limit="1"
          :on-change="handleFileChange"
          accept=".pdf,.doc,.docx,.txt"
        >
          <template #trigger>
            <el-button type="primary">选择文件</el-button>
          </template>
          <template #tip>
            <div class="el-upload__tip">
              支持 pdf、doc、docx、txt 格式文件
            </div>
          </template>
        </el-upload>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入文档标题" />
      </el-form-item>
      <el-form-item label="发布">
        <el-switch v-model="form.isPublish" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button
          type="primary"
          @click="submitUpload"
          :loading="loading"
        >
          确认上传
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'submit', 'close'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const formRef = ref(null)
const loading = ref(false)
const form = ref({
  file: null,
  title: '',
  isPublish: false
})

const rules = {
  file: [{ required: true, message: '请选择文件', trigger: 'change' }],
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }]
}

const handleFileChange = (file) => {
  if (file) {
    form.value.file = file.raw
  } else {
    form.value.file = null
  }
}

const submitUpload = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    const formData = new FormData()
    formData.append('file', form.value.file)
    formData.append('title', form.value.title.trim())
    formData.append('isPublish', form.value.isPublish)
    await emit('submit', formData)
    ElMessage.success('上传成功')
    visible.value = false
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('上传失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value.file = null
  emit('close')
}
</script>