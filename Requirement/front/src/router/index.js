import { createRouter, createWebHistory } from 'vue-router';
import TaskList from '../views/TaskList.vue';
import MyPublished from '../views/MyPublished.vue';
import PublishTask from '../views/PublishTask.vue';
import Profile from '../views/Profile.vue';
import Review from '../views/Review.vue';
import Ongoing from '../views/Ongoing.vue';
const Login = () => import('../views/Login.vue');

import MyAccepted from '../views/MyAccepted.vue';
import AiRecommend from '../views/AiRecommend.vue';
const Admin = () => import('../views/Admin.vue');

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/', name: 'home', component: TaskList },
  { path: '/my', name: 'my-published', component: MyPublished },
  { path: '/publish', component: PublishTask },
  { path: '/profile', component: Profile },
  { path: '/review', component: Review },
  { path: '/ongoing', component: Ongoing },
  { path: '/accepted', name: 'my-accepted', component: MyAccepted },
  { path: '/ai/recommend', name: 'ai-recommend', component: AiRecommend }
  ,{ path: '/admin', name: 'admin', component: Admin }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

import { useAuthStore } from '../store/auth';

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  if (to.name === 'login') {
    if (auth.token) return next('/');
    return next();
  }
  if (!auth.token) return next({ name: 'login' });
  // 已登录但还没加载用户信息：先拉取，再进行权限判断
  const proceed = () => {
    if (to.name === 'admin' && !(auth.user?.admin)) {
      return next({ name: 'home' });
    }
    return next();
  };
  if (auth.token && !auth.user) {
    if (!auth.loadingProfile) {
      auth.fetchMe().then(proceed).catch(() => proceed());
      return; // 等待 fetchMe 决定去向
    }
  }
  proceed();
});

export default router;
