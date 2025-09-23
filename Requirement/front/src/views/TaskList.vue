<template>
  <div>
  <div class="hero">
    <div class="hero-icon">
      <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
        <path d="M12 2a10 10 0 100 20 10 10 0 000-20zm-1 5h2v6h-2V7zm1 10a1.5 1.5 0 110-3 1.5 1.5 0 010 3z" />
      </svg>
    </div>
    <div>
      <div class="hero-title">任务广场</div>
      <div class="hero-subtitle">发现与发布校园兼职，智能筛选、快捷申请</div>
    </div>
  </div>
  <el-form :inline="true" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="搜索任务" style="width:150px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width:140px">
          <el-option :value="''" label="全部" />
          <el-option :value="1" label="发布中" />
          <el-option :value="2" label="申请中" />
          <el-option :value="3" label="进行中" />
          <el-option :value="5" label="完成" />
          <el-option :value="6" label="取消" />
        </el-select>
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="query.tag" placeholder="单个标签" style="width:120px" />
      </el-form-item>
      <el-form-item>
  <el-button type="primary" class="ripple-container" @click="load">搜索</el-button>
      </el-form-item>
      <el-form-item>
  <el-button class="ripple-container" @click="goPublish">发布任务</el-button>
      </el-form-item>
      <el-form-item>
        <el-button type="success" class="ripple-container ai-btn" @click="goAi">
          <span class="ai-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M8.5 3.5l1.2 2.9c.2.5.7.8 1.2.8h3c.7 0 1 .8.5 1.3l-2.4 2.4c-.4.4-.5 1-.3 1.5l1.2 3c.3.7-.5 1.4-1.1.9l-2.7-1.9c-.5-.3-1.1-.3-1.6 0l-2.7 1.9c-.6.5-1.4-.2-1.1-.9l1.2-3c.2-.5.1-1.1-.3-1.5L1.9 8.5c-.5-.5-.1-1.3.5-1.3h3c.6 0 1-.3 1.2-.8l1.2-2.9c.2-.7 1.3-.7 1.5 0z"/>
            </svg>
          </span>
          <span class="ai-text">AI 推荐</span>
          <span class="shine" aria-hidden="true"></span>
        </el-button>
      </el-form-item>
      <el-form-item label="申请附言">
        <el-input v-model="userCtx.message" placeholder="申请理由(可选)" style="width:180px" />
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
        <el-tag v-if="item.status !== undefined && item.status !== null" :type="statusType(item.status)" size="small">{{ statusText(item.status) }}</el-tag>
      </div>
      <div class="card-body">
        <div class="meta-line">
          <span class="meta-key">预算</span>
          <span class="meta-val">{{ formatBudget(item) }}</span>
        </div>
        <div class="meta-line">
          <span class="meta-key">截止</span>
          <span class="meta-val">{{ formatDate(item.deadline) }}</span>
        </div>
      </div>
      <div class="card-actions">
        <!-- 仅当未申请且任务未完成时显示“申请” -->
        <el-button
          size="small"
          type="primary"
          class="ripple-container"
          @click="apply(item)"
          v-if="!appliedIds.includes(item.id) && item?.status !== 5"
        >申请</el-button>
        <!-- 仅当该任务在“我申请中(待处理)”列表里时显示“撤回” -->
        <el-button
          size="small"
          type="warning"
          class="ripple-container"
          @click="withdraw(item)"
          v-if="appliedIds.includes(item.id)"
        >撤回</el-button>
      </div>
    </el-card>
  </div>
  <el-empty v-else description="没有找到任务" />

  <div style="margin-top:8px;font-size:12px;color:#666">共 {{ total }} 条</div>
  <div style="margin-top:6px; display:flex; justify-content:flex-end;">
    <el-pagination
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :current-page="query.pageNo"
      :page-size="query.pageSize"
      :page-sizes="[5,10,20,50]"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
  </div>
</template>
<script setup>
import { reactive, ref, onMounted, watch } from 'vue';
import { listTasks, applyTask, withdrawTask, listMyAppliedTaskIds } from '../api/task';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store/auth';

// 默认显示全部状态：status 使用空字符串表示未筛选
const query = reactive({ pageNo:1, pageSize:10, keyword:'', status:'', tag:'' });
const list = ref([]);
const total = ref(0);
// 仅保留申请附言 message
const userCtx = reactive({ message: '我可以完成该任务' });
const auth = useAuthStore();
// 记录已申请任务ID（前端临时状态，真实应后端查询）
const appliedIds = ref([]);
const router = useRouter();

const STATUS_MAP = {
  1: { text: '发布中', type: 'success' },
  2: { text: '申请中', type: 'primary' },
  3: { text: '进行中', type: 'warning' },
  5: { text: '完成', type: 'info' },
  6: { text: '取消', type: 'danger' }
};

function statusText(v){
  return STATUS_MAP?.[v]?.text ?? '未知';
}
function statusType(v){
  return STATUS_MAP?.[v]?.type ?? 'default';
}
function formatBudget(item){
  const min = item?.budgetMin;
  const max = item?.budgetMax;
  if(min == null && max == null) return '-';
  if(min != null && max != null){
    return min === max ? `${min}` : `${min} - ${max}`;
  }
  return `${min ?? max}`;
}
function formatDate(v){
  if(!v) return '-';
  const s = String(v).replace('T', ' ');
  return s.length > 16 ? s.slice(0,16) : s;
}

