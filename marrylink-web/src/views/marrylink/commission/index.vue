<template>
  <div class="app-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">总抽成金额</div>
            <div class="stat-value">¥{{ stats.totalCommission || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">待结算金额</div>
            <div class="stat-value pending">¥{{ stats.pendingAmount || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">已结算金额</div>
            <div class="stat-value settled">¥{{ stats.settledAmount || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-label">可提现余额</div>
            <div class="stat-value success">¥{{ stats.withdrawableBalance || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab 页签 -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 抽成记录 -->
      <el-tab-pane label="抽成记录" name="records">
        <div class="search-container">
          <el-form :model="recordQuery" :inline="true">
            <el-form-item label="订单号">
              <el-input v-model="recordQuery.orderNo" placeholder="订单号" clearable @keyup.enter="handleRecordQuery" />
            </el-form-item>
            <el-form-item label="主持人">
              <el-input v-model="recordQuery.hostName" placeholder="主持人姓名" clearable @keyup.enter="handleRecordQuery" />
            </el-form-item>
            <el-form-item label="状态" style="width: 150px;">
              <el-select v-model="recordQuery.status" placeholder="全部" clearable>
                <el-option label="待结算" :value="1" />
                <el-option label="已结算" :value="2" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="search" @click="handleRecordQuery">搜索</el-button>
              <el-button icon="refresh" @click="handleResetRecordQuery">重置</el-button>
              <el-button type="success" :disabled="selectedRecords.length === 0" @click="handleBatchSettle">批量结算</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-table v-loading="recordLoading" :data="recordData" border stripe @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" :selectable="canSelect" />
          <el-table-column label="订单号" prop="orderNo" width="150" />
          <el-table-column label="主持人" prop="hostName" width="120" />
          <el-table-column label="订单金额" width="120">
            <template #default="scope">¥{{ scope.row.orderAmount }}</template>
          </el-table-column>
          <el-table-column label="抽成比例" width="100">
            <template #default="scope">{{ scope.row.commissionRate }}%</template>
          </el-table-column>
          <el-table-column label="抽成金额" width="120">
            <template #default="scope">
              <span style="color: #e6a23c; font-weight: bold;">¥{{ scope.row.commissionAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'warning' : 'success'">
                {{ scope.row.status === 1 ? '待结算' : '已结算' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="结算时间" prop="settledTime" width="170" />
          <el-table-column label="创建时间" prop="createTime" width="170" />
        </el-table>

        <pagination
          v-if="recordTotal > 0"
          v-model:total="recordTotal"
          v-model:page="recordQuery.current"
          v-model:limit="recordQuery.size"
          @pagination="fetchRecords"
        />
      </el-tab-pane>

      <!-- 抽成设置 -->
      <el-tab-pane label="抽成设置" name="config">
        <el-card shadow="never" style="max-width: 600px;">
          <template #header>
            <span>当前抽成配置</span>
          </template>
          <el-form :model="configForm" label-width="120px" :rules="configRules" ref="configFormRef">
            <el-form-item label="抽成比例(%)" prop="commissionRate">
              <el-input-number v-model="configForm.commissionRate" :min="0" :max="100" :precision="2" :step="1" />
              <span style="margin-left: 10px; color: #909399;">即每笔订单平台收取的百分比</span>
            </el-form-item>
            <el-form-item label="最低抽成" prop="minAmount">
              <el-input-number v-model="configForm.minAmount" :min="0" :precision="2" :step="10" />
              <span style="margin-left: 10px; color: #909399;">每笔订单最低抽成金额(元)</span>
            </el-form-item>
            <el-form-item label="启用状态" prop="isEnabled">
              <el-switch v-model="configForm.isEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="configForm.remark" type="textarea" :rows="3" placeholder="配置备注信息" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 提现管理 -->
      <el-tab-pane label="提现管理" name="withdrawal">
        <div class="search-container" style="display: flex; justify-content: space-between; align-items: center;">
          <el-form :model="withdrawalQuery" :inline="true">
            <el-form-item label="状态" style="width: 150px;">
              <el-select v-model="withdrawalQuery.status" placeholder="全部" clearable>
                <el-option label="待处理" :value="1" />
                <el-option label="处理中" :value="2" />
                <el-option label="已完成" :value="3" />
                <el-option label="已拒绝" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="search" @click="handleWithdrawalQuery">搜索</el-button>
              <el-button icon="refresh" @click="handleResetWithdrawalQuery">重置</el-button>
            </el-form-item>
          </el-form>
          <el-button type="success" @click="handleOpenWithdrawalDialog">发起提现</el-button>
        </div>

        <el-table v-loading="withdrawalLoading" :data="withdrawalData" border stripe>
          <el-table-column label="提现单号" prop="withdrawalNo" width="150" />
          <el-table-column label="提现金额" width="120">
            <template #default="scope">
              <span style="color: #67c23a; font-weight: bold;">¥{{ scope.row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="收款方式" prop="accountType" width="100" />
          <el-table-column label="收款账号" prop="accountNo" width="180" />
          <el-table-column label="收款人" prop="accountName" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getWithdrawalStatusType(scope.row.status)">
                {{ getWithdrawalStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作人" prop="operator" width="100" />
          <el-table-column label="备注" prop="remark" min-width="150" />
          <el-table-column label="申请时间" prop="createTime" width="170" />
          <el-table-column label="操作" fixed="right" width="200">
            <template #default="scope">
              <template v-if="scope.row.status === 1">
                <el-button type="primary" link size="small" @click="handleUpdateWithdrawalStatus(scope.row.id, 2)">开始处理</el-button>
                <el-button type="danger" link size="small" @click="handleUpdateWithdrawalStatus(scope.row.id, 4)">拒绝</el-button>
              </template>
              <template v-if="scope.row.status === 2">
                <el-button type="success" link size="small" @click="handleUpdateWithdrawalStatus(scope.row.id, 3)">完成提现</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <pagination
          v-if="withdrawalTotal > 0"
          v-model:total="withdrawalTotal"
          v-model:page="withdrawalQuery.current"
          v-model:limit="withdrawalQuery.size"
          @pagination="fetchWithdrawals"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 提现申请弹窗 -->
    <el-dialog v-model="withdrawalDialog.visible" title="发起提现" width="500px" @close="handleCloseWithdrawalDialog">
      <div style="margin-bottom: 15px; padding: 10px; background: #f0f9eb; border-radius: 4px;">
        当前可提现余额: <span style="color: #67c23a; font-weight: bold; font-size: 18px;">¥{{ withdrawableBalance }}</span>
      </div>
      <el-form :model="withdrawalForm" :rules="withdrawalRules" ref="withdrawalFormRef" label-width="100px">
        <el-form-item label="提现金额" prop="amount">
          <el-input-number v-model="withdrawalForm.amount" :min="0.01" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="收款方式" prop="accountType">
          <el-select v-model="withdrawalForm.accountType" placeholder="请选择" style="width: 100%;">
            <el-option label="支付宝" value="支付宝" />
            <el-option label="银行卡" value="银行卡" />
            <el-option label="微信" value="微信" />
          </el-select>
        </el-form-item>
        <el-form-item label="收款账号" prop="accountNo">
          <el-input v-model="withdrawalForm.accountNo" placeholder="请输入收款账号" />
        </el-form-item>
        <el-form-item label="收款人" prop="accountName">
          <el-input v-model="withdrawalForm.accountName" placeholder="请输入收款人姓名" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="withdrawalForm.remark" type="textarea" :rows="2" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseWithdrawalDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmitWithdrawal">确认提现</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCommissionConfig,
  updateCommissionConfig,
  saveCommissionConfig,
  getCommissionRecordPage,
  getCommissionStats,
  settleCommissions,
  getWithdrawalPage,
  applyWithdrawal,
  updateWithdrawalStatus,
  getWithdrawableBalance
} from '@/api/marrylink-api'

// ==================== 统计数据 ====================
const stats = ref({})

async function fetchStats() {
  try {
    stats.value = await getCommissionStats()
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

// ==================== Tab ====================
const activeTab = ref('records')

// ==================== 抽成记录 ====================
const recordQuery = reactive({ current: 1, size: 10, orderNo: '', hostName: '', status: null })
const recordData = ref([])
const recordTotal = ref(0)
const recordLoading = ref(false)
const selectedRecords = ref([])

async function fetchRecords() {
  recordLoading.value = true
  try {
    const res = await getCommissionRecordPage(recordQuery)
    recordData.value = res.records
    recordTotal.value = res.total
  } finally {
    recordLoading.value = false
  }
}

function handleRecordQuery() {
  recordQuery.current = 1
  fetchRecords()
}

function handleResetRecordQuery() {
  recordQuery.orderNo = ''
  recordQuery.hostName = ''
  recordQuery.status = null
  handleRecordQuery()
}

function handleSelectionChange(val) {
  selectedRecords.value = val
}

function canSelect(row) {
  return row.status === 1
}

async function handleBatchSettle() {
  const ids = selectedRecords.value.map(r => r.id)
  ElMessageBox.confirm(`确认结算选中的 ${ids.length} 条抽成记录?`, '确认结算', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await settleCommissions(ids)
      ElMessage.success('结算成功')
      fetchRecords()
      fetchStats()
    } catch (e) {
      ElMessage.error(e.message || '结算失败')
    }
  })
}

// ==================== 抽成配置 ====================
const configForm = reactive({ id: null, commissionRate: 10, minAmount: 0, isEnabled: 1, remark: '' })
const configFormRef = ref()
const configRules = {
  commissionRate: [{ required: true, message: '请输入抽成比例', trigger: 'blur' }]
}

async function fetchConfig() {
  try {
    const res = await getCommissionConfig()
    if (res) {
      Object.assign(configForm, res)
    }
  } catch (e) {
    console.error('获取配置失败', e)
  }
}

async function handleSaveConfig() {
  configFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (configForm.id) {
          await updateCommissionConfig(configForm)
        } else {
          await saveCommissionConfig(configForm)
        }
        ElMessage.success('配置保存成功')
        fetchConfig()
      } catch (e) {
        ElMessage.error('保存失败')
      }
    }
  })
}

// ==================== 提现管理 ====================
const withdrawalQuery = reactive({ current: 1, size: 10, status: null })
const withdrawalData = ref([])
const withdrawalTotal = ref(0)
const withdrawalLoading = ref(false)
const withdrawableBalance = ref('0.00')

const withdrawalDialog = reactive({ visible: false })
const withdrawalForm = reactive({ amount: 0, accountType: '', accountNo: '', accountName: '', remark: '' })
const withdrawalFormRef = ref()
const withdrawalRules = {
  amount: [{ required: true, message: '请输入提现金额', trigger: 'blur' }],
  accountType: [{ required: true, message: '请选择收款方式', trigger: 'change' }],
  accountNo: [{ required: true, message: '请输入收款账号', trigger: 'blur' }],
  accountName: [{ required: true, message: '请输入收款人姓名', trigger: 'blur' }]
}

async function fetchWithdrawals() {
  withdrawalLoading.value = true
  try {
    const res = await getWithdrawalPage(withdrawalQuery)
    withdrawalData.value = res.records
    withdrawalTotal.value = res.total
  } finally {
    withdrawalLoading.value = false
  }
}

function handleWithdrawalQuery() {
  withdrawalQuery.current = 1
  fetchWithdrawals()
}

function handleResetWithdrawalQuery() {
  withdrawalQuery.status = null
  handleWithdrawalQuery()
}

async function handleOpenWithdrawalDialog() {
  withdrawalDialog.visible = true
  try {
    const res = await getWithdrawableBalance()
    withdrawableBalance.value = res.balance || '0.00'
  } catch (e) {
    withdrawableBalance.value = '0.00'
  }
}

function handleCloseWithdrawalDialog() {
  withdrawalDialog.visible = false
  withdrawalFormRef.value?.resetFields()
  Object.assign(withdrawalForm, { amount: 0, accountType: '', accountNo: '', accountName: '', remark: '' })
}

async function handleSubmitWithdrawal() {
  withdrawalFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await applyWithdrawal(withdrawalForm)
        ElMessage.success('提现申请已提交')
        handleCloseWithdrawalDialog()
        fetchWithdrawals()
        fetchStats()
      } catch (e) {
        ElMessage.error(e.message || '提现申请失败')
      }
    }
  })
}

function handleUpdateWithdrawalStatus(id, status) {
  const statusTexts = { 2: '开始处理', 3: '完成提现', 4: '拒绝提现' }
  ElMessageBox.confirm(`确认${statusTexts[status]}?`, '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: status === 4 ? 'warning' : 'info'
  }).then(async () => {
    try {
      await updateWithdrawalStatus(id, status)
      ElMessage.success('操作成功')
      fetchWithdrawals()
      fetchStats()
    } catch (e) {
      ElMessage.error(e.message || '操作失败')
    }
  })
}

function getWithdrawalStatusType(status) {
  const types = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger' }
  return types[status] || 'info'
}

function getWithdrawalStatusText(status) {
  const texts = { 1: '待处理', 2: '处理中', 3: '已完成', 4: '已拒绝' }
  return texts[status] || '未知'
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchStats()
  fetchRecords()
  fetchConfig()
  fetchWithdrawals()
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  padding: 10px 0;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.stat-value.pending {
  color: #e6a23c;
}
.stat-value.settled {
  color: #409eff;
}
.stat-value.success {
  color: #67c23a;
}
.search-container {
  margin-bottom: 15px;
}
</style>
