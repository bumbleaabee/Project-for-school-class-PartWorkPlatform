<template>
  <div class="admin-page">
    <div class="admin-header">
      <h2>管理员控制台</h2>
      <div class="actions">
  <el-select v-model="exportType" placeholder="选择导出表" size="small" style="width: 220px" @change="onTypeChange">
          <el-option label="account" value="account" />
          <el-option label="resume" value="resume" />
          <el-option label="review" value="review" />
          <el-option label="task" value="task" />
          <el-option label="task_application" value="task_application" />
          <el-option label="task_finish_request" value="task_finish_request" />
          <el-option label="user" value="user" />
          <el-option label="user_avatar" value="user_avatar" />
        </el-select>
        <el-button type="primary" size="small" :loading="downloading" @click="doExport">导出</el-button>
      </div>
    </div>

  <el-card class="mt16" :key="tableKey">
      <template #header>
        <div class="card-header">
          <span>当前表：{{ exportType }}</span>
        </div>
      </template>
  <el-table :key="'table-'+exportType" :data="list" :height="tableHeight" stripe v-loading="loading" :row-key="rowKey" empty-text="暂无数据">
        <el-table-column
          v-for="col in columns"
          :key="exportType + '-' + col"
          :prop="col"
          :label="col"
          :min-width="120"
          :show-overflow-tooltip="true"
        >
          <template #default="{ row }">
            <span>{{ renderCell(row[col]) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="pageNo"
          v-model:page-size="pageSize"
          :page-sizes="[10,20,50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="fetchTable"
          @current-change="fetchTable"
        />
      </div>
    </el-card>

    <!-- 管理管理员 -->
    <el-card class="mt16" :key="adminCardKey">
      <template #header>
        <div class="card-header">
          <span>管理管理员</span>
          <div style="display:flex; gap:8px; align-items:center;">
            <el-input v-model.number="promoteUserId" placeholder="输入用户ID" size="small" style="width:160px" clearable />
            <el-button type="primary" size="small" :loading="addingAdmin" @click="onAddAdmin">新增管理员</el-button>
            <el-button size="small" :loading="adminLoading" @click="fetchAdmins">刷新</el-button>
          </div>
        </div>
      </template>
      <el-table :data="admins" :height="360" stripe v-loading="adminLoading" empty-text="暂无管理员">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="username" label="用户名" min-width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="danger" :loading="removingId===row.id" @click="onRemoveAdmin(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { adminListTable, adminExport, getAdminList, addAdmin, removeAdmin } from '../api/admin';

const exportType = ref('task');
const downloading = ref(false);

const list = ref([]);
const columns = ref([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = ref(20);
const loading = ref(false);
const tableKey = ref(0);
// 暂无查询条件

const tableHeight = computed(() => window.innerHeight - 300);

function buildColumns(rows){
  if (!Array.isArray(rows) || rows.length === 0) { columns.value = []; return; }
  const set = new Set();
  for (const r of rows) {
    if (r && typeof r === 'object') {
      Object.keys(r).forEach(k => set.add(k));
    }
  }
  columns.value = Array.from(set);
}

function renderCell(val){
  if (val == null) return '';
  if (typeof val === 'object') {
    try {
      const str = JSON.stringify(val);
      return str.length > 120 ? str.slice(0, 117) + '...' : str;
    } catch(_) { return String(val); }
  }
  return String(val);
}

async function fetchTable(){
  try {
    loading.value = true;
    // 清空当前数据，避免切换时误以为未更新
    list.value = [];
    columns.value = [];
    total.value = 0;
    await nextTick();
    // 统一通过导出接口拉取 CSV，并在前端解析+分页（避免后端映射到 Task 导致其他表列不匹配）
  const { blob } = await adminExport(exportType.value);
  const text = await blobToText(blob);
  const parsed = parseCsv(text);
  try { console.debug('[admin/export]', exportType.value, { textPreview: text.slice(0, 200), headers: parsed.headers, rows: parsed.rows.length }); } catch(_) {}
    // 客户端分页
    const allRows = parsed.rows.map(r => toObject(parsed.headers, r));
    total.value = allRows.length;
    const start = (pageNo.value - 1) * pageSize.value;
    const end = start + pageSize.value;
  list.value = allRows.slice(start, end);
    columns.value = parsed.headers;
    tableKey.value++;
    await nextTick();
  } catch (err) {
    const msg = err?.message || '获取数据失败';
    try { window.ElMessage?.error(msg); } catch(_) { alert(msg); }
  } finally { loading.value = false; }
}

async function doExport(){
  if (!exportType.value) return;
  downloading.value = true;
  try {
    const { blob, headers } = await adminExport(exportType.value);
    const disposition = headers?.['content-disposition'] || headers?.['Content-Disposition'];
    let filename = 'export.csv';
    if (typeof disposition === 'string'){
      const match = disposition.match(/filename\*=UTF-8''([^;\n]+)|filename="?([^";\n]+)"?/i);
      const raw = decodeURIComponent(match?.[1] || match?.[2] || '').trim();
      if (raw) filename = raw;
    }
    // 为 Windows Excel 友好：若为 UTF-8 CSV 且无 BOM，则在前端注入 BOM，避免中文乱码
    const ct = String(headers?.['content-type'] || headers?.['Content-Type'] || '').toLowerCase();
    const isCsv = ct.includes('text/csv') || filename.toLowerCase().endsWith('.csv');
    const isUtf8 = ct.includes('utf-8') || ct.includes('utf8') || (!ct.includes('gbk') && !ct.includes('gb2312') && !ct.includes('gb18030'));
    let outBlob = blob instanceof Blob ? blob : new Blob([blob], { type: 'text/csv;charset=utf-8;' });
    if (isCsv && isUtf8 && outBlob && typeof outBlob.slice === 'function') {
      try {
        const head = await outBlob.slice(0,3).arrayBuffer();
        const u8 = new Uint8Array(head);
        const hasBom = u8.length >= 3 && u8[0] === 0xEF && u8[1] === 0xBB && u8[2] === 0xBF;
        if (!hasBom) {
          const bom = new Uint8Array([0xEF, 0xBB, 0xBF]);
          outBlob = new Blob([bom, outBlob], { type: 'text/csv;charset=utf-8;' });
        }
      } catch(_) { /* 忽略探测异常，按原样下载 */ }
    }
    const url = window.URL.createObjectURL(outBlob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  } catch(err){
    // 若后端在 blob 错误体中返回了 {code,message}，http.js 已尽力解析；此处兜底提示
    const msg = err?.message || '导出失败';
    // 使用 Element Plus 全局消息（若已引入）
    try { window.ElMessage?.error(msg); } catch(_) { alert(msg); }
  } finally {
    downloading.value = false;
  }
}

function onTypeChange(){ pageNo.value = 1; fetchTable(); }
watch(exportType, () => { /* 备用兜底：某些情况下 v-model 不触发 change 回调时仍会刷新 */ onTypeChange(); });

function rowKey(row){
  // 常见键优先：id 或主键；否则用 JSON 序列化的 hash 兜底
  if (row && (row.id != null || row.ID != null)) return row.id ?? row.ID;
  try { return JSON.stringify(row); } catch(_) { return String(row); }
}

// 轻量 CSV 解析：支持双引号包裹与转义，返回 { headers: string[], rows: string[][] }
function parseCsv(text){
  // 去 BOM
  if (text.charCodeAt(0) === 0xFEFF) text = text.slice(1);
  const lines = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n').filter(l => l != null && l.trim().length > 0);
  if (lines.length === 0) return { headers: [], rows: [] };
  // 自动检测分隔符：优先逗号，其次分号、制表符
  const sep = detectSep(lines[0]);
  const headers = parseCsvLine(lines[0], sep).map(h => (h ?? '').trim());
  if (headers.length === 0 || (headers.length === 1 && headers[0] === '')) return { headers: [], rows: [] };
  const rows = [];
  for (let i = 1; i < lines.length; i++) {
    const cells = parseCsvLine(lines[i], sep);
    // 跳过空行/全空单元格行
    if (cells.every(c => (c ?? '').trim() === '')) continue;
    rows.push(cells);
  }
  // 若解析后仅有表头无数据，尝试使用备用分隔符重试一次
  if (rows.length === 0 && sep !== ';') {
    const altSep = sep === ',' ? ';' : (sep === '\t' ? ',' : ',');
    const headers2 = parseCsvLine(lines[0], altSep).map(h => (h ?? '').trim());
    const altRows = [];
    for (let i = 1; i < lines.length; i++) {
      const cells = parseCsvLine(lines[i], altSep);
      if (cells.every(c => (c ?? '').trim() === '')) continue;
      altRows.push(cells);
    }
    if (headers2.length > 0 && altRows.length > 0) return { headers: headers2, rows: altRows };
  }
  return { headers, rows };
}

function detectSep(line){
  const counts = [',',';','\t'].map(s => ({ s, c: (line.match(new RegExp(escapeRegex(s), 'g')) || []).length }));
  counts.sort((a,b)=> b.c - a.c);
  return counts[0].c > 0 ? counts[0].s : ',';
}

function escapeRegex(s){ return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); }

function parseCsvLine(line, sep){
  const out = [];
  let cur = '';
  let inQuotes = false;
  for (let i = 0; i < line.length; i++) {
    const ch = line[i];
    if (inQuotes) {
      if (ch === '"') {
        if (line[i+1] === '"') { cur += '"'; i++; } // 转义双引号
        else { inQuotes = false; }
      } else { cur += ch; }
    } else {
      if (ch === (sep || ',')) { out.push(cur); cur = ''; }
      else if (ch === '"') { inQuotes = true; }
      else { cur += ch; }
    }
  }
  out.push(cur);
  return out;
}

function toObject(keys, arr){
  const obj = {};
  for (let i = 0; i < keys.length; i++) obj[keys[i]] = arr[i] ?? '';
  return obj;
}

async function blobToText(blob){
  try { return await blob.text(); } catch(_) {}
  try {
    const buf = await blob.arrayBuffer();
    // 优先 utf-8，失败则尝试 gbk、gb2312（部分浏览器支持）
    try { return new TextDecoder('utf-8').decode(buf); } catch(_) {}
    try { return new TextDecoder('gbk').decode(buf); } catch(_) {}
    try { return new TextDecoder('gb2312').decode(buf); } catch(_) {}
    return new TextDecoder().decode(buf);
  } catch(_) { return ''; }
}
onMounted(fetchTable);

// 管理管理员：状态与方法
const admins = ref([]);
const adminLoading = ref(false);
const addingAdmin = ref(false);
const removingId = ref(null);
const promoteUserId = ref();
const adminCardKey = ref(0);

async function fetchAdmins(){
  try {
    adminLoading.value = true;
    const resp = await getAdminList();
    // http 封装：若后端返回 data 为数组，已标准化为 { list, total }
    admins.value = Array.isArray(resp?.list) ? resp.list : (Array.isArray(resp) ? resp : []);
    adminCardKey.value++;
  } catch (err) {
    const msg = err?.message || '获取管理员列表失败';
    try { window.ElMessage?.error(msg); } catch(_) { alert(msg); }
  } finally {
    adminLoading.value = false;
  }
}

async function onAddAdmin(){
  if (!promoteUserId.value || Number.isNaN(Number(promoteUserId.value))) {
    try { window.ElMessage?.warning('请输入有效的用户ID'); } catch(_) { alert('请输入有效的用户ID'); }
    return;
  }
  addingAdmin.value = true;
  try {
    await addAdmin(Number(promoteUserId.value));
    try { window.ElMessage?.success('已提升为管理员'); } catch(_) {}
    promoteUserId.value = undefined;
    await fetchAdmins();
  } catch(err){
    const msg = err?.message || '新增管理员失败';
    try { window.ElMessage?.error(msg); } catch(_) { alert(msg); }
  } finally {
    addingAdmin.value = false;
  }
}

async function onRemoveAdmin(row){
  if (!row?.id) return;
  try {
    const ok = window.ElMessageBox ? await window.ElMessageBox.confirm('确认移除该管理员吗？', '提示', { type: 'warning' }) : confirm('确认移除该管理员吗？');
    if (ok === false) return;
  } catch(_){ return; }
  removingId.value = row.id;
  try {
    await removeAdmin(row.id);
    try { window.ElMessage?.success('已移除管理员'); } catch(_) {}
    await fetchAdmins();
  } catch(err){
    const msg = err?.message || '移除管理员失败';
    try { window.ElMessage?.error(msg); } catch(_) { alert(msg); }
  } finally {
    removingId.value = null;
  }
}

onMounted(() => { fetchAdmins(); });
</script>

<style scoped>
.admin-page { max-width: 1100px; margin: 0 auto; }
.admin-header { display: flex; align-items: center; justify-content: space-between; margin: 8px 0 12px; }
.actions { display: flex; gap: 8px; align-items: center; }
.mt16 { margin-top: 16px; }
.card-header { display:flex; align-items:center; justify-content:space-between; }
.pager { display:flex; justify-content:flex-end; margin-top: 12px; }
</style>
