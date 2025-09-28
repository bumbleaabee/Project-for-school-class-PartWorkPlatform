<template>
  <el-container style="height:100vh">
    <el-header class="app-header" v-if="route.name !== 'login'">
      <div style="display:flex;align-items:center;justify-content:space-between;">
        <el-menu v-if="auth.token" mode="horizontal" :default-active="active" :ellipsis="false" @select="onSelect">
          <el-menu-item index="/">任务</el-menu-item>
          <el-menu-item index="/publish">发布任务</el-menu-item>
          <el-menu-item index="/my">我的发布</el-menu-item>
          <el-menu-item index="/ongoing">进行中</el-menu-item>
          <el-menu-item index="/accepted">我接的单</el-menu-item>
          <el-menu-item index="/review">评价</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin">管理</el-menu-item>
        </el-menu>
        <div style="display:flex;align-items:center;gap:12px;flex-shrink:0;">
          <template v-if="auth.token">
            <!-- 验收提醒：仅在登录后显示 -->
            <el-badge :value="finishCount" :hidden="finishCount===0" type="danger">
              <el-button circle size="small" @click="openFinishDrawer" :title="finishCount>0?`有${finishCount}条验收请求`:'暂无验收请求'">
                <span class="el-icon">🔔</span>
              </el-button>
            </el-badge>
            <el-avatar :size="32" :src="user?.avatarUrl" @click="goProfile" style="cursor:pointer;">
              {{ avatarInitial }}
            </el-avatar>
            <span style="font-size:13px;color:#666;">{{ userLabel }}</span>
            <el-button size="small" @click="doLogout">退出</el-button>
          </template>
        </div>
      </div>
    </el-header>
    <el-main :class="mainClass">
      <transition name="view-fade" mode="out-in">
        <router-view />
      </transition>
    </el-main>

    <!-- 验收请求抽屉 -->
    <el-drawer v-model="finishDrawer" title="买家验收请求" size="380px">
      <div v-if="finishList.length">
        <el-timeline>
          <el-timeline-item
            v-for="it in finishList"
            :key="`${it.taskId}-${it.userId}-${it.createdAt||''}`"
            :timestamp="formatTime(it.createdAt)"
          >
            <div style="display:flex;flex-direction:column;gap:6px;">
              <div>
                任务 <b>#{{ it.taskId }}</b>
                <span v-if="it.taskTitle"> - {{ it.taskTitle }}</span>
              </div>
              <div>买家：{{ it.nickname || ('用户'+it.userId) }}</div>
              <div style="display:flex;gap:8px;">
                <el-button size="small" type="success" @click="quickAccept(it.taskId)">一键验收</el-button>
                <el-button size="small" @click="goTaskManage(it.taskId)">去查看</el-button>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <el-empty v-else description="暂无验收请求" />
    </el-drawer>
  </el-container>
</template>
<script setup>
import { useRoute, useRouter } from 'vue-router';
import { computed, onMounted, watch, ref, onBeforeUnmount } from 'vue';
import { useAuthStore } from './store/auth';
import { listFinishRequests, acceptTask } from './api/task';
import { ElMessage } from 'element-plus';
const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const user = computed(()=> auth.user);
const isAdmin = computed(()=> !!auth.user?.admin);
const active = computed(()=>
  route.path.startsWith('/publish') ? '/publish'
  : route.path.startsWith('/my') ? '/my'
  : route.path.startsWith('/ongoing') ? '/ongoing'
  : route.path.startsWith('/accepted') ? '/accepted'
  : route.path
);
function onSelect(i){
  router.push(i);
}
const userLabel = computed(()=> user.value ? `欢迎：${user.value.nickname || user.value.username || user.value.id}` : '未登录');
const avatarInitial = computed(()=>{
  const name = user.value?.nickname || user.value?.username || '';
  return name ? name.charAt(0).toUpperCase() : '?';
});
function doLogout(){
  auth.logout();
  router.replace('/login');
}
function goProfile(){ router.push('/profile'); }

// 登录页时，让主内容区域透明并去除默认内边距，确保登录页自带背景可见
const mainClass = computed(()=> route.name === 'login' ? 'main--login' : '');

// 通过 /auth/profile（或配置的当前用户端点）刷新用户信息与头像
async function refreshAvatar(){
  try {
    if(!auth.token) return;
    await auth.fetchMe();
  } catch(_) { /* 静默 */ }
}

// 登录后或用户发生变化时，确保从当前用户端点拉取最新资料（包含 avatarUrl）
watch(() => auth.user?.id, (id)=>{ if(id){ refreshAvatar(); } });
onMounted(()=>{ if(auth.token){ refreshAvatar(); } });

// ============== 验收提醒逻辑 ==============
const finishDrawer = ref(false);
const finishList = ref([]);
const finishCount = ref(0);
let timer = null;

function openFinishDrawer(){
  finishDrawer.value = true;
}

function formatTime(v){
  if(!v) return '';
  const s = String(v).replace('T',' ').replace('Z','');
  return s.length>16 ? s.slice(0,16) : s;
}

async function fetchFinishRequests(){
  if(!auth.token) { finishList.value = []; finishCount.value = 0; return; }
  try{
    const data = await listFinishRequests();
    // http.js 会把 {data:{list:[]}} 归一化，若顶层是对象也返回对象
    // 兼容 data 为数组/对象/包装 {list}
    let list = [];
    if (Array.isArray(data)) list = data;
    else if (data && Array.isArray(data.list)) list = data.list;
    else if (data && typeof data === 'object') list = [data];
    // 去重：按 taskId+userId
    const seen = new Set();
    const uniq = [];
    for (const it of list) {
      const key = `${it.taskId}-${it.userId}`;
      if (seen.has(key)) continue;
      seen.add(key);
      uniq.push(it);
    }
    finishList.value = uniq;
    finishCount.value = uniq.length;
  }catch(e){
    // 静默失败，不打扰用户
    finishList.value = [];
    finishCount.value = 0;
  }
}

async function quickAccept(taskId){
  try{
    await acceptTask(taskId);
    ElMessage.success('已验收');
    // 验收后刷新一次提醒列表
    fetchFinishRequests();
  }catch(e){
    ElMessage.error(e.message || '验收失败');
  }
}

function goTaskManage(taskId){
  // 跳到我的发布页，并建议用户查看对应任务申请/详情
  router.push('/my');
  finishDrawer.value = false;
}

watch(() => auth.token, (t)=>{
  if (t) { fetchFinishRequests(); startTimer(); }
  else { stopTimer(); finishList.value=[]; finishCount.value=0; }
}, { immediate: true });

function startTimer(){
  stopTimer();
  timer = setInterval(fetchFinishRequests, 20_000); // 20s 拉一次
}
function stopTimer(){ if (timer) { clearInterval(timer); timer = null; } }
onBeforeUnmount(stopTimer);
</script>
<style>
@import url('./styles/theme.css');
.main--login { background: transparent !important; padding: 0 !important; }
</style>
