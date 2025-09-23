import http from './http';

// 智能任务推荐（AI大模型）：基于当前登录用户的 skills 自动匹配
export function getAiMatch(){
  return http.get('/ai/match');
}
