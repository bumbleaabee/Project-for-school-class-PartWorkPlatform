<template>
  <div class="profile-wrap">
    <!-- 左列：信息展示 -->
    <div class="col">
      <el-card shadow="never" header="我的信息" :body-style="{padding:'16px'}">
  <div v-if="auth.user">
          <div style="display:flex; align-items:center; gap:16px; margin-bottom:12px;">
            <el-avatar :size="64" :src="auth.user.avatarUrl">{{ avatarInitial }}</el-avatar>
            <div style="font-weight:600; font-size:16px;">{{ auth.user.nickname || auth.user.username || '未命名' }}</div>
          </div>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="ID">{{ auth.user.id }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ auth.user.nickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ auth.user.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机">{{ auth.user.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="学校">{{ auth.user.school || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ auth.user.grade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="技能">{{ auth.user.skills || '-' }}</el-descriptions-item>
            <el-descriptions-item label="信誉分">{{ auth.user.creditScore ?? '-' }}</el-descriptions-item>
          </el-descriptions>
  </div>
  <div v-else style="color:#999;font-size:13px;padding:4px 0;">未获取到用户信息</div>
        <template #footer>
          <div style="display:flex;gap:8px;">
            <el-button size="small" @click="refreshAll" :loading="loadingAll">刷新</el-button>
          </div>
        </template>
      </el-card>

  <el-card style="margin-top:16px" shadow="never" header="统计" :body-style="{padding:'12px 16px'}">
        <div v-if="stats">
          <el-space wrap>
            <el-tag type="success">发布: {{ stats.publishedCount ?? '-' }}</el-tag>
            <el-tag type="info">完成: {{ stats.finishedCount ?? '-' }}</el-tag>
            <!-- 不再统计撤回/取消数量：按需求移除展示 -->
            <el-tag type="danger">评分: {{ stats.reviewScoreAvg ?? '-' }}</el-tag>
          </el-space>
        </div>
        <div v-else style="color:#999;">暂无统计</div>
      </el-card>

  <el-card style="margin-top:16px" shadow="never" header="最近收到的评价" :body-style="{padding:'8px 16px'}">
        <el-table :data="reviews" v-loading="loadingReviews" size="small" style="width:100%;" :empty-text="'暂无评价'">
          <el-table-column prop="id" label="#" width="60" />
          <el-table-column prop="rating" label="分" width="60" />
          <el-table-column prop="content" label="内容" />
          <el-table-column prop="createdAt" label="时间" width="170" />
        </el-table>
      </el-card>
    </div>

    <!-- 右列：编辑区 -->
    <div class="col">
      <el-card shadow="never" header="修改资料" :body-style="{padding:'16px'}">
        <el-form :model="form" label-width="70px" @submit.prevent>
          <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
          <el-form-item label="学校"><el-input v-model="form.school" /></el-form-item>
            <el-form-item label="年级"><el-input v-model="form.grade" /></el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loadingProfile" @click="submitProfile">保存</el-button>
            <el-button @click="resetProfile" :disabled="loadingProfile">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card style="margin-top:16px" shadow="never" header="头像设置" :body-style="{padding:'16px'}">
        <el-upload
          class="avatar-uploader"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="doAvatarUpload"
          accept="image/*"
        >
          <el-avatar :size="96" :src="auth.user?.avatarUrl" style="border:2px dashed var(--el-border-color);">
            {{ avatarInitial }}
          </el-avatar>
        </el-upload>
  <div style="color:#999; font-size:12px; margin-top:8px;">点击头像上传，建议尺寸 ≥ 200x200，大小 ≤ 10MB，格式 JPG/PNG</div>
      </el-card>

          <el-card style="margin-top:16px" shadow="never" header="简历（PDF）" :body-style="{padding:'16px'}">
            <div style="display:flex; align-items:center; gap:12px;">
              <el-upload
                :show-file-list="false"
                accept="application/pdf,.pdf"
                :before-upload="beforeResumeUpload"
                :http-request="doResumeUpload"
              >
                <el-button type="primary" :loading="loadingResume">上传/更新简历</el-button>
              </el-upload>
              <div v-if="resumeUrl" style="font-size:13px;color:#666;">
                <el-link type="primary" :href="resumeUrl" target="_blank">查看当前简历</el-link>
              </div>
              <div v-else style="font-size:13px;color:#999;">尚未上传简历</div>
            </div>
            <div style="color:#999; font-size:12px; margin-top:8px;">仅支持 PDF，大小 ≤ 10MB</div>
          </el-card>

      <el-card style="margin-top:16px" shadow="never" header="修改密码" :body-style="{padding:'16px'}">
            <el-form :model="pwdForm" label-width="90px" @submit.prevent>
              <el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
              <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
              <el-form-item label="确认新密码"><el-input v-model="pwdForm.confirm" type="password" show-password /></el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loadingPwd" @click="submitPassword">更新密码</el-button>
                <el-button @click="resetPassword" :disabled="loadingPwd">重置</el-button>
              </el-form-item>
            </el-form>
            <div style="color:#999; font-size:12px;">成功修改后建议重新登录以确保安全。</div>
          </el-card>

      <el-card style="margin-top:16px" shadow="never" header="技能维护" :body-style="{padding:'16px'}">
        <el-form :model="skillsForm" label-width="70px" @submit.prevent>
          <el-form-item label="技能"><el-input v-model="skillsForm.skillsJoin" placeholder="逗号分隔" /></el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loadingSkills" @click="submitSkills">更新</el-button>
            <el-button @click="resetSkills" :disabled="loadingSkills">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

    </div>
  </div>

</template>
<script setup>
import { reactive, ref, onMounted, watch, computed } from 'vue';
import { useRoute } from 'vue-router';
import { updateProfile, updateSkills, getUserStats, getUserReviews, getMyStats, uploadAvatar } from '../api/user';
import { uploadResume, getResume } from '../api/resume';
import { changePassword } from '../api/auth';
import { useAuthStore } from '../store/auth';
import { ElMessage } from 'element-plus';

const auth = useAuthStore();
const route = useRoute();

const form = reactive({ nickname: '', school: '', grade: '' });
const skillsForm = reactive({ skillsJoin: '' });
const stats = ref(null);
const reviews = ref([]);
const loadingAll = ref(false);
const loadingReviews = ref(false);
const loadingProfile = ref(false);
const loadingSkills = ref(false);
const loadingPwd = ref(false);
const loadingResume = ref(false);
const resumeUrl = ref('');
const avatarInitial = computed(()=>{
  const name = auth.user?.nickname || auth.user?.username || '';
  return name ? name.charAt(0).toUpperCase() : '?';
});

function resetProfile(){ form.nickname=''; form.school=''; form.grade=''; }
function resetSkills(){ skillsForm.skillsJoin=''; }
const pwdForm = reactive({ oldPassword:'', newPassword:'', confirm:'' });
function resetPassword(){ pwdForm.oldPassword=''; pwdForm.newPassword=''; pwdForm.confirm=''; }

async function submitProfile(){
  if(!form.nickname && !form.school && !form.grade){ return ElMessage.warning('没有可更新的字段'); }
  loadingProfile.value = true;
  try {
    const payload = {};
    if(form.nickname) payload.nickname = form.nickname;
    if(form.school) payload.school = form.school;
    if(form.grade) payload.grade = form.grade;
    await updateProfile(payload);
    ElMessage.success('资料已更新');
    await auth.fetchMe();
  } catch(e){
    ElMessage.error(e.message || '更新失败');
  } finally { loadingProfile.value = false; }
}

async function submitSkills(){
  if(!skillsForm.skillsJoin){ return ElMessage.warning('请输入技能'); }
  loadingSkills.value = true;
  try {
    const skillsString = skillsForm.skillsJoin.split(',').map(s=>s.trim()).filter(Boolean).join(',');
    await updateSkills({ skills: skillsString });
    ElMessage.success('技能已更新');
    await auth.fetchMe();
  } catch(e){
    ElMessage.error(e.message || '更新失败');
  } finally { loadingSkills.value = false; }
}

function beforeAvatarUpload(file){
  const isImage = /^image\/(png|jpeg|jpg)$/.test(file.type);
  const isLt10M = file.size / 1024 / 1024 < 10;
  if(!isImage){ ElMessage.error('仅支持 JPG/PNG'); return false; }
  if(!isLt10M){ ElMessage.error('图片大小不能超过 10MB'); return false; }
  return true;
}
async function doAvatarUpload({ file }){
  try {
    const resp = await uploadAvatar(file);
    // 后端可能返回 { url:"/uploads/avator/xxx.png" } 或 { avatarUrl:"..." } 或 data:null
    const rawUrl = resp?.avatarUrl || resp?.url || '';
    const finalUrl = rawUrl ? withTs(normalizeUrl(rawUrl)) : '';
  // 乐观更新本地展示，随后再拉取一次 /auth/profile（或配置的当前用户端点）以确保状态一致
    if (finalUrl && auth.user) {
      auth.user.avatarUrl = finalUrl;
    }
    ElMessage.success('头像已更新');
    // 后台刷新，确保与服务端一致（若返回为 data:null 时也能更新）
    await auth.fetchMe();
  } catch(e){
    ElMessage.error(e.message || '上传失败');
  }
}

function beforeResumeUpload(file){
  const isPdf = file.type === 'application/pdf' || /\.pdf$/i.test(file.name);
  const isLt10M = file.size / 1024 / 1024 < 10;
  if(!isPdf){ ElMessage.error('仅支持 PDF 文件'); return false; }
  if(!isLt10M){ ElMessage.error('文件大小不能超过 10MB'); return false; }
  return true;
}
async function doResumeUpload({ file }){
  loadingResume.value = true;
  try {
    const resp = await uploadResume(file);
    const raw = resp?.fileUrl || resp?.url || resp?.filePath || '';
    resumeUrl.value = normalizeUrl(raw);
    ElMessage.success('简历已上传');
  } catch(e) {
    ElMessage.error(e.message || '上传失败');
  } finally { loadingResume.value = false; }
}

async function submitPassword(){
  if(!pwdForm.oldPassword || !pwdForm.newPassword){ return ElMessage.warning('请输入旧密码和新密码'); }
  if(pwdForm.newPassword.length < 6){ return ElMessage.warning('新密码长度至少 6 位'); }
  if(pwdForm.newPassword !== pwdForm.confirm){ return ElMessage.warning('两次输入的新密码不一致'); }
  loadingPwd.value = true;
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword });
    ElMessage.success('密码已更新');
    resetPassword();
    // 可选：强制登出并跳转登录
    // auth.logout();
    // router.push({ name: 'login' });
  } catch(e){
    ElMessage.error(e.message || '更新失败');
  } finally { loadingPwd.value = false; }
}

