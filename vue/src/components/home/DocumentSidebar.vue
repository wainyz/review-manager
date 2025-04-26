<template>
  <el-drawer v-model="visible" title="个人文档" direction="ltr" @update:modelValue="$emit('update:modelValue', $event)">
    <div class="document-list">
      <div class="search-box">
        <el-input
          :model-value="searchQuery"
          placeholder="搜索文档"
          clearable
          :prefix-icon="Search"
          @update:model-value="$emit('update:searchQuery', $event)"
        />
      </div>
      <div class="document-cards">
        <el-card
          v-for="document in documents"
          :key="document.id"
          class="doc-card"
          shadow="hover"
          @click="$emit('select-document', document.id)"
        >
          <template #header>
            <div class="card-header">
              <span>{{ document.title }}</span>
            </div>
          </template>
          <div class="card-content">
            <el-tag type="info" size="small">id:{{ document.id }}</el-tag>
            <el-tag type="info" size="small">知识点覆盖度：{{ document.coverage }}</el-tag>
            <el-tag type="info" size="small">知识点熟悉度：{{ document.mastery }}</el-tag>
          </div>
        </el-card>
        <el-empty v-if="documents.length === 0" description="暂无文档" />
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'
import { computed, ref } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  documents: {
    type: Array,
    default: () => []
  },
  searchQuery: String
})

const emit = defineEmits(['update:modelValue', 'update:searchQuery', 'select-document'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
</script>

<style scoped>
.document-list {
  padding: 20px;
  background-color: var(--card-bg);
  color: var(--text-color);
}

.search-box {
  margin-bottom: 20px;
}

.document-cards {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  padding: 8px;
}

.doc-card {
  border-radius: 12px;
  transition: transform 0.2s;
}

.doc-card:hover {
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-align: left;
  font-weight: 500;
}
#controller_element{
  text-align: left;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>