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

    <!-- 右列：编辑区（合并为选项卡，避免过长） -->
    <div class="col right-col">
      <el-card class="settings-card" shadow="never" header="账户设置" :body-style="{padding:'12px 16px'}">
        <el-tabs type="border-card">
          <el-tab-pane>
            <template #label>
              <span class="tab-label"><span class="tab-icon">📝</span><span>资料</span></span>
            </template>
            <el-form :model="form" label-width="70px" @submit.prevent>
              <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
              <el-form-item label="学校"><el-input v-model="form.school" /></el-form-item>
              <el-form-item label="年级"><el-input v-model="form.grade" /></el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loadingProfile" @click="submitProfile">保存</el-button>
                <el-button @click="resetProfile" :disabled="loadingProfile">重置</el-button>
              </el-form-item>
            </el-form>
            <div class="hint">完善资料有助于平台更好地为你推荐任务。</div>
          </el-tab-pane>

          <el-tab-pane>
            <template #label>
              <span class="tab-label"><span class="tab-icon">🖼️</span><span>头像</span></span>
            </template>
            <div style="display:flex; align-items:center; gap:16px;">
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
              <div style="color:#999; font-size:12px;">建议 ≥ 200x200，≤ 10MB，JPG/PNG</div>
            </div>
            <div class="hint">清晰的头像有助于让合作方更快认识你。</div>
          </el-tab-pane>

          <el-tab-pane>
            <template #label>
              <span class="tab-label"><span class="tab-icon">📎</span><span>简历</span></span>
            </template>
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
            <div class="hint">建议突出技能关键词，利于任务匹配与筛选。</div>
          </el-tab-pane>

          <el-tab-pane>
            <template #label>
              <span class="tab-label"><span class="tab-icon">🔒</span><span>密码</span></span>
            </template>
            <el-form :model="pwdForm" label-width="90px" @submit.prevent>
              <el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
              <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
              <el-form-item label="确认新密码"><el-input v-model="pwdForm.confirm" type="password" show-password /></el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loadingPwd" @click="submitPassword">更新密码</el-button>
                <el-button @click="resetPassword" :disabled="loadingPwd">重置</el-button>
              </el-form-item>
            </el-form>
            <div style="color:#999; font-size:12px;">修改成功后建议重新登录。</div>
            <div class="hint">为账号安全，建议定期更新密码并开启强口令。</div>
          </el-tab-pane>

          <el-tab-pane>
            <template #label>
              <span class="tab-label"><span class="tab-icon">✨</span><span>技能</span></span>
            </template>
            <el-form :model="skillsForm" label-width="70px" @submit.prevent>
              <el-form-item label="技能"><el-input v-model="skillsForm.skillsJoin" placeholder="逗号分隔" /></el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="loadingSkills" @click="submitSkills">更新</el-button>
                <el-button @click="resetSkills" :disabled="loadingSkills">重置</el-button>
              </el-form-item>
            </el-form>
            <div class="hint">技能标签将用于“AI 推荐”与搜索筛选，提高曝光度。</div>
          </el-tab-pane>
        </el-tabs>
        <!-- 右侧底部装饰区域：渐变条 + 浮动光球 + 斜向扫光，仅视觉效果 -->
        <div class="settings-decor">
          <div class="gradient-bar"></div>
          <span class="orb o1" aria-hidden="true"></span>
          <span class="orb o2" aria-hidden="true"></span>
          <span class="orb o3" aria-hidden="true"></span>
          <span class="orb o4" aria-hidden="true"></span>
          <span class="shine-line" aria-hidden="true"></span>
        </div>
        <!-- 追加底部装饰层-2：细网格 + 波形 + 星光闪烁（仅视觉） -->
        <div class="settings-decor-2">
          <div class="grid-overlay"></div>
          <svg class="wave" viewBox="0 0 1200 120" preserveAspectRatio="none" aria-hidden="true">
            <defs>
              <linearGradient id="gradWave" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#7c3aed" stop-opacity="0.18"/>
                <stop offset="50%" stop-color="#06b6d4" stop-opacity="0.18"/>
                <stop offset="100%" stop-color="#22c55e" stop-opacity="0.18"/>
              </linearGradient>
            </defs>
            <path d="M0,30 C150,90 350,0 600,60 C850,120 1050,20 1200,50 L1200,120 L0,120 Z" fill="url(#gradWave)" />
          </svg>
          <span class="spark sp1" aria-hidden="true"></span>
          <span class="spark sp2" aria-hidden="true"></span>
          <span class="spark sp3" aria-hidden="true"></span>
        </div>
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
  try { const data = await getUserReviews(auth.user.id, { pageNo:1, pageSize:3 }); reviews.value = data?.list||[]; }
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
  max-width: 1100px;
  margin: 12px auto;
  padding: 0 12px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.col { min-width: 320px; display: flex; flex-direction: column; }
