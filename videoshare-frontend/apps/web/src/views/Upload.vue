<template>
  <div class="upload-page">
    <NavBar @toggle-sidebar="() => {}" />

    <div class="upload-body">
      <div class="upload-card">
        <div class="card-header">
          <h1 class="card-title">发布视频</h1>
          <p class="card-sub">上传你的作品，与全世界分享</p>
        </div>

        <div class="upload-layout">
          <div class="upload-left">
            <!-- Drop zone -->
            <div class="drop-zone" :class="{ 'has-video': videoUrl, dragging }"
              @dragover.prevent="dragging = true"
              @dragleave.prevent="dragging = false"
              @drop.prevent="handleDrop">

              <template v-if="!videoUrl && !uploading">
                <svg viewBox="0 0 24 24" width="48" height="48" fill="var(--text-muted)"><path d="M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z"/></svg>
                <p class="drop-title">拖拽视频到此处</p>
                <p class="drop-sub">或者</p>
                <label class="select-btn">
                  <input type="file" accept="video/*" @change="handleFileSelect" hidden />
                  选择视频文件
                </label>
                <p class="drop-hint">支持 MP4、WebM、MOV，最大 500MB</p>
              </template>

              <template v-if="uploading">
                <div class="upload-progress">
                  <p class="progress-text">上传中... {{ uploadProgress }}%</p>
                  <div class="progress-bar-wrap">
                    <div class="progress-bar" :style="{ width: uploadProgress + '%' }" />
                  </div>
                  <p class="progress-size">{{ uploadedSize }} / {{ totalSize }}</p>
                </div>
              </template>

              <template v-if="videoUrl && !uploading">
                <p class="success-text">视频上传成功</p>
                <video class="preview-video" :src="videoUrl" controls preload="metadata" />
                <button class="re-upload-btn" @click="resetVideo">重新选择</button>
              </template>
            </div>

            <div class="cover-section">
              <div class="cover-label">视频封面</div>
              <div class="cover-upload" @click="$refs.coverInput.click()">
                <img v-if="coverUrl" :src="coverUrl" class="cover-preview" alt="封面" />
                <template v-else>
                  <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor"><path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/></svg>
                  <span>点击上传封面图</span>
                </template>
              </div>
              <input ref="coverInput" type="file" accept="image/*" hidden @change="handleCoverSelect" />
              <p class="cover-hint">建议尺寸 1280×720</p>
            </div>
          </div>

          <div class="upload-right">
            <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
              <el-form-item label="视频标题" prop="title">
                <el-input v-model="form.title" placeholder="给视频起一个吸引人的标题..."
                  :maxlength="100" show-word-limit size="large" />
              </el-form-item>
              <el-form-item label="视频简介">
                <el-input v-model="form.description" type="textarea"
                  placeholder="介绍视频内容，帮助观众了解你的视频..."
                  :rows="4" :maxlength="2000" show-word-limit resize="none" />
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
                    @close="removeTag(tag)" class="tag-item">
                    {{ tag }}
                  </el-tag>
                  <input v-if="tagList.length < 5" ref="tagInputRef"
                    v-model="tagInput" class="tag-input"
                    placeholder="输入标签后按回车（最多5个）"
                    @keydown.enter.prevent="addTag"
                    @keydown.backspace="handleBackspace" />
                </div>
              </el-form-item>
              <div class="publish-actions">
                <el-button size="large" @click="saveDraft">存为草稿</el-button>
                <el-button type="danger" size="large"
                  :loading="publishing" :disabled="!videoUrl" @click="handlePublish">
                  {{ publishing ? '发布中...' : '立即发布' }}
                </el-button>
              </div>
              <p v-if="!videoUrl" class="no-video-hint">请先上传视频文件</p>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { videoApi } from '@/api'
import { formatFileSize } from '@videoshare/utils/format'

const router = useRouter()

const videoUrl       = ref('')
const coverUrl       = ref('')
const uploading      = ref(false)
const uploadProgress = ref(0)
const uploadedSize   = ref('0 B')
const totalSize      = ref('0 B')
const dragging       = ref(false)
const publishing     = ref(false)
const coverInput     = ref(null)
const tagInput       = ref('')
const tagInputRef    = ref(null)
const formRef        = ref(null)

const form = reactive({ title: '', description: '', category: '' })
const tagList = ref([])

const categories = [
  { value: 'tech', label: '科技' }, { value: 'game', label: '游戏' },
  { value: 'music', label: '音乐' }, { value: 'sport', label: '体育' },
  { value: 'food', label: '美食' }, { value: 'life', label: '生活' },
  { value: 'edu', label: '教育' }, { value: 'film', label: '影视' },
]

const rules = {
  title:    [{ required: true, message: '请输入视频标题', trigger: 'blur' }, { min: 2, max: 100, message: '标题长度 2-100 字', trigger: 'blur' }],
  category: [{ required: true, message: '请选择视频分类', trigger: 'change' }]
}

async function uploadVideoFile(file) {
  if (file.size > 500 * 1024 * 1024) { ElMessage.error('视频文件不能超过 500MB'); return }
  totalSize.value = formatFileSize(file.size)
  uploading.value = true; uploadProgress.value = 0
  try {
    const result = await videoApi.uploadVideo(file, (percent) => {
      uploadProgress.value = percent
      uploadedSize.value = formatFileSize(file.size * percent / 100)
    })
    videoUrl.value = result.videoUrl
    ElMessage.success('视频上传成功')
  } catch { ElMessage.error('视频上传失败，请重试') }
  finally { uploading.value = false }
}

