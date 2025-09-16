<template>
  <div class="login-stage light">
    <!-- 动态渐变背景 & 浮动粒子 -->
    <div class="bg-gradient"></div>
    <div class="blob b1"></div>
    <div class="blob b2"></div>
    <div class="blob b3"></div>
  <div class="blob b4"></div>
    <!-- 网格与噪点叠加，增强细节层次 -->
  <div class="bg-ribbon"></div>
  <!-- 校园兼职主题插图层（轻量线稿，不干扰交互） -->
    <div class="bg-illustrations"></div>
    <!-- 左侧插图（内联 SVG，避免 data-uri 渲染差异） -->
    <svg class="illu illu-left" viewBox="0 0 560 560" aria-hidden="true">
      <g transform="translate(60,60)">
        <rect x="0" y="0" width="80" height="96" rx="10" />
        <line x1="16" y1="22" x2="64" y2="22" />
        <line x1="16" y1="38" x2="64" y2="38" />
        <line x1="16" y1="54" x2="60" y2="54" />
      </g>
      <g transform="translate(210,210)">
        <circle cx="40" cy="40" r="40" />
        <circle cx="40" cy="40" r="28" />
      </g>
      <g transform="translate(80,380)">
        <path d="M0 26 a26 26 0 0 1 26-26 h78 a26 26 0 0 1 26 26 v26 a26 26 0 0 1 -26 26 h-38 l-22 18 v-18 h-16 a26 26 0 0 1 -26-26 z" />
      </g>
    </svg>
    <!-- 右侧插图（内联 SVG） -->
    <svg class="illu illu-right" viewBox="0 0 560 560" aria-hidden="true">
      <g transform="translate(420,60)">
        <circle cx="40" cy="40" r="36" />
        <line x1="40" y1="40" x2="40" y2="18" />
        <line x1="40" y1="40" x2="58" y2="40" />
        <path d="M22 14 l14 10" />
        <path d="M58 14 l-14 10" />
      </g>
      <g transform="translate(350,390)">
        <rect x="0" y="24" width="104" height="66" rx="10" />
        <path d="M26 24 v-10 a8 8 0 0 1 8-8 h36 a8 8 0 0 1 8 8 v10" />
        <line x1="0" y1="46" x2="104" y2="46" />
      </g>
      <g transform="translate(480,280)">
        <rect x="0" y="0" width="58" height="58" rx="10" />
        <path d="M12 32 l12 12 l24 -26" />
      </g>
    </svg>
    <div class="bg-grid"></div>
    <div class="bg-noise"></div>

    <div class="login-wrapper shift-up">
      <el-card class="login-card" :style="tiltStyle" @mousemove="onTilt" @mouseleave="resetTilt">
        <div class="brand">
          <div class="logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" aria-hidden="true">
              <path d="M12 2l3 6 6 .9-4.5 4.4 1 6.2L12 16l-5.5 3.5 1-6.2L3 8.9 9 8l3-6z" />
            </svg>
          </div>
          <div class="title-wrap">
            <div class="title title-gradient">校园兼职平台</div>
            <div class="subtitle">登录你的创作与协作空间</div>
          </div>
        </div>

        <el-tabs v-model="active" class="fancy-tabs">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" @submit.prevent="onLogin" :disabled="loading" class="form-col">
              <el-form-item>
                <el-input v-model="loginForm.username" placeholder="用户名" clearable />
              </el-form-item>
              <el-form-item>
                <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password />
              </el-form-item>
              <el-form-item>
                <div style="display:flex; gap:10px; width:100%; align-items:center;">
                  <el-input v-model="loginForm.captchaCode" placeholder="验证码" maxlength="6" style="flex:1;" />
                  <img :src="captchaImg" @click="refreshCaptcha" :alt="captchaAlt" style="height:38px; border-radius:8px; cursor:pointer; user-select:none;" />
                </div>
              </el-form-item>
              <div class="form-row minor">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <a class="link" @click.prevent>忘记密码?</a>
              </div>
              <el-button type="primary" class="btn-cta" native-type="submit" :loading="loading">
                <span>登录</span>
              </el-button>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="注册" name="register">
            <el-form :model="regForm" @submit.prevent="onRegister" :disabled="loading" class="form-col">
              <el-form-item>
                <el-input v-model="regForm.username" placeholder="用户名" clearable />
              </el-form-item>
              <el-form-item>
                <el-input v-model="regForm.password" type="password" placeholder="密码" show-password />
              </el-form-item>
              <el-form-item>
                <el-input v-model="regForm.confirm" type="password" placeholder="确认密码" show-password />
              </el-form-item>
              <el-form-item>
                <el-input v-model="regForm.email" placeholder="邮箱（可选）" clearable />
              </el-form-item>
              <el-form-item>
                <div style="display:flex; gap:10px; width:100%; align-items:center;">
                  <el-input v-model="regForm.captchaCode" placeholder="验证码" maxlength="6" style="flex:1;" />
                  <img :src="captchaImg" @click="refreshCaptcha" :alt="captchaAlt" style="height:38px; border-radius:8px; cursor:pointer; user-select:none;" />
                </div>
              </el-form-item>
              <el-button type="primary" class="btn-cta" native-type="submit" :loading="loading">
                <span>注册</span>
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="socials">
          <span class="hint">更多登录方式（敬请期待）</span>
          <div class="icons">
            <i class="i wx"></i>
            <i class="i qq"></i>
            <i class="i github"></i>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref, watch, onMounted } from 'vue';
