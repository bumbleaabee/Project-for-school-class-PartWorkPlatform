import http from './http';

// 管理员：查看所有表的数据（分页）
// params: { type, pageNo?, pageSize? }
export function adminListTable(params){
  const { type, pageNo, pageSize } = params || {};
  // 兼容不同后端命名：type/table/tableName
  const query = { type, table: type, tableName: type, pageNo, pageSize };
  return http.get('/admin/table', { params: query });
}

// 管理员：导出数据库数据（仅支持 type 参数）
// 返回 { blob, headers }
export function adminExport(type){
  return http.get(`/admin/export`, { params: { type }, responseType: 'blob' });
}

// 管理员管理：查询所有管理员
export function getAdminList(){
  return http.get('/admin/list'); // 统一响应适配会返回 { list, total }
}

// 管理员管理：将已有用户提升为管理员
export function addAdmin(userId){
  return http.post('/admin/add', { userId });
}

// 管理员管理：移除管理员
export function removeAdmin(userId){
  return http.post('/admin/remove', { userId });
}
