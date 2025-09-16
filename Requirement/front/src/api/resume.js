import http from './http';

// 上传简历（PDF），multipart/form-data，字段名 file
export function uploadResume(file) {
  const fd = new FormData();
  fd.append('file', file);
  return http.post('/resume/upload', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}

// 获取用户简历信息（返回文件 URL 或路径），后端接口：GET /api/resume/{userId}
export async function getResume(userId) {
  const data = await http.get(`/resume/${userId}`);
  if (data == null) return null;
  // 若后端直接返回字符串（相对/绝对 URL）
  if (typeof data === 'string') {
    return data || null;
  }
  // 常见字段名：尽量兼容
  const keys = ['fileUrl','url','filePath','path','resumeUrl','resumePath','location'];
  for (const k of keys) {
    const v = data?.[k];
    if (typeof v === 'string' && v) return v;
    if (v && typeof v === 'object') {
      for (const kk of keys) {
        const v2 = v[kk];
        if (typeof v2 === 'string' && v2) return v2;
      }
    }
  }
  // 若 data.embed 或 data.data 内嵌
  const inner = (typeof data === 'object') ? (data.data || data.embed || data.value) : null;
  if (typeof inner === 'string' && inner) return inner;
  if (inner && typeof inner === 'object') {
    for (const k of keys) {
      const v = inner[k];
      if (typeof v === 'string' && v) return v;
    }
  }
  return null;
}
