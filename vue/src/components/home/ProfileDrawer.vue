<template>
  <el-drawer v-model="visible" title="个人信息" direction="rtl" @update:modelValue="$emit('update:modelValue', $event)">
    <div class="profile-info">
      <div class="avatar-section">
        <el-avatar :size="100" src="head.png" />
        <h3>{{ userInfo.username }}</h3>
      </div>
      <el-divider />
      <div class="info-section">
        <el-descriptions :column="1">
          <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
          <el-descriptions-item label="文档数量">{{ userInfo.documentCount }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="action-section">
        <el-button type="primary" @click="showEditDialog">修改信息</el-button>
        <el-button type="danger" @click="$emit('logout')">退出登录</el-button>
      </div>
    </div>
    <EditProfileDialog
      v-model="editDialogVisible"
      :user-info="userInfo"
      @update-profile="handleProfileUpdate"
    />
  </el-drawer>
</template>

<script setup>
import { computed, ref } from 'vue'
import EditProfileDialog from './EditProfileDialog.vue'

const props = defineProps({
  modelValue: Boolean,
  userInfo: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'edit-profile', 'logout', 'update-profile'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const editDialogVisible = ref(false)

const showEditDialog = () => {
  editDialogVisible.value = true
}

const handleProfileUpdate = (updatedInfo) => {
  emit('update-profile', updatedInfo)
}
</script>

<style scoped>
.profile-info {
  padding: 20px;
  background-color: var(--card-bg);
  color: var(--text-color);
}

.avatar-section {
  text-align: center;
  margin-bottom: 20px;
}

.avatar-section h3 {
  margin-top: 10px;
  color: var(--text-color);
}

.info-section {
  margin: 20px 0;
  color: var(--text-color);
}

.action-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>