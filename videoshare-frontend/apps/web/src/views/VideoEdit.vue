<template>
  <div class="edit-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="edit-body">
      <div class="edit-card" v-loading="loading">
        <div class="card-header">
          <h1 class="card-title">编辑视频</h1>
        </div>

        <div v-if="video" class="edit-form">
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="视频标题" prop="title">
              <el-input v-model="form.title" placeholder="输入视频标题..."
                :maxlength="100" show-word-limit size="large" />
            </el-form-item>
            <el-form-item label="视频简介">
              <el-input v-model="form.description" type="textarea"
                placeholder="介绍视频内容..." :rows="4"
                :maxlength="2000" show-word-limit resize="none" />
            </el-form-item>
            <el-form-item label="视频分类" prop="category">
              <el-select v-model="form.category" placeholder="选择分类" style="width:100%">
                <el-option v-for="cat in categories" :key="cat.value"
                  :label="cat.label" :value="cat.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签">
              <div class="tag-input-area">
                <el-tag v-for="tag in tagList" :key="tag" closable
                  @close="removeTag(tag)" class="tag-item">{{ tag }}</el-tag>
                <input v-if="tagList.length < 5" v-model="tagInput" class="tag-input"
                  placeholder="输入标签后按回车（最多5个）"
                  @keydown.enter.prevent="addTag"
                  @keydown.backspace="handleBackspace" />
              </div>
            </el-form-item>
          </el-form>

          <div class="form-actions">
            <el-button size="large" @click="goBack">取消</el-button>
            <el-button type="primary" size="large" :loading="saving"
              @click="handleSave">保存</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { videoApi } from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const video = ref(null)
const tagList = ref([])
const tagInput = ref('')
const formRef = ref(null)

const form = reactive({ title: '', description: '', category: '' })

const categories = [
  { value: 'tech', label: '科技' }, { value: 'game', label: '游戏' },
  { value: 'music', label: '音乐' }, { value: 'sport', label: '体育' },
  { value: 'food', label: '美食' }, { value: 'life', label: '生活' },
  { value: 'edu', label: '教育' }, { value: 'film', label: '影视' },
]

const rules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }, { min: 2, max: 100, message: '标题长度 2-100 字', trigger: 'blur' }],
}

onMounted(loadVideo)

async function loadVideo() {
  const videoId = route.params.videoId
  if (!videoId) { ElMessage.error('参数错误'); return }
  loading.value = true
  try {
    const detail = await videoApi.getVideoDetail(videoId)
    video.value = detail
    form.title = detail.title || ''
    form.description = detail.description || ''
    form.category = detail.category || ''
    tagList.value = detail.tags ? detail.tags.split(',') : []
  } catch { ElMessage.error('加载视频信息失败') }
  finally { loading.value = false }
}

function addTag() {
  const tag = tagInput.value.trim()
  if (!tag) return
  if (tagList.value.includes(tag)) { ElMessage.warning('标签已存在'); return }
  if (tagList.value.length >= 5) { ElMessage.warning('最多添加 5 个标签'); return }
  tagList.value.push(tag); tagInput.value = ''
}

function removeTag(tag) { tagList.value = tagList.value.filter(t => t !== tag) }
function handleBackspace() { if (tagInput.value === '' && tagList.value.length > 0) tagList.value.pop() }

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await videoApi.updateVideo({
      videoId: route.params.videoId,
      title: form.title,
      description: form.description,
      category: form.category,
      tags: tagList.value.join(',')
    })
    ElMessage.success('保存成功')
    router.push('/my-videos')
  } catch { ElMessage.error('保存失败') }
  finally { saving.value = false }
}

function goBack() { router.push('/my-videos') }
</script>

<style scoped>
.edit-page { min-height: 100vh; background: var(--bg-base); }
.edit-body { max-width: 700px; margin: 0 auto; padding: 72px 20px 60px; }
.edit-card { background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-lg); overflow: hidden; }
.card-header { padding: 24px 28px 16px; border-bottom: 1px solid var(--border); }
.card-title { font-size: 20px; font-weight: 700; }

.edit-form { padding: 24px 28px; }

.tag-input-area {
  display: flex; flex-wrap: wrap; gap: 6px; padding: 12px 10px;
  min-height: 64px; background: var(--bg-input);
  border: 1px solid var(--border); border-radius: var(--radius-sm);
  align-items: center; cursor: text; transition: var(--transition);
}
.tag-input-area:focus-within { border-color: var(--color-accent); }
.tag-item { border-radius: 3px !important; }
.tag-input { flex: 1; min-width: 120px; background: none; border: none; outline: none; color: var(--text-1); font-size: 13px; font-family: var(--font-body); }
.tag-input::placeholder { color: var(--text-muted); }

.form-actions { display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px; }
.form-actions .el-button { min-width: 100px; }
</style>
