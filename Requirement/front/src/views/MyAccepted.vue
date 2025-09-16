<template>
  <div class="page-wrap">
    <div class="hero">
      <div class="hero-icon icon-pulse">
        <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M5 4h14v2H5V4zm2 4h10v2H7V8zm-2 4h14v2H5v-2zm2 4h10v2H7v-2z"/></svg>
      </div>
      <div>
        <div class="hero-title">我接的单</div>
        <div class="hero-subtitle">展示当前账号作为接单人参与的所有任务</div>
      </div>
    </div>

    <div class="toolbar">
      <el-button type="primary" class="ripple-container" @click="load">刷新</el-button>
    </div>

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
          <el-tag :type="statusType(item.status)" size="small">{{ statusText(item.status) }}</el-tag>
        </div>
        <div class="card-body">
          <div class="meta-line"><span class="meta-key">任务ID</span><span class="meta-val">{{ item.id }}</span></div>
          <div class="meta-line"><span class="meta-key">发布者</span><span class="meta-val">{{ item.publisherName || item.publisherId }}</span></div>
          <div class="meta-line"><span class="meta-key">截止</span><span class="meta-val">{{ formatDate(item.deadline) }}</span></div>
        </div>
        <div class="card-actions">
          <el-tooltip
            v-if="item.status == 3"
            :content="item.hasFinishRequest ? '已申请完成，请等待卖家验收' : '向卖家发起申请完成'"
            placement="top"
          >
            <el-badge :is-dot="item.hasFinishRequest" type="danger">
              <el-button
                type="warning" size="small"
                :disabled="item.hasFinishRequest"
                :loading="item._applying"
                @click="applyFinish(item)"
              >申请完成</el-button>
            </el-badge>
          </el-tooltip>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无任务" />

    <div class="list-footer">共 {{ total }} 条</div>
    <div class="pager-wrap">
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
import { listMyAccepted, applyTaskFinish } from '../api/task';

const query = reactive({ pageNo:1, pageSize:10 });
const list = ref([]);
const total = ref(0);
const loading = ref(false);

// 纯前端幂等：记录“已申请完成”的任务ID，减少重复点击；与后端返回的 hasFinishRequest 合并
const FIN_REQ_KEY = 'finishRequestedIds';
function loadFinishRequestedSet(){
  try{
    const raw = localStorage.getItem(FIN_REQ_KEY);
    const arr = raw ? JSON.parse(raw) : [];
    return new Set(Array.isArray(arr) ? arr.filter(x=>typeof x==='number') : []);
  }catch{ return new Set(); }
}
function saveFinishRequestedSet(set){
  try{
    const arr = Array.from(set);
    localStorage.setItem(FIN_REQ_KEY, JSON.stringify(arr));
  }catch{ /* 忽略 */ }
}

async function load(){
  loading.value = true;
  try{
    const data = await listMyAccepted({ pageNo: query.pageNo, pageSize: query.pageSize });
    const cached = loadFinishRequestedSet();
    const rows = (data?.list || []).map(t=>({
      id: t.id,
      title: t.title,
      status: t.status,
      deadline: t.deadline || t.dueTime || t.endTime,
      publisherId: t.publisherId,
      publisherName: t.publisherName || t.publisherNickname || t.publisher,
      // 兼容不同后端字段名：hasFinishRequest/finishRequested/hasApplyFinish
      hasFinishRequest: !!(t.hasFinishRequest ?? t.finishRequested ?? t.hasApplyFinish) || cached.has(t.id)
    }));
    list.value = rows; total.value = data?.total ?? rows.length;
  }catch(e){
    list.value = []; total.value = 0;
    ElMessage.error(e.message || '加载失败');
  }finally{ loading.value = false; }
}

onMounted(load);

// 买家（接单人）申请完成
async function applyFinish(row){
  if(!row || !row.id) return;
  if(row._applying) return;
  row._applying = true;
  try{
    await applyTaskFinish(row.id);
    ElMessage.success('已申请完成，请等待卖家验收');
    // 本地幂等：写入缓存并更新行状态，避免重复点击
    const s = loadFinishRequestedSet(); s.add(row.id); saveFinishRequestedSet(s);
    row.hasFinishRequest = true;
    await load();
  }catch(e){
    // 若后端返回业务码 2003 或仅返回“已申请完成”类文案 -> 视为成功提示
    const msg = (e && e.message) ? String(e.message) : '';
    const isAlreadyAppliedMsg = /已申请完成|任务申请已完成|已提交申请完成/i.test(msg);
    if (e && (e.code === 2003 || e.code === '2003' || isAlreadyAppliedMsg)) {
      ElMessage.info(msg || '已申请完成，请等待卖家验收');
      const s = loadFinishRequestedSet(); s.add(row.id); saveFinishRequestedSet(s);
      row.hasFinishRequest = true;
      await load();
    } else {
      ElMessage.error(e.message || '申请失败');
    }
  }finally{
    row._applying = false;
  }
}

// 状态文本/类型与日期格式化函数
const STATUS_MAP = {
  1: { text: '发布中', type: 'success' },
  2: { text: '申请中', type: 'primary' },
  3: { text: '进行中', type: 'warning' },
  5: { text: '完成', type: 'info' },
  6: { text: '取消', type: 'danger' }
};
function statusText(v){ return STATUS_MAP?.[v]?.text ?? '未知'; }
function statusType(v){ return STATUS_MAP?.[v]?.type ?? 'default'; }
function formatDate(v){ if(!v) return '-'; const s = String(v).replace('T',' '); return s.length>16? s.slice(0,16): s; }
</script>
<style scoped>
.page-wrap { width: 100%; padding: 12px; }
.hero { margin-bottom: 10px; }
.hero-icon { width:28px; height:28px; color: var(--brand-1); }
.toolbar { display:flex; justify-content:flex-end; margin-bottom: 8px; }
.list-footer { margin-top:10px; font-size:12px; color: #6b7280; }
.pager-wrap { margin-top:6px; display:flex; justify-content:flex-end; }

/* Add styles for the new card grid */
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