import { useAuthStore } from '../store/auth';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { registerApi, getCaptchaApi, loginApi } from '../api/auth';

const active = ref('login');
const loginForm = reactive({ username:'', password:'', captchaCode:'' });
const regForm = reactive({ username:'', password:'', confirm:'', email:'', captchaCode:'' });
const loading = ref(false);
const rememberMe = ref(true);
const auth = useAuthStore();
const router = useRouter();

// 验证码状态
const captchaId = ref('');
const captchaImg = ref('');
const captchaAlt = '点击刷新验证码';

async function loadCaptcha(){
  try {
    const resp = await getCaptchaApi();
    // 约定返回 { captchaId, imageBase64 }
    captchaId.value = resp?.captchaId || resp?.id || '';
    const img = resp?.imageBase64 || resp?.image || '';
    captchaImg.value = img ? (img.startsWith('data:') ? img : `data:image/png;base64,${img}`) : '';
  } catch(e){
    // 静默失败
  }
}
function refreshCaptcha(){ loadCaptcha(); }
onMounted(loadCaptcha);
watch(active, () => { loadCaptcha(); });

// 3D 轻微倾斜动效（不影响可访问性）
const tiltStyle = ref({ transform: 'perspective(800px) rotateX(0deg) rotateY(0deg)' });
function onTilt(e){
  const card = e.currentTarget;
  const r = card.getBoundingClientRect();
  const px = (e.clientX - r.left) / r.width; // 0~1
  const py = (e.clientY - r.top) / r.height; // 0~1
  const rx = (py - 0.5) * -8; // 最大 8 度
  const ry = (px - 0.5) * 8;
  tiltStyle.value = { transform: `perspective(800px) rotateX(${rx.toFixed(2)}deg) rotateY(${ry.toFixed(2)}deg)` };
}
function resetTilt(){
  tiltStyle.value = { transform: 'perspective(800px) rotateX(0deg) rotateY(0deg)', transition:'transform .35s ease' };
  setTimeout(()=>{ tiltStyle.value = { transform: 'perspective(800px) rotateX(0deg) rotateY(0deg)' }; }, 350);
}