.right-col { }
.settings-card { flex: 1; display: flex; flex-direction: column; }
.settings-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; }
.settings-card :deep(.el-tabs) { flex: 1; display: flex; flex-direction: column; }
.settings-card :deep(.el-tabs__content) { flex: 1; display: flex; }
.settings-card :deep(.el-tab-pane) { flex: 1; display: flex; flex-direction: column; }

/* 右侧卡片的轻量渐变背景与边框虚化，丰富视觉层次 */
.settings-card {
  background:
    radial-gradient(1200px 300px at 100% -20%, rgba(124,58,237,0.08), transparent 60%),
    radial-gradient(900px 300px at -10% 120%, rgba(34,197,94,0.06), transparent 60%);
  border: 1px solid var(--el-border-color-lighter);
}

/* 标签带图标与对齐优化 */
.tab-label { display: inline-flex; align-items: center; gap: 6px; font-weight: 600; }
.tab-icon { filter: drop-shadow(0 1px 1px rgba(0,0,0,0.1)); }

/* 小提示文案，弱化色彩 */
.hint { margin-top: 8px; font-size: 12px; color: #8b8b8b; }

/* 右侧底部装饰：渐变条 + 浮动光球 + 扫光 */
.settings-decor {
  position: relative;
  margin-top: 12px;
  height: 56px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(90deg, rgba(124,58,237,0.10), rgba(6,182,212,0.10), rgba(34,197,94,0.10));
}
.settings-decor .gradient-bar {
  position: absolute; left: 0; right: 0; bottom: 0;
  height: 4px;
  background: linear-gradient(90deg, #7c3aed, #06b6d4, #22c55e);
  filter: blur(0.5px);
}
.orb { position: absolute; width: 14px; height: 14px; border-radius: 50%; opacity: .9; }
.o1 { left: 12%; top: 12px; background: radial-gradient(circle at 30% 30%, #7c3aed, rgba(124,58,237,0.3)); animation: floatY 4s ease-in-out infinite; }
.o2 { left: 38%; top: 22px; background: radial-gradient(circle at 30% 30%, #06b6d4, rgba(6,182,212,0.3)); animation: floatY 5s 0.2s ease-in-out infinite; }
.o3 { left: 64%; top: 10px; background: radial-gradient(circle at 30% 30%, #22c55e, rgba(34,197,94,0.3)); animation: floatY 4.5s 0.1s ease-in-out infinite; }
.o4 { left: 82%; top: 18px; background: radial-gradient(circle at 30% 30%, #f59e0b, rgba(245,158,11,0.3)); animation: floatY 5.2s 0.3s ease-in-out infinite; }

@keyframes floatY { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }

.shine-line {
  position: absolute;
  top: -120%; left: -30%;
  width: 40%; height: 300%; transform: rotate(25deg);
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,.18) 50%, rgba(255,255,255,0) 100%);
  animation: sweep 3.5s linear infinite;
}
@keyframes sweep { 0% { left: -40%; } 100% { left: 120%; } }

/* 追加底部装饰层-2 */
.settings-decor-2 {
  position: relative;
  margin-top: 10px;
  height: 72px;
  border-radius: 10px;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(255,255,255,0.65), rgba(255,255,255,0.85));
  border: 1px solid var(--el-border-color-lighter);
}
.settings-decor-2 .grid-overlay {
  position: absolute; inset: 0;
  background-image: linear-gradient(rgba(0,0,0,0.04) 1px, transparent 1px), linear-gradient(90deg, rgba(0,0,0,0.04) 1px, transparent 1px);
  background-size: 14px 14px;
  opacity: .8;
}
.settings-decor-2 .wave {
  position: absolute; bottom: 0; left: 0; right: 0;
  width: 100%; height: 44px;
}
.spark { position: absolute; width: 6px; height: 6px; border-radius: 50%; background: radial-gradient(circle, #fff, rgba(255,255,255,0)); box-shadow: 0 0 12px rgba(255,255,255,0.8); opacity: .85; animation: twinkle 2.2s infinite ease-in-out; }
.sp1 { left: 18%; top: 18px; animation-delay: .1s; }
.sp2 { left: 52%; top: 10px; animation-delay: .8s; }
.sp3 { left: 82%; top: 22px; animation-delay: 1.3s; }
@keyframes twinkle { 0%, 100% { transform: scale(0.8); opacity: .5; } 50% { transform: scale(1.1); opacity: 1; } }

@media (max-width: 960px) {
  .profile-wrap { grid-template-columns: 1fr; }
}
</style>