async function load(){
  // 所有参数必填校验（允许 keyword / tag 为空字符串但字段必须存在）
  if(!query.pageNo || !query.pageSize){
    ElMessage.error('参数缺失'); return;
  }
  // 组装参数；仅在选择具体状态时才发送 status
  const params = {
    pageNo: query.pageNo,
    pageSize: query.pageSize,
    keyWord: query.keyword ?? '',
    tag: query.tag ?? ''
  };
  if(query.status !== '' && query.status !== null && query.status !== undefined){
    params.status = query.status;
  }
  try {
    const data = await listTasks(params);
  // 只展示非本人发布的任务
  const myId = auth?.user?.id;
  const all = data?.list || [];
  const filtered = myId ? all.filter(t => t.publisherId !== myId) : all;
  list.value = filtered;
  total.value = data?.total ?? filtered.length;
  await fetchAppliedIds();
  } catch(e){
    list.value = [];
    total.value = 0;
    ElMessage.error(e.message || '加载失败');
  }
}

// 单独拉取“我已申请”的任务ID列表，以在登录后/切换账号时恢复按钮状态
async function fetchAppliedIds(){
  const myId = auth?.user?.id;
  if (!myId) { appliedIds.value = []; return; }
  try {
    const applied = await listMyAppliedTaskIds();
    const ids = Array.isArray(applied?.list) ? applied.list : (Array.isArray(applied) ? applied : []);
    appliedIds.value = ids.filter(x => typeof x === 'number');
  } catch(_) {
    // 忽略错误，避免影响主列表
    appliedIds.value = [];
  }
}
async function apply(row){
  try {
    const payload = { message: userCtx.message };
    await applyTask(row.id, payload);
    if(!appliedIds.value.includes(row.id)) appliedIds.value.push(row.id);
    // 再次从后端拉取一次，确保多端/并发下一致
    await fetchAppliedIds();
    ElMessage.success('已申请');
  } catch(e){
    ElMessage.error(e.message || '申请失败');
  }
}

async function withdraw(row){
  try {
    await withdrawTask(row.id, {});
    appliedIds.value = appliedIds.value.filter(id => id !== row.id);
    // 再次从后端拉取一次，确保多端/并发下一致
    await fetchAppliedIds();
    ElMessage.success('已撤回');
  } catch(e){
    ElMessage.error(e.message || '撤回失败');
  }
}
function goPublish(){
  router.push('/publish');
}
function goAi(){
  router.push('/ai/recommend');
}

// 按钮渲染逻辑已内联至模板：
// - 申请：!appliedIds.includes(id) && status !== 5
// - 撤回：appliedIds.includes(id)

onMounted(load);

// 登录状态变化时，刷新“已申请”按钮状态
watch(() => auth?.user?.id, () => { fetchAppliedIds(); });

// 分页事件：页码或每页条数变化时自动刷新
function handlePageChange(p){
  if(!p) return; query.pageNo = p; load();
}
function handleSizeChange(s){
  if(!s) return; query.pageSize = s; query.pageNo = 1; load();
}
</script>

<style scoped>
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-top: 10px;
}
.task-card {
  border-radius: 12px;
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.title {
  font-weight: 600;
  font-size: 16px;
  line-height: 1.4;
  max-width: 75%;
}
.ellipsis-2 {
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-body {
  color: var(--el-text-color-regular);
  font-size: 13px;
}
.meta-line { display: flex; gap: 8px; margin: 6px 0; }
.meta-key { color: var(--el-text-color-secondary); }
.meta-val { color: var(--el-text-color-primary); font-weight: 500; }
.card-actions { margin-top: 12px; display: flex; gap: 8px; }

/* AI 推荐按钮特效 */
.ai-btn {
  position: relative;
  overflow: hidden;
  border: none;
  color: #fff !important;
  background: linear-gradient(135deg, #7c3aed 0%, #06b6d4 50%, #22c55e 100%);
  background-size: 200% 200%;
  box-shadow: 0 6px 20px rgba(124,58,237,0.35), 0 2px 8px rgba(34,197,94,0.2);
  transition: transform .2s ease, box-shadow .2s ease;
}
.ai-btn:hover {
  transform: translateY(-1px) scale(1.02);
  box-shadow: 0 10px 28px rgba(124,58,237,0.5), 0 4px 14px rgba(34,197,94,0.3);
  animation: aiGradient 2.5s ease infinite;
}
.ai-btn:active { transform: translateY(0) scale(0.99); }

.ai-icon { display: inline-flex; margin-right: 6px; filter: drop-shadow(0 1px 1px rgba(0,0,0,0.2)); }
.ai-text { letter-spacing: .5px; font-weight: 600; }

/* 斜向闪光 */
.shine {
  position: absolute;
  top: -150%;
  left: -50%;
  width: 50%;
  height: 300%;
  transform: rotate(25deg);
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,.22) 50%, rgba(255,255,255,0) 100%);
  transition: left .5s ease, top .5s ease;
}
.ai-btn:hover .shine {
  left: 120%;
  top: -120%;
}

@keyframes aiGradient {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
</style>