function normalizeUrl(u){
  if(!u) return '';
  // 若返回为相对路径（例如 /uploads/avator/xxx.png），原样返回；若为绝对 http/https，直接使用
  if (/^https?:\/\//i.test(u)) return u;
  return u.startsWith('/') ? u : `/${u}`;
}
function withTs(u){
  if(!u) return u;
  const sep = u.includes('?') ? '&' : '?';
  return `${u}${sep}t=${Date.now()}`;
}

async function loadStats(){
  if(!auth.user) return; 
  try {
    let raw = await getUserStats(auth.user.id);
    if(!raw || typeof raw !== 'object'){
      // 回退到无ID接口
      raw = await getMyStats();
    }
    stats.value = normalizeStats(raw);
  } catch(_) {}
}
async function loadReviews(){
  if(!auth.user) return;
  loadingReviews.value = true;
  try { const data = await getUserReviews(auth.user.id, { pageNo:1, pageSize:5 }); reviews.value = data?.list||[]; }
  catch(_){}
  finally { loadingReviews.value=false; }
}
async function loadResume(){
  if(!auth.user) return; 
  try {
    const url = await getResume(auth.user.id);
    resumeUrl.value = normalizeUrl(url);
  } catch(_) {}
}
async function refreshAll(){
  loadingAll.value = true;
  try {
    await auth.fetchMe();
    await Promise.all([loadStats(), loadReviews()]);
  } finally { loadingAll.value=false; }
}

watch(()=>auth.user, (u)=>{ if(u){ loadStats(); loadReviews(); } });
onMounted(()=>{ if(auth.user){ loadStats(); loadReviews(); loadResume(); } else if(auth.token){ auth.fetchMe().then(()=>{ loadStats(); loadReviews(); loadResume(); }); } });
// 当路由进入 /profile 或发生变化时，主动刷新，确保发布任务后统计能即时更新
watch(() => route.fullPath, (p)=>{ if(route.path === '/profile'){ refreshAll(); loadResume(); } });

function normalizeStats(s){
  if(!s || typeof s !== 'object') return null;
  return {
    // 统一为前端显示字段
    publishedCount: s.publishedCount ?? s.tasksPublished ?? 0,
    finishedCount: s.finishedCount ?? s.tasksFinished ?? 0,
    reviewScoreAvg: s.reviewScoreAvg ?? s.avgRating ?? 0,
  };
}
</script>
<style scoped>
.profile-wrap {
  width: 100%;
  max-width: 1200px;
  margin: 12px auto;
  padding: 0 12px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.col { min-width: 320px; }

@media (max-width: 960px) {
  .profile-wrap { grid-template-columns: 1fr; }
}
</style>
