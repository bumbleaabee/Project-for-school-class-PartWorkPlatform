import { createRouter, createWebHistory } from 'vue-router';
import TaskList from '../views/TaskList.vue';
import MyPublished from '../views/MyPublished.vue';
import PublishTask from '../views/PublishTask.vue';
import Profile from '../views/Profile.vue';
import Review from '../views/Review.vue';
import Ongoing from '../views/Ongoing.vue';
const Login = () => import('../views/Login.vue');

import MyAccepted from '../views/MyAccepted.vue';

const routes = [
  { path: '/login', name: 'login', component: Login },
  { path: '/', name: 'home', component: TaskList },
  { path: '/my', name: 'my-published', component: MyPublished },
  { path: '/publish', component: PublishTask },
  { path: '/profile', component: Profile },
  { path: '/review', component: Review },
  { path: '/ongoing', component: Ongoing },
  { path: '/accepted', name: 'my-accepted', component: MyAccepted }
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
  // 已登录但还没加载用户信息
  if (auth.token && !auth.user && !auth.loadingProfile) auth.fetchMe();
  next();
});

export default router;