async function onLogin(){
  if(!loginForm.username || !loginForm.password){ ElMessage.error('请输入用户名和密码'); return; }
  if(!loginForm.captchaCode){ ElMessage.error('请输入验证码'); return; }
  loading.value = true;
  try {
    // 优先尝试直接调用 loginApi 以便携带验证码，若后端返回 token 再走 bootstrap
    const resp = await loginApi({
      username: loginForm.username,
      password: loginForm.password,
      captchaId: captchaId.value,
      captchaCode: loginForm.captchaCode
    });
    let ok = false;
    const token = resp?.token || resp?.accessToken || resp?.jwt;
    if (token) {
      ok = await auth.bootstrapWithToken(token);
    } else {
      // 若后端仍使用原 store.login 逻辑，这里降级
      ok = await auth.login(loginForm.username, loginForm.password);
    }
    if(ok){
      ElMessage.success('登录成功');
      router.replace('/');
    } else {
      ElMessage.error('登录失败');
    }
  } catch(e){
    ElMessage.error(e.message || '请求失败');
    refreshCaptcha();
  } finally { loading.value = false; }
}

async function onRegister(){
  if(!regForm.username || !regForm.password){ ElMessage.error('请输入用户名和密码'); return; }
  if(regForm.password !== regForm.confirm){ ElMessage.error('两次输入的密码不一致'); return; }
  if(!regForm.captchaCode){ ElMessage.error('请输入验证码'); return; }
  loading.value = true;
  try {
    const regResp = await registerApi({
      username: regForm.username,
      password: regForm.password,
      email: regForm.email || undefined,
      captchaId: captchaId.value,
      captchaCode: regForm.captchaCode
    });
    // 优先使用后端直接返回的 token
    const token = regResp?.token || regResp?.accessToken || regResp?.jwt;
    let ok = false;
    if (token) {
      ok = await auth.bootstrapWithToken(token);
    } else {
      // 若未返回 token，则用注册凭据直接登录
      ok = await auth.login(regForm.username, regForm.password);
    }
    if (ok) {
      ElMessage.success('注册并已自动登录');
      router.replace('/');
    } else {
      // 回退到手动登录
      ElMessage.success('注册成功，请使用新账号登录');
      active.value = 'login';
      loginForm.username = regForm.username;
      loginForm.password = '';
    }
  } catch(e){
    ElMessage.error(e.message || '注册失败');
    refreshCaptcha();
  } finally { loading.value = false; }
}
</script>
<style scoped>
/* 舞台背景（默认深色） */
.login-stage { position: relative; min-height: calc(100vh - 60px); overflow: hidden; background: radial-gradient(1200px 600px at 10% 10%, #1d1b3a, transparent), radial-gradient(1000px 500px at 90% 20%, #102a43, transparent), linear-gradient(120deg, #0f172a 0%, #111827 100%); }
.bg-gradient { position: absolute; inset: -20%; background: linear-gradient(135deg, rgba(99,102,241,.25), rgba(236,72,153,.25), rgba(20,184,166,.25)); filter: blur(60px); animation: bg-move 18s ease-in-out infinite; opacity: .7; }
@keyframes bg-move { 0%{ transform: translateY(-10%) translateX(-5%) rotate(0deg); } 50%{ transform: translateY(6%) translateX(4%) rotate(180deg); } 100%{ transform: translateY(-10%) translateX(-5%) rotate(360deg); } }
/* 叠加的网格与噪点，提升质感 */
.bg-grid { position:absolute; inset:0; background-image: linear-gradient(rgba(255,255,255,.05) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.05) 1px, transparent 1px); background-size: 30px 30px; pointer-events:none; mix-blend-mode: overlay; opacity:.5; }
.bg-noise { position:absolute; inset:-50%; background-image: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 preserveAspectRatio=%22none%22 viewBox=%220 0 100 100%22><filter id=%22n%22><feTurbulence type=%22fractalNoise%22 baseFrequency=%220.9%22 numOctaves=%222%22 stitchTiles=%22stitch%22/></filter><rect width=%22100%25%22 height=%22100%25%22 filter=%22url(%23n)%22 opacity=%220.04%22/></svg>'); opacity:.35; pointer-events:none; mix-blend-mode: soft-light; }

/* 漂浮色块 */
.blob { position:absolute; width:320px; height:320px; border-radius:50%; filter: blur(30px); opacity:.35; }
.blob.b1 { background: #60a5fa; top: -60px; left: -60px; animation: float 16s ease-in-out infinite; }
.blob.b2 { background: #f472b6; bottom: -80px; right: -100px; animation: float 20s ease-in-out infinite reverse; }
.blob.b3 { background: #34d399; top: 40%; right: 20%; animation: float 22s ease-in-out infinite; }
@keyframes float { 0%,100%{ transform: translateY(-10px) translateX(0); } 50%{ transform: translateY(10px) translateX(10px); } }

/* 布局 */
.login-wrapper { position:relative; z-index:1; display:flex; justify-content:center; align-items:center; min-height: calc(100vh - 60px); padding: 24px; background: transparent !important; }
/* 上移卡片，留出更大下沉背景空间 */
.shift-up { align-items: flex-start; padding-top: 8vh; padding-bottom: 6vh; }

/* 玻璃拟态卡片 */
.login-card { width: 420px; background: rgba(17,25,40,.78); border: 1px solid rgba(255,255,255,.12); box-shadow: 0 10px 30px rgba(0,0,0,.45), inset 0 1px 0 rgba(255,255,255,.06); backdrop-filter: blur(14px) saturate(120%); border-radius: 18px; transition: box-shadow .3s ease, transform .2s ease; transform-style: preserve-3d; }
.login-card:hover { box-shadow: 0 18px 50px rgba(0,0,0,.45), 0 0 0 1px rgba(255,255,255,.06) inset; }

/* 品牌区 */
.brand { display:flex; align-items:center; gap: 12px; margin-bottom: 8px; background: rgba(0,0,0,.32); padding: 10px 12px; border-radius: 14px; }
.logo { width: 40px; height: 40px; display:grid; place-items:center; color: #fff; background: conic-gradient(from 210deg at 50% 50%, #60a5fa, #a78bfa, #f472b6, #34d399, #60a5fa); border-radius: 12px; box-shadow: 0 6px 18px rgba(99,102,241,.35); animation: logo-hue 8s linear infinite; }
.logo svg { width: 20px; height: 20px; filter: drop-shadow(0 2px 6px rgba(255,255,255,.3)); }
.title-gradient { position: relative; color: #ffffff; text-shadow: 0 2px 4px rgba(0,0,0,.55); -webkit-text-stroke: .4px rgba(0,0,0,.35); }
.title-gradient::after { content:""; position:absolute; inset:0; background: linear-gradient(90deg, #93c5fd, #a78bfa, #f0abfc, #fda4af, #6ee7b7); mix-blend-mode: overlay; opacity:.35; pointer-events:none; border-radius: 6px; }
@keyframes logo-hue { 0%{ filter: hue-rotate(0deg); } 100%{ filter: hue-rotate(360deg); } }
.title-wrap { display:flex; flex-direction:column; }
.title { font-weight: 800; font-size: 20px; color: #ffffff; letter-spacing: .6px; text-shadow: 0 2px 4px rgba(0,0,0,.55); }
/* 提高副标题对比度与可读性 */
.subtitle { font-size: 12px; color: rgba(255,255,255,.9); text-shadow: 0 1px 2px rgba(0,0,0,.3); }

/* Tabs 美化 */
:deep(.fancy-tabs .el-tabs__nav-wrap)::after { background: transparent; }
:deep(.fancy-tabs .el-tabs__nav-wrap) { background: rgba(0,0,0,.35); border-radius: 12px; padding: 4px 6px 4px 10px; backdrop-filter: blur(6px); box-shadow: 0 1px 0 rgba(255,255,255,.06) inset; }
:deep(.fancy-tabs .el-tabs__active-bar) { height: 3px; background: linear-gradient(90deg, #60a5fa, #a78bfa, #f472b6); border-radius: 2px; }
:deep(.fancy-tabs .el-tabs__item) { color: #ffffff; font-weight: 800; letter-spacing: .3px; text-shadow: 0 2px 3px rgba(0,0,0,.55); padding: 0 18px; }
:deep(.fancy-tabs .el-tabs__item.is-active) { color: #ffffff; }

/* 表单 */
.form-col { display:flex; flex-direction:column; gap: 12px; margin-top: 10px; }
.form-row.minor { display:flex; justify-content:space-between; align-items:center; margin-top: -4px; }
.link { color: rgba(255,255,255,.92); font-size: 12px; cursor: pointer; text-decoration: underline dashed rgba(255,255,255,.5); text-underline-offset: 3px; text-shadow: 0 1px 1px rgba(0,0,0,.25); }

/* 输入框：统一深色背景（避免输入时变白） */
/* 基础外观 */
:deep(.el-input) { --el-input-bg-color: transparent; }
:deep(.el-input__wrapper) {
  background: rgba(0,0,0,.42) !important;
  box-shadow: 0 0 0 1px rgba(255,255,255,.16) inset, 0 2px 8px rgba(0,0,0,.28);
  border-radius: 12px;
}
/* 悬停/聚焦状态仍保持深色 */
:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-hovering),
:deep(.el-input.is-focus .el-input__wrapper),
:deep(.el-input__wrapper.is-focus) {
  background: rgba(0,0,0,.52) !important;
  box-shadow: 0 0 0 1px rgba(147,197,253,.45) inset, 0 2px 10px rgba(0,0,0,.34);
}
/* 文本与占位符颜色 */
:deep(.el-input__inner) { color: #ffffff; caret-color: #ffffff; }
:deep(.el-input__inner::placeholder) { color: rgba(255,255,255,.7); }
/* 前后缀/密码显隐图标颜色 */
:deep(.el-input__prefix),
:deep(.el-input__suffix),
:deep(.el-input__suffix-inner),
:deep(.el-icon) { color: rgba(255,255,255,.85); }
/* 处理浏览器自动填充导致的背景变色 */
:deep(.el-input__inner:-webkit-autofill) {
  -webkit-text-fill-color: #ffffff;
  caret-color: #ffffff;
  box-shadow: 0 0 0px 1000px rgba(0,0,0,.42) inset !important;
  transition: background-color 9999s ease-out 0s;
}

/* CTA 按钮炫光效果 */
.btn-cta { width: 100%; --shine: linear-gradient(120deg, transparent, rgba(255,255,255,.65), transparent); position: relative; overflow: hidden; background-image: linear-gradient(90deg, #6366f1, #a78bfa, #f472b6); border: none; }
.btn-cta span { position: relative; z-index: 1; }
.btn-cta::after { content: ""; position:absolute; inset:-40%; width: 50%; transform: skewX(-20deg) translateX(-120%); background-image: var(--shine); filter: blur(8px); }
.btn-cta:hover::after { animation: shine 1s ease; }
@keyframes shine { 100% { transform: skewX(-20deg) translateX(220%); } }

/* Icon 占位（后续可换为真实图标） */
.socials { margin-top: 12px; display:flex; flex-direction:column; align-items:center; gap: 8px; }
.socials .hint { color: rgba(255,255,255,.88); font-size: 12px; text-shadow: 0 1px 1px rgba(0,0,0,.25); }
.icons { display:flex; gap: 10px; }
.i { width: 28px; height: 28px; border-radius: 6px; opacity:.9; filter: saturate(130%); box-shadow: 0 4px 12px rgba(0,0,0,.25) inset, 0 2px 8px rgba(0,0,0,.25); transition: transform .2s ease, box-shadow .2s ease; }
.i:hover { transform: translateY(-2px) scale(1.04); box-shadow: 0 6px 18px rgba(0,0,0,.35) inset, 0 4px 12px rgba(0,0,0,.35); }
.i.wx { background: radial-gradient(circle at 30% 30%, #34d399, #059669, #16a34a); }
.i.qq { background: radial-gradient(circle at 50% 40%, #60a5fa, #2563eb, #1d4ed8); }
.i.github { background: radial-gradient(circle at 50% 60%, #cbd5e1, #94a3b8, #475569); }

/* 进入动画 */
.login-card { animation: pop-in .5s ease both; }
@keyframes pop-in { from { transform: translateY(14px) scale(.98); opacity: .0; } to { transform: translateY(0) scale(1); opacity: 1; } }

/* 内联插图：放在两侧，清晰可见但不喧宾夺主 */
.illu { position:absolute; width: 560px; height: 560px; stroke: #6b7280; stroke-width: 2; fill: none; opacity: .28; pointer-events: none; z-index: 1; }
.illu-left { left: 6%; top: 50%; transform: translateY(-50%); }
.illu-right { right: 6%; top: 54%; transform: translateY(-50%); }

@media (max-width: 992px) {
  .illu { width: 420px; height: 420px; opacity: .24; }
  .illu-left { left: 4%; }
  .illu-right { display: none; }
}

/* 浅色场景（让白色区域更丰富，而不喧宾夺主） */
.login-stage.light { background:
  radial-gradient(1200px 700px at 12% -5%, rgba(147,197,253,.55), transparent 60%),
  radial-gradient(900px 520px at 88% 8%, rgba(255,182,193,.45), transparent 62%),
  radial-gradient(800px 520px at 10% 85%, rgba(167,139,250,.35), transparent 62%),
  linear-gradient(180deg, #f3f8ff, #f6f9ff 40%, #f2f5fb 100%);
}
.login-stage.light .bg-gradient { background: linear-gradient(135deg, rgba(96,165,250,.28), rgba(167,139,250,.24), rgba(244,114,182,.22)); filter: blur(80px); opacity: .95; }
.login-stage.light .bg-grid { background-image: linear-gradient(rgba(0,0,0,.06) 1px, transparent 1px), linear-gradient(90deg, rgba(0,0,0,.06) 1px, transparent 1px); opacity:.22; }
.login-stage.light .bg-noise { opacity:.18; mix-blend-mode: multiply; }
.login-stage.light .blob { filter: blur(52px); opacity:.34; }
.login-stage.light .blob.b1 { background:#93c5fd; top: -80px; left:-120px; }
.login-stage.light .blob.b2 { background:#fda4af; bottom: -130px; right:-140px; }
.login-stage.light .blob.b3 { background:#a7f3d0; top: 48%; right: 16%; }
.login-stage.light .blob.b4 { background:#c7d2fe; bottom: -160px; left: -120px; width: 360px; height: 360px; animation: float 24s ease-in-out infinite reverse; }

/* 飘带装饰（浅色） */
.bg-ribbon { position:absolute; top: 64px; left: -20%; width: 140%; height: 180px; background: linear-gradient(90deg, rgba(147,197,253,.22), rgba(167,139,250,.18), rgba(244,114,182,.16)); filter: blur(22px); transform: rotate(-2deg); opacity:.8; pointer-events:none; }

/* 适配浅色背景下卡片对比：保持卡片深色不变，增强聚焦 */
.login-stage.light .login-card { box-shadow: 0 20px 40px rgba(15,23,42,.18), inset 0 1px 0 rgba(255,255,255,.06); }

/* 校园兼职主题插图层 */
.bg-illustrations { position:absolute; inset:0; pointer-events:none; opacity:.7; z-index:0; }
.bg-illustrations::before { content:""; position:absolute; inset:0; background: url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22560%22 height=%22560%22 viewBox=%220 0 560 560%22 fill=%22none%22 stroke=%22%236b7280%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22 opacity=%220.28%22><g transform=%22translate(60,60)%22><rect x=%220%22 y=%220%22 width=%2280%22 height=%2296%22 rx=%2210%22/><line x1=%2216%22 y1=%2222%22 x2=%2264%22 y2=%2222%22/><line x1=%2216%22 y1=%2238%22 x2=%2264%22 y2=%2238%22/><line x1=%2216%22 y1=%2254%22 x2=%2260%22 y2=%2254%22/></g><g transform=%22translate(420,60)%22><circle cx=%2240%22 cy=%2240%22 r=%2236%22/><line x1=%2240%22 y1=%2240%22 x2=%2240%22 y2=%2218%22/><line x1=%2240%22 y1=%2240%22 x2=%2258%22 y2=%2240%22/><path d=%22M22 14 l14 10%22/><path d=%22M58 14 l-14 10%22/></g><g transform=%22translate(350,390)%22><rect x=%220%22 y=%2224%22 width=%22104%22 height=%2266%22 rx=%2210%22/><path d=%22M26 24 v-10 a8 8 0 0 1 8-8 h36 a8 8 0 0 1 8 8 v10%22/><line x1=%220%22 y1=%2246%22 x2=%22104%22 y2=%2246%22/></g><g transform=%22translate(80,380)%22><path d=%22M0 26 a26 26 0 0 1 26-26 h78 a26 26 0 0 1 26 26 v26 a26 26 0 0 1 -26 26 h-38 l-22 18 v-18 h-16 a26 26 0 0 1 -26-26 z%22/></g><g transform=%22translate(210,210)%22><circle cx=%2240%22 cy=%2240%22 r=%2440%22/><circle cx=%2240%22 cy=%2240%22 r=%2228%22/></g><g transform=%22translate(480,280)%22><rect x=%220%22 y=%220%22 width=%2258%22 height=%2258%22 rx=%2210%22/><path d=%22M12 32 l12 12 l24 -26%22/></g></svg>') left 5% center / 560px 560px no-repeat, url('data:image/svg+xml;utf8,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%22560%22 height=%22560%22 viewBox=%220 0 560 560%22 fill=%22none%22 stroke=%22%236b7280%22 stroke-width=%222%22 stroke-linecap=%22round%22 stroke-linejoin=%22round%22 opacity=%220.24%22><g transform=%22translate(40,80)%22><rect x=%220%22 y=%220%22 width=%2280%22 height=%2296%22 rx=%2210%22/><line x1=%2216%22 y1=%2222%22 x2=%2264%22 y2=%2222%22/><line x1=%2216%22 y1=%2238%22 x2=%2264%22 y2=%2238%22/><line x1=%2216%22 y1=%2254%22 x2=%2260%22 y2=%2254%22/></g><g transform=%22translate(420,40)%22><circle cx=%2240%22 cy=%2240%22 r=%2236%22/><line x1=%2240%22 y1=%2240%22 x2=%2240%22 y2=%2218%22/><line x1=%2240%22 y1=%2240%22 x2=%2258%22 y2=%2240%22/><path d=%22M22 14 l14 10%22/><path d=%22M58 14 l-14 10%22/></g><g transform=%22translate(320,400)%22><rect x=%220%22 y=%2224%22 width=%22104%22 height=%2266%22 rx=%2210%22/><path d=%22M26 24 v-10 a8 8 0 0 1 8-8 h36 a8 8 0 0 1 8 8 v10%22/><line x1=%220%22 y1=%2246%22 x2=%22104%22 y2=%2246%22/></g><g transform=%22translate(60,380)%22><path d=%22M0 26 a26 26 0 0 1 26-26 h78 a26 26 0 0 1 26 26 v26 a26 26 0 0 1 -26 26 h-38 l-22 18 v-18 h-16 a26 26 0 0 1 -26-26 z%22/></g><g transform=%22translate(190,200)%22><circle cx=%2240%22 cy=%2240%22 r=%2440%22/><circle cx=%2240%22 cy=%2240%22 r=%2228%22/></g><g transform=%22translate(460,300)%22><rect x=%220%22 y=%220%22 width=%2258%22 height=%2258%22 rx=%2210%22/><path d=%22M12 32 l12 12 l24 -26%22/></g></svg>') right 5% center / 560px 560px no-repeat; }
.bg-illustrations::after { content:""; position:absolute; inset:0; background:
  radial-gradient(420px 420px at 12% 50%, rgba(147,197,253,.28), transparent 60%),
  radial-gradient(380px 380px at 88% 56%, rgba(167,139,250,.22), transparent 60%);
  opacity:.55; }
/* 小屏减少插图密度，避免拥挤 */
@media (max-width: 768px) {
  .bg-illustrations { opacity:.46; }
  .bg-illustrations::before { background-size: 340px 340px, 0 0; background-position: left 4% center, right -100% center; }
  .bg-illustrations::after { background: radial-gradient(320px 320px at 10% 54%, rgba(147,197,253,.22), transparent 60%); }
}
</style>
