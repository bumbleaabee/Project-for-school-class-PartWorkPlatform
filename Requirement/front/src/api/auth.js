import http from './http';

export function loginApi(data){
  return http.post('/auth/login', data);
}
export function registerApi(data){
  return http.post('/auth/register', data);
}
// 获取验证码图片与ID
export function getCaptchaApi(){
  // 约定后端返回 { captchaId, imageBase64, expireAt }
  return http.get('/auth/captcha/image');
}
// 修改密码（需要登录）
export function changePassword(data){
  // data: { oldPassword, newPassword }
  return http.put('/auth/password', data);
}
export function meApi(){
  // 后端 /auth/me 返回完整用户；/auth/profile 仅返回头像 URL
  const path = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.VITE_AUTH_ME_PATH) || '/auth/me';
  return http.get(path);
}

export function getMyAvatar(){
  // 仅获取头像 URL 的端点
  return http.get('/auth/profile');
}
