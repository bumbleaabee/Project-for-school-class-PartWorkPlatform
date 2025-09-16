<template>
  <div style="padding:16px;">
    <div class="hero">
      <div class="hero-icon">
        <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M12 2l3 7h7l-5.5 4.1L18 20l-6-3.8L6 20l1.5-6.9L2 9h7l3-7z"/></svg>
      </div>
      <div>
        <div class="hero-title">提交评价</div>
        <div class="hero-subtitle">为合作对象打分与反馈，促进良好信用</div>
      </div>
    </div>
    <h2>提交评价</h2>
    <el-form :model="form" label-width="110px" style="max-width:520px;">
      <el-form-item label="评价对象">
        <el-radio-group v-model="targetType" @change="()=>{ form.taskId=null; loadFinishedTasks(); }">
          <el-radio-button label="publisher">评价卖家（发布者）</el-radio-button>
          <el-radio-button label="assignee">评价买家（接单人）</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="任务ID">
        <el-select v-model="form.taskId" placeholder="请选择已完成任务">
          <el-option
            v-for="t in finishedTasks"
            :key="t.id"
            :label="targetType==='publisher'
              ? `${t.id} - ${t.title}（卖家：${t.publisherName || t.publisherId}）`
              : `${t.id} - ${t.title}（买家：${t.assigneeName || t.executorName || t.assigneeId || t.executorId || t.chosenApplicantId}）`"
            :value="t.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="!(finishedTasks && finishedTasks.length)" label-width="0">
        <el-alert
          :title="targetType==='publisher'
            ? '没有可评价的任务。仅买家（接单人）可对卖家（发布者）进行评价；若当前账号未作为接单人完成任务，则此处为空。'
            : '没有可评价的任务。仅卖家（发布者）可对买家（接单人）进行评价；若当前账号未发布完成的任务或任务无接单人，则此处为空。'"
          type="info"
          :closable="false"
          show-icon
        />
      </el-form-item>
      <el-form-item label="评分(1-5)">
        <el-input-number v-model="form.rating" :min="1" :max="5" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.content" placeholder="评价内容" type="textarea" :rows="3" />
      </el-form-item>
  <!-- 正式版：Reviewer 从 Token 解析，无需填写 -->
      <el-form-item>
  <el-button type="primary" class="ripple-container" :disabled="!form.taskId" @click="submit">提交</el-button>
        <el-button class="ripple-container" @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <div style="margin-top:32px;">
      <h3>最近收到的评价</h3>
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="用户ID" v-if="allowCustomUserId">
          <el-input-number v-model="query.userId" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="ripple-container" @click="loadReviews">刷新</el-button>
        </el-form-item>
      </el-form>
      <div v-if="reviews && reviews.length" class="card-grid">
        <el-card
          v-for="r in reviews"
          :key="r.id"
          class="review-card"
          shadow="never"
        >
          <div class="card-header">
            <div class="left">
              <el-rate v-model="r.rating" disabled allow-half />
            </div>
            <div class="right time">{{ formatDate(r.createdAt) }}</div>
          </div>
          <div class="card-body">
            <div class="meta-line"><span class="meta-key">评价ID</span><span class="meta-val">{{ r.id }}</span></div>
            <div class="meta-line"><span class="meta-key">任务</span><span class="meta-val">#{{ r.taskId }}</span></div>
            <div class="content">{{ r.content || '（无文字）' }}</div>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无评价" />
      <div style="margin-top:6px; display:flex; justify-content:flex-end;">
        <el-pagination
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="query.pageNo"
          :page-size="query.pageSize"
          :page-sizes="[5,10,20,50]"
          @current-change="p=>{ query.pageNo=p; loadReviews(); }"
          @size-change="s=>{ query.pageSize=s; query.pageNo=1; loadReviews(); }"
        />
      </div>
    </div>
  </div>
</template>
<script setup>
function formatDate(v){ if(!v) return '-'; const s = String(v).replace('T',' '); return s.length>16? s.slice(0,16): s; }
import { reactive, ref, onMounted, computed } from 'vue';
import { listMyAccepted, listMyPublished } from '../api/task';
import { ElMessage } from 'element-plus';
import { createReview, listUserReviews } from '../api/review';
import { useAuthStore } from '../store/auth';

const form = reactive({ taskId: null, rating: 5, content: '' });
// 评价对象：publisher = 评价卖家(发布者)，assignee = 评价买家(接单人)
const targetType = ref('publisher');
const finishedTasks = ref([]);
const hasEligibleTasks = computed(() => (finishedTasks.value?.length || 0) > 0);
const reviews = ref([]);
const total = ref(0);
const query = reactive({ userId: null, pageNo:1, pageSize:10 });
const allowCustomUserId = ref(false); // 如需查询他人评价可手动开启
const auth = useAuthStore();

async function submit(){
  if(!form.taskId || !form.rating){
    ElMessage.error('必填缺失'); return;
  }
  const payload = { taskId: form.taskId, rating: form.rating, content: form.content, targetType: targetType.value };
  try {
    await createReview(payload);
    ElMessage.success('提交成功');
    reset();
    loadReviews();
  } catch(e){
    ElMessage.error(e.message || '提交失败');
  }
}
function reset(){
  form.taskId = null;
  form.rating = 5;
  form.content = '';
}
async function loadFinishedTasks(){
  if (targetType.value === 'publisher') {
    // 买家评价卖家：当前用户作为接单人完成的任务
    const data = await listMyAccepted({ pageNo:1, pageSize:50 });
    finishedTasks.value = (data?.list || []).filter(t => String(t.status) == '5');
  } else {
    // 卖家评价买家：当前用户作为发布者发布且已完成的任务（需存在执行人）
    const data = await listMyPublished({ pageNo:1, pageSize:50 });
    finishedTasks.value = (data?.list || []).filter(t => String(t.status) == '5' && (t.assigneeId || t.executorId || t.chosenApplicantId));
  }
}
async function loadReviews(){
  if(!query.userId){ ElMessage.error('缺少用户ID'); return; }
  try {
    const data = await listUserReviews(query.userId, { pageNo: query.pageNo, pageSize: query.pageSize });
    reviews.value = data?.list || [];
    total.value = data?.total ?? reviews.value.length;
  } catch(e){
    ElMessage.error(e.message || '加载失败');
  }
}

onMounted(() => {
  // 默认查看当前登录用户的评价
  if(auth?.user?.id){
    query.userId = auth.user.id;
    loadReviews();
    loadFinishedTasks();
  }
});
</script>
<style scoped>
.card-grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap:16px; margin-top:10px; }
.review-card { border-radius:12px; overflow:hidden; }
.card-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:8px; }
.left { display:flex; align-items:center; gap:8px; }
.time { color:#6b7280; font-size:12px; }
.card-body { color: var(--el-text-color-regular); font-size:13px; }
.meta-line { display:flex; gap:8px; margin:4px 0; }
.meta-key { color: var(--el-text-color-secondary); }
.meta-val { color: var(--el-text-color-primary); font-weight:500; }
.content { margin-top:8px; color:#374151; }
</style>
