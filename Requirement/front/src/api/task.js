// 买家申请完成任务
export function applyTaskFinish(id){
  return http.put(`/tasks/${id}/apply-finish`, null);
}
import http from './http';

export function listTasks(params){
  return http.get('/tasks', { params });
}
export function publishTask(data){
  return http.post('/tasks', data);
}
export function applyTask(id, data){
  return http.post(`/tasks/${id}/apply`, data);
}
export function withdrawTask(id, data){
  return http.put(`/tasks/${id}/withdraw`, data);
}

// 获取任务的申请列表（仅发布者可查看全部）
export function getTaskApplications(id, params){
  return http.get(`/tasks/${id}/application`, { params });
}

// 选择申请人为执行人
export function chooseTaskApplicant(id, applicationId){
  return http.put(`/tasks/${id}/choose`, { applicationId });
}

// 获取我发布的任务（独立路径）
export function listMyPublished(params){
  return http.get('/tasks/mine', { params });
}

// 获取我发布的进行中任务（已选择执行人）
export function listMyOngoing(params){
  return http.get('/tasks/in-progress', { params });
}


// 发布者验收任务
export function acceptTask(id){
  return http.put(`/tasks/${id}/accept`, null);
}

// 获取我接的单（作为执行人参与的任务）
export function listMyAccepted(params){
  return http.get('/tasks/accepted', { params });
}

// 获取我“申请中”的任务ID列表（用于首页按钮状态恢复）
// 返回示例：data 为数组 [1, 5, 9] 或 { list: [1,5,9] }
export function listMyAppliedTaskIds(){
  return http.get('/tasks/my-applied-ids');
}

// 卖家查看买家提交的验收请求（无分页）
// 期望后端返回：
// { code:0, data:{ list:[ { userId, nickname, taskId, taskTitle?, status?, createdAt? } ] } }
// 也兼容：data 直接为数组或单对象
export function listFinishRequests(){
  return http.get('/tasks/finish-requests');
}
