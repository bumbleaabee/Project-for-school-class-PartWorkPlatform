<template>
  <div style="padding:12px;">
    <div class="hero">
      <div class="hero-icon icon-pulse">
        <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M7 4h10l1 4H6l1-4zm-1 6h12l1 8H5l1-8zm3 2v4h2v-4H9zm4 0v4h2v-4h-2z"/></svg>
      </div>
      <div>
        <div class="hero-title">我的发布</div>
        <div class="hero-subtitle">查看我发布的任务，管理申请并选择执行人</div>
      </div>
    </div>
    <el-form :inline="true" @submit.prevent>
      <el-form-item>
        <el-button type="primary" class="ripple-container" @click="load">查询</el-button>
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
            <span class="meta-key">任务ID</span>
            <span class="meta-val">{{ item.id }}</span>
          </div>
          <div class="meta-line">
            <span class="meta-key">截止</span>
            <span class="meta-val">{{ formatDate(item.deadline) }}</span>
          </div>
        </div>
        <div class="card-actions">
          <el-button
            size="small"
            class="ripple-container"
            @click="openApplications(item)"
            :disabled="item.status===5 || item.status===6"
          >查看申请/选择</el-button>
        </div>
      </el-card>
    </div>
    <el-empty v-else description="暂无任务" />
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

    <el-dialog v-model="appDialog.visible" :title="`任务#${appDialog.task?.id || ''} 申请列表`" width="760px">
      <div v-if="appDialog.task" style="margin-bottom:8px;color:#666;">{{ appDialog.task.title }}</div>
      <el-table :data="applications" v-loading="loadingApps" size="small" style="width:100%;">
        <el-table-column prop="id" label="#" width="70" />
        <el-table-column prop="applicantId" label="申请人ID" width="120" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="message" label="附言" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="createdAt" label="时间" width="170" />
        <el-table-column label="操作" width="300">
          <template #default="{row}">
              <el-button
                size="small"
                @click="viewResume(row)"
                :disabled="row._canViewResume !== true"
                :title="row._canViewResume ? '打开对方简历' : '仅对申请中或已被选中的申请可查看简历'"
              >查看简历</el-button>
              <el-button
                size="small"
                type="success"
                class="ripple-container"
                @click="choose(row)"
                :disabled="!(row._canChoose === true)"
              >选择为执行人</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-space>
          <el-pagination
            layout="total, sizes, prev, pager, next, jumper"
            :page-size="appQuery.pageSize"
            :total="appTotal"
            :current-page="appQuery.pageNo"
            :page-sizes="[5,10,20,50]"
            @current-change="p=>{ appQuery.pageNo=p; loadApplications(); }"
            @size-change="s=>{ appQuery.pageSize=s; appQuery.pageNo=1; loadApplications(); }"
          />
          <el-button @click="appDialog.visible=false">关闭</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
// 状态映射，复用任务广场的风格
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
import { reactive, ref, onMounted } from 'vue';
import { listMyPublished, getTaskApplications, chooseTaskApplicant } from '../api/task';
import { getResume } from '../api/resume';
import { ElMessage } from 'element-plus';

const query = reactive({ pageNo:1, pageSize:10 });
const list = ref([]);
const total = ref(0);
const loading = ref(false);

async function load(){
  loading.value = true;
  try{
    const data = await listMyPublished({ pageNo: query.pageNo, pageSize: query.pageSize });
    list.value = data?.list || [];
    total.value = data?.total ?? list.value.length;
  }catch(e){
    list.value = []; total.value = 0;
    ElMessage.error(e.message || '加载失败');
  }finally{ loading.value = false; }
}

// 申请对话框逻辑
const appDialog = reactive({ visible:false, task:null });
const applications = ref([]);
const loadingApps = ref(false);
const appTotal = ref(0);
const appQuery = reactive({ pageNo:1, pageSize:10 });

function openApplications(task){
  appDialog.task = task;
  appDialog.visible = true;
  appQuery.pageNo = 1;
  loadApplications();
}

async function loadApplications(){
  if(!appDialog.task) return;
  loadingApps.value = true;
  try{
    const data = await getTaskApplications(appDialog.task.id, { pageNo: appQuery.pageNo, pageSize: appQuery.pageSize });
    const currentTaskStatus = appDialog.task?.status;
    const list = (data?.list || []).map(a=>{
      const statusRaw = a?.status; // 后端：0申请中 1被拒绝 2撤回 3被选中
      const status = statusRaw ?? 'pending';
      // 仅“申请中(0)”可被选择；任务状态需为 发布中(1)/申请中(2)
      const canChoose = (status === 0 || status === 'pending') && (currentTaskStatus === 1 || currentTaskStatus === 2);
      const statusNum = Number(status);
      const statusStr = String(statusRaw || '').toLowerCase();
      // 支持多种后端状态表示：0(申请中) / 2(被选中) / 3(历史实现) / 'pending' / 'chosen' / 'selected'
      const canViewResume = (
        statusNum === 0 || statusStr === '0' || statusStr === 'pending' ||
        statusNum === 2 || statusStr === '2' ||
        statusNum === 3 || statusStr === '3' || statusStr === 'chosen' || statusStr === 'selected'
      );
      return {
        // 明确区分：applicationId 是申请记录ID；applicantId 是用户ID
        applicationId: a.applicationId ?? a.appId ?? a.id,
        applicantId: a.applicant?.id ?? a.applicantId ?? a.applicantUserId ?? a.userId,
        nickname: a.applicant?.nickname ?? a.nickname ?? a.applicantNickname ?? '-',
        message: a.message ?? a.reason ?? '',
        status,
        _canChoose: canChoose,
        _canViewResume: canViewResume,
        createdAt: a.createdAt ?? a.gmtCreate ?? a.createTime
      };
    });
    applications.value = list; appTotal.value = data?.total ?? list.length;
  }catch(e){
    applications.value = []; appTotal.value = 0;
    ElMessage.error(e.message || '加载申请失败');
  }finally{ loadingApps.value = false; }
}

async function choose(row){
  if(!appDialog.task) return;
  try{
    const appId = row.applicationId ?? row.id; // 兼容旧数据结构
    await chooseTaskApplicant(appDialog.task.id, appId);
    ElMessage.success('已选择执行人');
    await loadApplications();
    await load();
  }catch(e){
    ElMessage.error(e.message || '选择失败');
  }
}

async function viewResume(row){
  try{
    const uid = row.applicantId;
    if(!uid){ return ElMessage.warning('缺少申请人ID'); }
    const url = await getResume(uid);
    if(!url){ return ElMessage.info('对方尚未上传简历'); }
    const finalUrl = /^https?:\/\//i.test(url) ? url : (url.startsWith('/') ? url : `/${url}`);
    window.open(finalUrl, '_blank');
  }catch(e){
    ElMessage.error(e.message || '获取简历失败');
  }
}

onMounted(load);
</script>
<style scoped>
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  margin-top: 10px;
}
.task-card { border-radius: 12px; overflow: hidden; }
.card-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:8px; }
.title { font-weight:600; font-size:16px; line-height:1.4; max-width:75%; }
.ellipsis-2 { display:-webkit-box; -webkit-line-clamp:2; line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.card-body { color: var(--el-text-color-regular); font-size:13px; }
.meta-line { display:flex; gap:8px; margin:6px 0; }
.meta-key { color: var(--el-text-color-secondary); }
.meta-val { color: var(--el-text-color-primary); font-weight:500; }
.card-actions { margin-top:12px; display:flex; gap:8px; }
</style>
