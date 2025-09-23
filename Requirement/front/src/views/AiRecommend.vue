<template>
  <div>
    <div class="hero">
      <div class="hero-icon">
        <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
          <path d="M12 2a10 10 0 100 20 10 10 0 000-20zm-1 5h2v6h-2V7zm1 10a1.5 1.5 0 110-3 1.5 1.5 0 010 3z" />
        </svg>
      </div>
      <div>
        <div class="hero-title">AI 智能任务推荐</div>
        <div class="hero-subtitle">基于你的技能自动匹配，更快发现合适的任务</div>
      </div>
    </div>

    <div class="toolbar">
      <el-button type="primary" class="ripple-container" @click="load" :loading="loading">获取推荐</el-button>
      <span class="tip">说明：系统将读取你的技能（Profile 中的 skills），调用大模型生成推荐。</span>
    </div>

    <el-alert v-if="error" :title="error" type="error" show-icon class="mb8" />

    <div v-if="list.length" class="card-grid">
      <el-card v-for="item in list" :key="item.id" class="task-card" shadow="never">
        <div class="card-header">
          <div class="title ellipsis-2">{{ item.title || ('任务 #' + item.id) }}</div>
        </div>
        <div class="card-body">
          <div class="reason" v-if="item.reason">
            <span class="reason-key">推荐理由</span>
            <span class="reason-val">{{ item.reason }}</span>
          </div>
        </div>
        <div class="card-actions">
          <el-button size="small" type="primary" @click="applyById(item)" :disabled="appliedIds.includes(item.id)">
            {{ appliedIds.includes(item.id) ? '已申请' : '一键申请' }}
          </el-button>
          <el-button size="small" @click="goSquare">去任务广场</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-else-if="!loading" description="暂无推荐，点击上方“获取推荐”试试" />
  </div>
  
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { getAiMatch } from '../api/ai';
import { applyTask, listMyAppliedTaskIds } from '../api/task';

const router = useRouter();
const loading = ref(false);
const error = ref('');
const list = ref([]);
const appliedIds = ref([]);

async function load(){
  loading.value = true; error.value = '';
  try {
    const data = await getAiMatch();
    // http.js 统一返回 payload.data；此处期望 { list: [...] }
    list.value = Array.isArray(data?.list) ? data.list : [];
    // 同步“已申请”的任务ID，避免重复申请
    await fetchAppliedIds();
    if (!list.value.length) {
      ElMessage.info('大模型暂未返回匹配项，可稍后再试');
    }
  } catch (e) {
    error.value = e?.message || '获取推荐失败';
  } finally {
    loading.value = false;
  }
}

async function fetchAppliedIds(){
  try {
    const applied = await listMyAppliedTaskIds();
    const ids = Array.isArray(applied?.list) ? applied.list : (Array.isArray(applied) ? applied : []);
    appliedIds.value = ids.filter(x => typeof x === 'number');
  } catch (_) {
    appliedIds.value = [];
  }
}

async function applyById(item){
  try {
    await applyTask(item.id, { message: '来自AI推荐：我与该任务匹配度高，可高质量完成' });
    if (!appliedIds.value.includes(item.id)) appliedIds.value.push(item.id);
    ElMessage.success('已申请任务 #' + item.id);
  } catch(e){
    ElMessage.error(e?.message || '申请失败');
  }
}

function goSquare(){ router.push('/'); }

onMounted(() => {
  // 自动尝试拉取一次，用户也可点击按钮刷新
  load();
});
</script>

<style scoped>
.hero { display:flex; align-items:center; gap:10px; margin-bottom: 12px; }
.hero-icon { width:36px; height:36px; color:#409eff; }
.hero-title { font-size:18px; font-weight:700; }
.hero-subtitle { font-size:12px; color:#666; }
.toolbar { display:flex; align-items:center; gap:12px; margin-bottom:10px; }
.tip { font-size:12px; color:#666; }
.mb8 { margin-bottom:8px; }
.card-grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
.task-card { border-radius:12px; overflow:hidden; }
.card-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:8px; }
.title { font-weight:600; font-size:16px; line-height:1.4; max-width: 75%; }
.ellipsis-2 { display:-webkit-box; line-clamp:2; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.card-body { color: var(--el-text-color-regular); font-size: 13px; }
.reason { display:flex; gap:8px; margin: 6px 0; }
.reason-key { color: var(--el-text-color-secondary); }
.reason-val { color: var(--el-text-color-primary); font-weight: 500; }
.card-actions { margin-top: 12px; display:flex; gap:8px; }
</style>
