<template>
  <div class="publish-wrap">
    <div class="hero">
      <div class="hero-icon icon-pulse">
        <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
          <path d="M4 4h16v2H4V4zm2 4h12v2H6V8zm-2 4h16v2H4v-2zm2 4h12v2H6v-2z"/>
        </svg>
      </div>
      <div>
        <div class="hero-title">发布任务</div>
        <div class="hero-subtitle">完善信息以吸引更合适的同学来合作</div>
      </div>
    </div>

    <el-card class="form-card" shadow="never">
      <div class="form-grid">
        <div class="form-section">
          <div class="section-title">基础信息</div>
          <el-form :model="form" label-width="90px" @submit.prevent>
            <el-form-item label="标题">
              <el-input v-model="form.title" placeholder="一句话概括任务，如：海报设计-校庆主题" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="form.description" type="textarea" :rows="5" placeholder="补充更多细节、交付要求、验收标准、素材链接等" />
            </el-form-item>
            <el-form-item label="截止时间">
              <el-date-picker
                v-model="form.deadline"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                :shortcuts="dateShortcuts"
                placeholder="请选择截止时间"
              />
            </el-form-item>
            <el-form-item label="标签">
              <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如：设计,PS,海报" />
            </el-form-item>
          </el-form>
        </div>

        <div class="form-section">
          <div class="section-title">预算与期望</div>
          <el-form :model="form" label-width="90px" @submit.prevent>
            <el-form-item label="预算范围">
              <div class="inline-range">
                <el-input-number v-model="form.budgetMin" :min="0" :step="10" />
                <span class="sep">-</span>
                <el-input-number v-model="form.budgetMax" :min="0" :step="10" />
              </div>
            </el-form-item>
            <el-form-item label-width="0">
              <el-alert type="info" :closable="false" show-icon title="建议给出合理的预算范围，能提高申请意愿与效率" />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <div class="actions">
        <el-button type="primary" @click="submit" :loading="loading" class="btn-cta">提交发布</el-button>
        <el-button @click="reset" :disabled="loading">重置</el-button>
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../store/auth';
import { publishTask } from '../api/task';
import { getMyStats } from '../api/user';
import { ElMessage } from 'element-plus';

// 正式版：不再传 publisherId，由后端 token 解析
const form = reactive({ title:'', description:'', deadline:'', budgetMin:null, budgetMax:null, tags:'' });
const loading = ref(false);
const router = useRouter();
const auth = useAuthStore();

const dateShortcuts = [
  { text: '今天晚些时候', value: () => addHours(6) },
  { text: '明天这个时间', value: () => addDays(1) },
  { text: '一周后', value: () => addDays(7) }
];
function addHours(h){ const d = new Date(); d.setHours(d.getHours()+h); return d; }
function addDays(dy){ const d = new Date(); d.setDate(d.getDate()+dy); return d; }

async function submit(){
  loading.value = true;
  try {
  await publishTask(form);
    ElMessage.success('发布成功');
    // 重新拉取个人信息（如后端统计依赖 me）
  try { await auth.fetchMe?.(); } catch(_) {}
  // 主动触发统计接口一次（若后端统计为异步更新，至少拉取最新值）
  try { await getMyStats(); } catch(_) {}
    // 跳转到我的页面，促使统计与列表刷新
    router.push('/profile');
  } catch(e){
    ElMessage.error(e.message);
  } finally {
    loading.value = false;
  }
}

function reset(){
  form.title = '';
  form.description = '';
  form.deadline = '';
  form.budgetMin = null;
  form.budgetMax = null;
  form.tags = '';
}
</script>
<style scoped>
.publish-wrap { width:100%; margin: 12px auto; padding: 0 12px; }
.form-grid { display:grid; grid-template-columns: 1fr 1fr; gap: 16px; max-width: 1520px; }
.form-section { background: #fff; border-radius: 12px; padding: 8px 8px 2px; }
.section-title { font-weight: 700; font-size: 14px; color: #1f2937; margin: 8px 6px 6px; }
.inline-range { display:flex; align-items:center; gap:8px; }
.sep { color:#9ca3af; }
.actions { display:flex; gap:10px; justify-content:flex-end; margin-top: 12px; }
.btn-cta { background-image: linear-gradient(90deg, #6366f1, #a78bfa, #f472b6); border:none; }

@media (max-width: 960px) {
  .form-grid { grid-template-columns: 1fr; }
}

/* 大屏优化：限制内部网格最大宽度，避免表单行太长影响可读性 */
@media (min-width: 1440px) {
  .form-grid { max-width: 1680px; }
}
@media (min-width: 1920px) {
  .form-grid { max-width: 1840px; }
}
</style>
