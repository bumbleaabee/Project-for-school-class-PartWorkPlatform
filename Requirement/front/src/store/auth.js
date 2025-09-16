import { defineStore } from 'pinia';
import { loginApi, meApi, getMyAvatar } from '../api/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: safeReadUser(),
    loadingProfile: false
  }),
  actions: {
    async login(username, password){
      // http.js 已将 {code,message,data} 解包为 data，因此 resp 即为 {token: '...'}
      const resp = await loginApi({ username, password });
      if (resp?.token) {
        return await this.bootstrapWithToken(resp.token);
      }
      return false;
    },
    async bootstrapWithToken(token){
      if(!token) return false;
      this.token = token;
      try { localStorage.setItem('token', token); } catch(_) {}
      await this.fetchMe();
      return true;
    },
    async fetchMe(){
      if(!this.token) return;
      this.loadingProfile = true;
      try {
        const userResp = await meApi();
        if (userResp && typeof userResp === 'object') {
          const next = { ...userResp };
          // 先保留当前本地头像（例如刚上传的乐观 URL）
          next.avatarUrl = this.user?.avatarUrl ?? null;
          // 再尝试从 /auth/profile 获取头像
          try {
            const av = await getMyAvatar();
            const raw = av?.avatarUrl || av?.url;
            if (typeof raw === 'string' && raw.trim()) {
              next.avatarUrl = normalizeUrl(raw.trim());
            }
          } catch(_) {}
          this.user = { ...(this.user || {}), ...next };
          try { localStorage.setItem('user', JSON.stringify(this.user)); } catch(_) {}
        } else {
          // 兜底：某些后端可能在 /auth/me 未初始化 user 表时返回 null
          // 从 JWT 中解析 userId 与用户名（sub）以构造最小可用用户对象
          const info = decodeJwt(this.token);
          if (info && (info.userId || info.sub)) {
            const next = {
              id: info.userId || this.user?.id,
              username: info.sub || this.user?.username,
              nickname: this.user?.nickname || undefined,
              avatarUrl: this.user?.avatarUrl || null
            };
            try {
              const av = await getMyAvatar();
              const raw = av?.avatarUrl || av?.url;
              if (typeof raw === 'string' && raw.trim()) {
                next.avatarUrl = normalizeUrl(raw.trim());
              }
            } catch(_) {}
            this.user = { ...(this.user || {}), ...next };
            try { localStorage.setItem('user', JSON.stringify(this.user)); } catch(_) {}
          }
        }
      } catch(_) {
        // 请求失败时的兜底：从 JWT 解码基本信息
        const info = decodeJwt(this.token);
        if (info && (info.userId || info.sub)) {
          const next = {
            id: info.userId || this.user?.id,
            username: info.sub || this.user?.username,
            nickname: this.user?.nickname || undefined,
            avatarUrl: this.user?.avatarUrl || null
          };
          try {
            const av = await getMyAvatar();
            const raw = av?.avatarUrl || av?.url;
            if (typeof raw === 'string' && raw.trim()) {
              next.avatarUrl = normalizeUrl(raw.trim());
            }
          } catch(_) {}
          this.user = { ...(this.user || {}), ...next };
          try { localStorage.setItem('user', JSON.stringify(this.user)); } catch(_) {}
        }
      }
      finally { this.loadingProfile = false; }
    },
    logout(){
      this.token = '';
      this.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  }
});

function normalizeUrl(u){
  if(!u) return '';
  if(/^https?:\/\//i.test(u)) return u;
  return u.startsWith('/') ? u : `/${u}`;
}
function withTs(u){
  if(!u) return u;
  const sep = u.includes('?') ? '&' : '?';
  return `${u}${sep}t=${Date.now()}`;
}

function safeReadUser(){
  try {
    const raw = localStorage.getItem('user');
    if(!raw) return null;
    const obj = JSON.parse(raw);
    if (obj && typeof obj === 'object') return obj;
    return null;
  } catch(_) { return null; }
}

// 轻量 JWT 解码（不校验签名，仅解析 payload），用于兜底读取 userId/sub
function decodeJwt(token){
  try {
    const parts = String(token).split('.');
    if (parts.length !== 3) return null;
    const payload = JSON.parse(base64UrlDecode(parts[1]));
    return payload || null;
  } catch(_) { return null; }
}

function base64UrlDecode(str){
  // 浏览器环境：atob 处理标准 base64；需将 base64url 转换为 base64
  try {
    let s = str.replace(/-/g, '+').replace(/_/g, '/');
    const pad = s.length % 4;
    if (pad) s += '='.repeat(4 - pad);
    const json = atob(s);
    return json;
  } catch(_) { return '';
  }
}
