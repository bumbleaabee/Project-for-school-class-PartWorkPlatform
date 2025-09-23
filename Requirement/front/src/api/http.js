import axios from 'axios';
import { useAuthStore } from '../store/auth';

const instance = axios.create({ baseURL: '/api', timeout: 10000 });

// 请求拦截：附加 Token（若存在）
instance.interceptors.request.use(cfg => {
  // 优先从 Pinia 读取；失败则从 localStorage 兜底
  let token = '';
  try {
    const auth = useAuthStore();
    if (auth?.token) token = auth.token;
  } catch(_) { /* 运行早期可能 store 未就绪 */ }
  if (!token) {
    try { token = localStorage.getItem('token') || ''; } catch(_) {}
  }
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

// 响应统一处理：保持旧 payload.data 语义；401 触发登出
instance.interceptors.response.use(r => {
  const cfg = r.config || {};
  const headers = r.headers || {};
  const ct = String(headers['content-type'] || '').toLowerCase();
  // 对于文件下载（blob），直接返回二进制与响应头，避免统一响应类解析
  if (cfg.responseType === 'blob') {
    return { blob: r.data, headers };
  }
  const payload = r.data;
  // 统一处理：code 可为 number 或 string；非0均视为业务错误
  const hasCode = payload && payload.code !== undefined && payload.code !== null;
  const normalizedCode = hasCode ? Number(payload.code) : 0;
  const isBizError = hasCode && String(payload.code) !== '0' && normalizedCode !== 0;
  if (isBizError) {
    const msg = payload.message || payload.msg || '请求错误';
    const err = new Error(msg);
    err.code = Number.isNaN(normalizedCode) ? payload.code : normalizedCode; // 保留可比对的数值，否则回落到原值
    err.rawCode = payload.code;
    err.data = payload.data;
    err.payload = payload;
    return Promise.reject(err);
  }
  let data = payload?.data;
  // 兼容后端直接返回数组或对象的情况
  if (data === undefined) {
    // 顶层即为数组：直接视为列表
    if (Array.isArray(payload)) {
      const headerTotalRawA = headers['x-total'] || headers['x-total-count'] || headers['x-pagination-total'];
      const headerTotalA = headerTotalRawA != null ? Number(headerTotalRawA) : NaN;
      return { list: payload, total: Number.isNaN(headerTotalA) ? payload.length : headerTotalA };
    }
    // 顶层为对象：当作 data 使用
    if (payload && typeof payload === 'object') {
      data = payload;
    }
  }
  // 从响应头提取分页总数（常见的头部命名）
  const headerTotalRaw = headers['x-total'] || headers['x-total-count'] || headers['x-pagination-total'];
  const headerTotal = headerTotalRaw != null ? Number(headerTotalRaw) : NaN;
  // 若 data 为数组，则规范化为 { list, total }
  if (Array.isArray(data)) {
    return { list: data, total: Number.isNaN(headerTotal) ? data.length : headerTotal };
  }
  // 若 data 为对象且包含 list，但缺少 total，则补全 total
  if (data && typeof data === 'object') {
    if ('list' in data && (data.total === undefined || data.total === null)) {
      if (!Number.isNaN(headerTotal)) {
        data.total = headerTotal;
      } else if (Array.isArray(data.list)) {
        data.total = data.list.length;
      }
    }
    // 常见字段名适配：records/items/rows 等
    const listKey = ['list','records','items','rows','dataList'].find(k => Array.isArray(data[k]));
    if (listKey && listKey !== 'list') {
      const list = data[listKey];
      // 常见的总数字段
      const totalKeys = ['total','totalCount','count','totalElements','recordCount','rowCount'];
      let bodyTotal = totalKeys.map(k=>data[k]).find(v => v !== undefined && v !== null);
      let totalNum = !Number.isNaN(headerTotal) ? headerTotal : Number(bodyTotal);
      if (Number.isNaN(totalNum)) totalNum = Array.isArray(list) ? list.length : 0;
      return { list, total: totalNum };
    }
    // 如果顶层就有 total 而 data 是对象但没有 list，也尝试从 data.data 提取数组
    if (!('list' in data)) {
      const innerArrayKey = ['data','content','values'].find(k => Array.isArray(data[k]));
      if (innerArrayKey) {
        const list = data[innerArrayKey];
        let bodyTotal = data.total ?? data.totalCount ?? data.count ?? data.totalElements;
        let totalNum = !Number.isNaN(headerTotal) ? headerTotal : Number(bodyTotal);
        if (Number.isNaN(totalNum)) totalNum = Array.isArray(list) ? list.length : 0;
        return { list, total: totalNum };
      }
    }
  }
  return data;
}, err => {
  const res = err?.response;
  if (res) {
    const status = res.status;
    let payload = res.data;
    // 处理 blob 错误体（某些后端错误也以 blob+application/json 返回）
    const cfg = res.config || {};
    const ct = String(res.headers?.['content-type'] || '').toLowerCase();
    if (cfg.responseType === 'blob' && payload && typeof payload === 'object' && 'type' in payload) {
      // 尝试将 blob 转为 JSON，以提取 {code,message}
      if (ct.includes('application/json') || ct.includes('text/json')) {
        try {
          return payload.text().then(txt => {
            try { payload = JSON.parse(txt); } catch(_) { /* ignore */ }
            if (payload && payload.code !== undefined && payload.code !== null) {
              const normalizedCode = Number(payload.code);
              err.code = Number.isNaN(normalizedCode) ? payload.code : normalizedCode;
              err.rawCode = payload.code;
              err.payload = payload;
              const msg = payload.message || payload.msg;
              if (msg) err.message = msg;
            }
            if (status === 401) { try { useAuthStore().logout(); } catch(_) {} }
            return Promise.reject(err);
          });
        } catch(_) { /* ignore and fallthrough */ }
      }
    }
    // 将后端在错误响应体中的业务码与消息透传出来（支持 number/string）
    if (payload && payload.code !== undefined && payload.code !== null) {
      const normalizedCode = Number(payload.code);
      err.code = Number.isNaN(normalizedCode) ? payload.code : normalizedCode;
      err.rawCode = payload.code;
      err.payload = payload;
      const msg = payload.message || payload.msg;
      if (msg) err.message = msg;
    }
    if (status === 401) {
      try { useAuthStore().logout(); } catch(_) {}
    }
  }
  return Promise.reject(err);
});

export default instance;
