<template>
  <div style="padding:12px;">

    <div class="hero" style="position:relative;">
      <div class="hero-icon icon-pulse">
        <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M3 6h18v2H3V6zm2 4h14v2H5v-2zm-2 4h18v2H3v-2z"/></svg>
      </div>
      <div>
        <div class="hero-title" style="position:relative;display:inline-block;">
          进行中任务
          <span v-if="list.some(t=>t.hasFinishRequest)" style="position:absolute;top:-6px;right:-18px;width:12px;height:12px;background:#e53935;border-radius:50%;display:inline-block;"></span>
        </div>
        <div class="hero-subtitle">查看已选择执行人的任务并进行验收</div>
      </div>
    </div>

    <el-form :inline="true" @submit.prevent>
      <el-form-item>
        <el-button type="primary" class="ripple-container" @click="load">刷新</el-button>
      </el-form-item>
    </el-form>

    <!-- 卡片网格 -->
    <div v-if="list && list.length" class="card-grid">
      <el-card
        v-for="item in list"
        :key="item.id"
        class="task-card"
        shadow="never"
      >
        <div class="card-header">
          <div class="title ellipsis-2">{{ item.title || '未命名任务' }}</div>
          <el-tag type="warning" size="small">进行中</el-tag>
        </div>
        <div class="card-body">
          <div class="meta-line"><span class="meta-key">任务ID</span><span class="meta-val">{{ item.id }}</span></div>
          <div class="meta-line"><span class="meta-key">执行人</span><span class="meta-val">{{ item.assigneeName || item.assigneeId }}</span></div>
          <div class="meta-line"><span class="meta-key">截止</span><span class="meta-val">{{ formatDate(item.deadline) }}</span></div>
        </div>
        <div class="card-actions">
          <el-popconfirm title="确认验收该任务吗？" @confirm="()=>accept(item)">
            <template #reference>
              <el-button size="small" type="success" class="ripple-container" :disabled="item.status!==3 && item.status!=='in_progress'" style="position:relative;">
                验收
                <span v-if="item.hasFinishRequest" style="position:absolute;top:-4px;right:-4px;width:10px;height:10px;background:#e53935;border-radius:50%;display:inline-block;"></span>
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无进行中的任务" />

    <div style="margin-top:8px;font-size:12px;color:#666">共 {{ total }} 条</div>
    <div style="margin-top:6px; display:flex; justify-content:flex-end;">
      <el-pagination
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page="query.pageNo"
        :page-size="query.pageSize"
        :page-sizes="[5,10,20,50]"
        @current-change="p=>{ query.pageNo=p; load(); }"
        @size-change="s=>{ query.pageSize=s; query.pageNo=1; load(); }"
      />
    </div>
  </div>
  </template>
  <script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { ElMessage } from 'element-plus';
  import { listMyOngoing, acceptTask } from '../api/task';
  
  const query = reactive({ pageNo:1, pageSize:10 });
  const list = ref([]);
  const total = ref(0);
  const loading = ref(false);
  
  function formatDate(v){
    if(!v) return '-';
    const s = String(v).replace('T',' ');
    return s.length>16 ? s.slice(0,16) : s;
  }
  
  async function load(){
    loading.value = true;
    try{
      const data = await listMyOngoing({ pageNo: query.pageNo, pageSize: query.pageSize });
      const rows = (data?.list || []).map(t=>({
        id: t.id,
        title: t.title,
        status: t.status,
        deadline: t.deadline || t.dueTime || t.endTime,
        assigneeId: t.assigneeId || t.buyerId || t.executorId,
        assigneeName: t.assigneeName || t.buyerName || t.executorName,
        hasFinishRequest: !!t.hasFinishRequest // 后端需返回该字段，表示买家已申请完成
      }));
      list.value = rows; total.value = data?.total ?? rows.length;
    }catch(e){
      list.value = []; total.value = 0;
      ElMessage.error(e.message || '加载失败');
    }finally{ loading.value = false; }
  }
  
  async function accept(row){
    try{
      await acceptTask(row.id);
      ElMessage.success('验收成功');
      await load();
    }catch(e){
      ElMessage.error(e.message || '验收失败');
    }
  }
  
  onMounted(load);
  </script>
<style scoped>
.card-grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap:16px; margin-top:10px; }
.task-card { border-radius:12px; overflow:hidden; }
.card-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:8px; }
.title { font-weight:600; font-size:16px; line-height:1.4; max-width:75%; }
.ellipsis-2 { display:-webkit-box; -webkit-line-clamp:2; line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.card-body { color: var(--el-text-color-regular); font-size:13px; }
.meta-line { display:flex; gap:8px; margin:6px 0; }
.meta-key { color: var(--el-text-color-secondary); }
.meta-val { color: var(--el-text-color-primary); font-weight:500; }
.card-actions { margin-top:12px; display:flex; gap:8px; }
</style>
