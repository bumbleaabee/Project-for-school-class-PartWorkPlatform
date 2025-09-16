import http from './http';

// 更新用户资料（无鉴权版本，body 需携带 id）
export function updateProfile(data){
  return http.put('/users/profile', data);
}

// 更新技能（无鉴权版本，body 需携带 id）
export function updateSkills(data){
  return http.put('/users/skills', data);
}

// 获取指定用户基础信息（正式版：当前用户可用 auth.me）
export function getUser(id){
  return http.get(`/users/${id}`);
}

// 获取用户统计
export function getUserStats(id){
  return http.get(`/users/${id}/stats`);
}

// 某些后端实现提供基于 Token 的统计接口（无 ID）
export function getMyStats(){
  return http.get('/users/stats');
}

// 获取用户评价列表
export function getUserReviews(id, params){
  return http.get(`/users/${id}/reviews`, { params });
}

// 上传头像（需要登录；multipart 表单）
export function uploadAvatar(file){
  const fd = new FormData();
  fd.append('file', file);
  return http.put('/users/avatar', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
