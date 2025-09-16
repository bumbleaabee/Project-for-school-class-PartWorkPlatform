import http from './http';

// 提交评价（临时允许前端传 reviewerId）
export function createReview(data){
  return http.post('/reviews', data);
}

// 获取某用户收到的评价
export function listUserReviews(userId, params){
  return http.get(`/users/${userId}/reviews`, { params });
}
