
<!-- 搜索好友对话框 -->
<el-dialog
  v-model="showSearchDialog"
  title="搜索好友"
  width="30%"
  :close-on-click-modal="false"
>
  <div class="search-container">
    <el-input
      v-model="searchKeyword"
      placeholder="请输入用户名或邮箱"
      class="search-input"
    >
      <template #append>
        <el-button @click="handleSearch">搜索</el-button>
      </template>
    </el-input>
  </div>

  <!-- 搜索结果列表 -->
  <div class="search-results" v-if="searchResults.length > 0">
    <div v-for="(user, index) in searchResults" :key="'search-' + index" class="search-item">
      <el-avatar :size="40" :src="user.avatar || 'head.png'" :class="'user-avatar'" />
      <div class="user-info">
        <div class="user-name">{{ user.username }}</div>
        <div class="user-email">{{ user.email }}</div>
      </div>
      <div class="action-area">
        <el-button 
          v-if="!user.isFriend && !user.hasRequested"
          type="primary" 
          size="small"
          @click="sendFriendRequest(user.user_id)"
          :loading="user.isRequesting"
        >
          申请添加
        </el-button>
        <el-tag v-else-if="user.hasRequested" type="warning">已申请</el-tag>
        <el-tag v-else type="success">已是好友</el-tag>
      </div>
    </div>
  </div>
  <div v-else-if="hasSearched" class="empty-search">
    <el-empty description="未找到相关用户" />
  </div>
</el-dialog>
