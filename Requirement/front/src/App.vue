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
        </el-menu>
        <div style="display:flex;align-items:center;gap:12px;flex-shrink:0;">
          <template v-if="auth.token">
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
  </el-container>
</template>
<script setup>
import { useRoute, useRouter } from 'vue-router';
import { computed, onMounted, watch } from 'vue';
import { useAuthStore } from './store/auth';
const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const user = computed(()=> auth.user);
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
</script>
<style>
@import url('./styles/theme.css');
.main--login { background: transparent !important; padding: 0 !important; }
</style>
