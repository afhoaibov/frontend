<template>
  <div class="ranking-container">
    <el-card class="ranking-card">
      <template #header>
        <div class="card-header">
          <h2>实时排行榜</h2>
          <el-radio-group v-model="currentTab" @change="handleTabChange">
            <el-radio-button label="score">综合排行</el-radio-button>
<!--            <el-radio-button label="followers">粉丝排行</el-radio-button>-->
<!--            <el-radio-button label="posts">动态排行</el-radio-button>-->
          </el-radio-group>
        </div>
      </template>

      <div class="ranking-content">
        <el-table :data="rankingData" style="width: 100%" v-loading="loading">
          <el-table-column label="排名" width="80" align="center">
            <template #default="{ $index }">
              <div class="rank-number" :class="getRankClass($index + 1)">
                {{ $index + 1 }}
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="用户" min-width="200">
            <template #default="{ row }">
              <div class="user-info">
                <el-avatar :size="40" :src="row.user.avatar">
                  {{ row.user.nickname ? row.user.nickname.charAt(0) : row.user.username.charAt(0) }}
                </el-avatar>
                <div class="user-details">
                  <div class="username">{{ row.user.nickname || row.user.username }}</div>
                  <div class="user-bio">{{ row.user.bio || '这个人很懒，什么都没写~' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="分数" width="100" align="center">
            <template #default="{ row }">
              <span class="score">{{ getScore(row) }}</span>
            </template>
          </el-table-column>
          
          <!-- <el-table-column label="粉丝" width="80" align="center">
            <template #default="{ row }">
              <span class="follower-count">{{ row.user.followerCount }}</span>
            </template>
          </el-table-column> -->
          
          <el-table-column label="动态" width="80" align="center">
            <template #default="{ row }">
              <span class="post-count">{{ row.user.postCount }}</span>
            </template>
          </el-table-column>
          
<!--          <el-table-column label="操作" width="120" align="center">-->
<!--            <template #default="{ row }">-->
<!--              <el-button -->
<!--                type="primary" -->
<!--                size="small" -->
<!--                @click="viewProfile(row.user.id)"-->
<!--              >-->
<!--                查看主页-->
<!--              </el-button>-->
<!--            </template>-->
<!--          </el-table-column>-->
        </el-table>

        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { rankingApi } from '@/utils/api'
import { ElMessage } from 'element-plus'

export default {
  name: 'Ranking',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const currentTab = ref('score')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const rankingData = ref([])

    // 获取排行榜数据
    const fetchRankingData = async () => {
      loading.value = true
      try {
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value - 1
        
        let response
        switch (currentTab.value) {
          case 'score':
            response = await rankingApi.getCompositeScoreRanking(start, end)
            break
          case 'followers':
            response = await rankingApi.getFollowerRanking(start, end)
            break
          case 'posts':
            response = await rankingApi.getPostRanking(start, end)
            break
        }
        
        // 适配新的数据格式，使用userRankings数组
        const userRankings = response.userRankings || []
        // 按照compositeScore进行降序排序
        rankingData.value = userRankings.sort((a, b) => b.compositeScore - a.compositeScore)
        total.value = response.total || 0
      } catch (error) {
        ElMessage.error('获取排行榜数据失败')
        console.error('获取排行榜数据失败:', error)
      } finally {
        loading.value = false
      }
    }

    // 处理标签页切换
    const handleTabChange = () => {
      currentPage.value = 1
      fetchRankingData()
    }

    // 处理页码变化
    const handleCurrentChange = () => {
      fetchRankingData()
    }

    // 处理页面大小变化
    const handleSizeChange = () => {
      currentPage.value = 1
      fetchRankingData()
    }

    // 获取分数显示
    const getScore = (row) => {
      switch (currentTab.value) {
        case 'score':
          return row.compositeScore
        case 'followers':
          return row.user.followerCount
        case 'posts':
          return row.user.postCount
        default:
          return row.compositeScore
      }
    }

    // 获取排名样式
    const getRankClass = (rank) => {
      if (rank === 1) return 'rank-gold'
      if (rank === 2) return 'rank-silver'
      if (rank === 3) return 'rank-bronze'
      return 'rank-normal'
    }

    // 查看用户主页
    const viewProfile = (userId) => {
      router.push(`/profile/${userId}`)
    }

    onMounted(() => {
      fetchRankingData()
    })

    return {
      loading,
      currentTab,
      currentPage,
      pageSize,
      total,
      rankingData,
      handleTabChange,
      handleCurrentChange,
      handleSizeChange,
      getScore,
      getRankClass,
      viewProfile
    }
  }
}
</script>

<style scoped>
.ranking-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.ranking-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.ranking-content {
  margin-top: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  flex: 1;
}

.username {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.user-bio {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  margin: 0 auto;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #8b6914;
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0, #e5e5e5);
  color: #666;
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  color: #8b4513;
}

.rank-normal {
  background: #f5f7fa;
  color: #606266;
}

.score {
  font-weight: 600;
  color: #409eff;
  font-size: 16px;
}

.follower-count, .post-count {
  font-weight: 500;
  color: #606266;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .ranking-container {
    padding: 0 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .el-table {
    font-size: 12px;
  }
}
</style> 