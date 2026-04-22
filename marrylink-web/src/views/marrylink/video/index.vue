<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="关键字">
          <el-input
            v-model="queryParams.keyword"
            placeholder="视频标题"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="主持人ID">
          <el-input
            v-model="queryParams.hostId"
            placeholder="主持人ID"
            clearable
            style="width: 120px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>

        <el-form-item label="状态" style="width: 150px;">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="待审核" :value="2" />
            <el-option label="已禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="首页展示" style="width: 170px;">
          <el-select v-model="queryParams.showOnHome" placeholder="全部" clearable>
            <el-option label="首页展示" :value="1" />
            <el-option label="不展示" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="search" @click="handleQuery">搜索</el-button>
          <el-button icon="refresh" @click="handleResetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-card shadow="hover">
      <!-- 操作按钮 -->
      <div style="margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 14px; color: #606266;">
          提示：打开「首页展示」开关的视频将显示在新人端首页；视频同时会展示在对应主持人详情页
        </span>
        <el-button type="primary" icon="upload" @click="handleUploadDialog">上传视频</el-button>
      </div>

      <el-table v-loading="loading" :data="pageData" border stripe>
        <el-table-column label="ID" prop="id" width="70" />
        <el-table-column label="视频" width="140">
          <template #default="scope">
            <div class="video-cell" @click="handlePreviewVideo(scope.row)">
              <div class="video-thumb">
                <el-image
                  v-if="scope.row.coverUrl"
                  :src="baseUrl + scope.row.coverUrl"
                  fit="cover"
                  style="width: 100px; height: 70px; border-radius: 4px;"
                />
                <div v-else class="video-thumb-placeholder">
                  <el-icon :size="24"><VideoPlay /></el-icon>
                </div>
                <div class="play-overlay">
                  <el-icon :size="20" color="#fff"><VideoPlay /></el-icon>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="标题" prop="title" min-width="150" show-overflow-tooltip />
        <el-table-column label="描述" prop="description" min-width="150" show-overflow-tooltip />
        <el-table-column label="主持人ID" prop="hostId" width="100" />
        <el-table-column label="文件大小" width="100">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ statusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="首页展示" width="100">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.showOnHome === 1"
              @change="(val) => handleToggleShowOnHome(scope.row.id, val ? 1 : 0)"
            />
          </template>
        </el-table-column>
        <el-table-column label="上传时间" prop="createTime" width="170" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="scope">
            <el-button type="primary" icon="edit" link size="small" @click="handleOpenDialog(scope.row)">
              编辑
            </el-button>
            <el-button
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" icon="delete" link size="small" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-if="total > 0"
        :total="total"
        :page="queryParams.current"
        :limit="queryParams.size"
        @update:page="queryParams.current = $event"
        @update:limit="queryParams.size = $event"
        @pagination="fetchData"
      />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" @close="handleCloseDialog">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入视频描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="999" />
          <span style="margin-left: 8px; font-size: 12px; color: #999;">数值越小越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadDialog.visible" title="上传案例视频" width="500px" @close="handleCloseUpload">
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="请输入视频描述" />
        </el-form-item>
        <el-form-item label="视频文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept="video/mp4,video/quicktime,video/x-msvideo,video/x-flv,video/x-ms-wmv"
          >
            <el-button type="primary">选择视频文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持 mp4/mov/avi/flv/wmv 格式，最大 500MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <div v-if="uploadProgress > 0 && uploadProgress < 100" style="margin-top: 16px;">
        <el-progress :percentage="uploadProgress" :stroke-width="8" />
      </div>
      <template #footer>
        <el-button @click="handleCloseUpload">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUploadSubmit">上传</el-button>
      </template>
    </el-dialog>

    <!-- 视频预览弹窗 -->
    <el-dialog v-model="previewDialog.visible" :title="previewDialog.title" width="700px" @close="handleClosePreview">
      <div style="text-align: center;">
        <video
          v-if="previewDialog.videoUrl"
          :src="previewDialog.videoUrl"
          controls
          style="max-width: 100%; max-height: 400px; border-radius: 8px;"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay } from '@element-plus/icons-vue'
import {
  getHostVideoPage,
  updateHostVideo,
  deleteHostVideo,
  updateHostVideoShowOnHome,
  updateHostVideoStatus,
  uploadHostVideo
} from '@/api/marrylink-api'
import Pagination from '@/components/Pagination/index.vue'

const baseUrl = import.meta.env.VITE_APP_BASE_API

const queryParams = reactive({
  current: 1,
  size: 10,
  keyword: '',
  hostId: undefined,
  status: undefined,
  showOnHome: undefined
})