function handleDrop(e) { dragging.value = false; const file = e.dataTransfer.files[0]; if (file?.type.startsWith('video/')) uploadVideoFile(file); else ElMessage.error('请上传视频文件') }
function handleFileSelect(e) { const file = e.target.files[0]; if (file) uploadVideoFile(file) }
function resetVideo() { videoUrl.value = ''; uploadProgress.value = 0 }

async function handleCoverSelect(e) {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('封面图不能超过 5MB'); return }
  coverUrl.value = URL.createObjectURL(file)
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

async function handlePublish() {
  if (!videoUrl.value) { ElMessage.error('请先上传视频'); return }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  publishing.value = true
  try {
    await videoApi.publishVideo({ title: form.title, description: form.description, coverUrl: coverUrl.value, videoUrl: videoUrl.value, category: form.category, tags: tagList.value.join(',') })
    ElMessage.success('视频发布成功！'); router.push('/')
  } finally { publishing.value = false }
}

function saveDraft() { ElMessage.info('草稿功能开发中') }
</script>

<style scoped>
.upload-page { min-height: 100vh; background: var(--bg-base); }
.upload-body { max-width: 1100px; margin: 0 auto; padding: 72px 20px 60px; }
.upload-card { background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-lg); overflow: hidden; }
.card-header { padding: 24px 28px 0; padding-bottom: 16px; border-bottom: 1px solid var(--border); }
.card-title { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
.card-sub { font-size: 13px; color: var(--text-2); }

.upload-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 0; }
.upload-left { padding: 24px 28px; border-right: 1px solid var(--border); }
.upload-right { padding: 24px 28px; }

.drop-zone {
  border: 2px dashed var(--border-strong); border-radius: var(--radius-md);
  min-height: 240px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 12px; padding: 24px;
  text-align: center; transition: var(--transition); cursor: pointer;
  background: var(--bg-card);
}
.drop-zone:hover, .drop-zone.dragging { border-color: var(--color-accent); background: var(--color-accent-dim); }
.drop-zone.has-video { border-style: solid; border-color: var(--border); background: var(--bg-card); cursor: default; }
.drop-title { font-size: 15px; font-weight: 600; color: var(--text-1); }
.drop-sub { font-size: 13px; color: var(--text-muted); }
.drop-hint { font-size: 12px; color: var(--text-muted); }
.select-btn { display: inline-block; padding: 8px 20px; background: var(--color-accent); color: #fff; border-radius: var(--radius-sm); font-size: 13px; font-weight: 600; cursor: pointer; transition: var(--transition); }
.select-btn:hover { background: #cc0029; }

.upload-progress { display: flex; flex-direction: column; align-items: center; gap: 10px; width: 100%; }
.progress-text { font-size: 14px; font-weight: 600; }
.progress-bar-wrap { width: 80%; height: 4px; background: var(--bg-hover); border-radius: 2px; overflow: hidden; }
.progress-bar { height: 100%; background: var(--color-accent); transition: width .3s ease; border-radius: 2px; }
.progress-size { font-size: 12px; color: var(--text-muted); }

.success-text { font-size: 14px; font-weight: 600; color: #22c55e; }
.preview-video { width: 100%; max-height: 160px; border-radius: var(--radius-sm); object-fit: contain; background: #000; }
.re-upload-btn { background: none; border: 1px solid var(--border); color: var(--text-2); padding: 5px 14px; border-radius: var(--radius-sm); font-size: 12px; cursor: pointer; transition: var(--transition); font-family: var(--font-body); }
.re-upload-btn:hover { border-color: var(--color-accent); color: var(--color-accent); }

.cover-section { margin-top: 20px; }
.cover-label { font-size: 12px; font-weight: 600; color: var(--text-2); margin-bottom: 6px; }
.cover-upload { width: 100%; aspect-ratio: 16/9; border: 1px dashed var(--border-strong); border-radius: var(--radius-sm); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; cursor: pointer; background: var(--bg-card); color: var(--text-muted); font-size: 13px; transition: var(--transition); overflow: hidden; }
.cover-upload:hover { border-color: var(--color-accent); color: var(--color-accent); }
.cover-preview { width: 100%; height: 100%; object-fit: cover; }
.cover-hint { font-size: 11px; color: var(--text-muted); margin-top: 4px; }

.tag-input-area { display: flex; flex-wrap: wrap; gap: 6px; padding: 6px 10px; min-height: 40px; background: var(--bg-input); border: 1px solid var(--border); border-radius: var(--radius-sm); align-items: center; cursor: text; transition: var(--transition); }
.tag-input-area:focus-within { border-color: #555; }
.tag-item { border-radius: 3px !important; }
.tag-input { flex: 1; min-width: 120px; background: none; border: none; outline: none; color: var(--text-1); font-size: 13px; font-family: var(--font-body); }
.tag-input::placeholder { color: var(--text-muted); }

.publish-actions { display: flex; gap: 12px; margin-top: 8px; }
.publish-actions .el-button { flex: 1; height: 40px; font-size: 14px; font-weight: 600; }
.no-video-hint { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--text-muted); margin-top: 10px; justify-content: center; }

@media (max-width: 900px) {
  .upload-layout { grid-template-columns: 1fr; }
  .upload-left { border-right: none; border-bottom: 1px solid var(--border); }
}
</style>