const pageData = ref([])
const total = ref(0)
const loading = ref(false)

// 编辑弹窗
const dialog = reactive({ visible: false, title: '编辑视频' })
const formData = reactive({ id: null, title: '', description: '', sortOrder: 0 })
const formRef = ref()
const rules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }]
}

// 上传弹窗
const uploadDialog = reactive({ visible: false })
const uploadForm = reactive({ title: '', description: '', file: null })
const uploadFormRef = ref()
const uploadRef = ref()
const uploadRules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }]
}
const uploading = ref(false)
const uploadProgress = ref(0)

// 预览弹窗
const previewDialog = reactive({ visible: false, title: '', videoUrl: '' })

function statusTagType(status) {
  if (status === 1) return 'success'
  if (status === 2) return 'warning'
  return 'danger'
}

function statusLabel(status) {
  if (status === 1) return '正常'
  if (status === 2) return '待审核'
  return '已禁用'
}

function formatFileSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getHostVideoPage(queryParams)
    pageData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.current = 1
  fetchData()
}

function handleResetQuery() {
  queryParams.keyword = ''
  queryParams.hostId = undefined
  queryParams.status = undefined
  queryParams.showOnHome = undefined
  handleQuery()
}

// === 编辑 ===
function handleOpenDialog(row) {
  dialog.visible = true
  dialog.title = '编辑视频'
  formData.id = row.id
  formData.title = row.title || ''
  formData.description = row.description || ''
  formData.sortOrder = row.sortOrder || 0
}

function handleCloseDialog() {
  dialog.visible = false
  formRef.value?.resetFields()
  Object.assign(formData, { id: null, title: '', description: '', sortOrder: 0 })
}

function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateHostVideo(formData)
        ElMessage.success('修改成功')
        handleCloseDialog()
        fetchData()
      } finally {
        loading.value = false
      }
    }
  })
}

// === 删除 ===
function handleDelete(id) {
  ElMessageBox.confirm('确认删除该视频?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteHostVideo(id)
    ElMessage.success('删除成功')
    fetchData()
  })
}

// === 首页展示开关 ===
async function handleToggleShowOnHome(id, val) {
  try {
    await updateHostVideoShowOnHome(id, val)
    ElMessage.success(val === 1 ? '已设为首页展示' : '已取消首页展示')
    fetchData()
  } catch {
    ElMessage.error('更新失败')
  }
}

// === 状态切换 ===
async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${label}该视频?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await updateHostVideoStatus(row.id, newStatus)
    ElMessage.success(`${label}成功`)
    fetchData()
  } catch { /* cancelled */ }
}

// === 上传 ===
function handleUploadDialog() {
  uploadDialog.visible = true
}

function handleFileChange(file) {
  uploadForm.file = file.raw
}

function handleFileRemove() {
  uploadForm.file = null
}

function handleCloseUpload() {
  uploadDialog.visible = false
  uploadForm.title = ''
  uploadForm.description = ''
  uploadForm.file = null
  uploadProgress.value = 0
  uploadRef.value?.clearFiles()
}

async function handleUploadSubmit() {
  uploadFormRef.value.validate(async (valid) => {
    if (!valid) return
    if (!uploadForm.file) {
      ElMessage.warning('请选择视频文件')
      return
    }

    uploading.value = true
    uploadProgress.value = 10
    try {
      const fd = new FormData()
      fd.append('file', uploadForm.file)
      fd.append('title', uploadForm.title)
      if (uploadForm.description) {
        fd.append('description', uploadForm.description)
      }
      uploadProgress.value = 30
      await uploadHostVideo(fd)
      uploadProgress.value = 100
      ElMessage.success('上传成功')
      handleCloseUpload()
      fetchData()
    } catch (e) {
      ElMessage.error('上传失败')
    } finally {
      uploading.value = false
    }
  })
}

// === 视频预览 ===
function handlePreviewVideo(row) {
  previewDialog.visible = true
  previewDialog.title = row.title || '视频预览'
  previewDialog.videoUrl = baseUrl + row.videoUrl
}

function handleClosePreview() {
  previewDialog.visible = false
  previewDialog.videoUrl = ''
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.video-cell {
  cursor: pointer;
}

.video-thumb {
  position: relative;
  width: 100px;
  height: 70px;
  border-radius: 4px;
  overflow: hidden;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: white;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.2s;
}

.video-thumb:hover .play-overlay {
  opacity: 1;
}

:deep(.el-table__cell) {
  line-height: 1.8;
}
</style>
