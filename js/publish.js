// 发布页面交互功能

// 图片上传相关
const imageInput = document.getElementById('imageInput');
const uploadBox = document.getElementById('uploadBox');
const imageList = document.getElementById('imageList');
const previewImages = document.getElementById('previewImages');
let uploadedImages = [];

// 点击上传框
uploadBox.addEventListener('click', () => {
    imageInput.click();
});

// 处理图片上传
imageInput.addEventListener('change', (e) => {
    const files = Array.from(e.target.files);
    
    files.forEach(file => {
        if (uploadedImages.length >= 9) {
            showToast('最多只能上传9张图片');
            return;
        }
        
        if (file.size > 10 * 1024 * 1024) {
            showToast('图片大小不能超过10MB');
            return;
        }
        
        const reader = new FileReader();
        reader.onload = (e) => {
            const imageData = {
                url: e.target.result,
                file: file
            };
            uploadedImages.push(imageData);
            renderImages();
            updatePreviewImages();
        };
        reader.readAsDataURL(file);
    });
    
    imageInput.value = '';
});

// 渲染图片列表
function renderImages() {
    // 移除所有图片预览
    document.querySelectorAll('.image-preview-item').forEach(item => item.remove());
    
    uploadedImages.forEach((image, index) => {
        const previewItem = document.createElement('div');
        previewItem.className = 'image-preview-item';
        previewItem.innerHTML = `
            <img src="${image.url}" alt="预览图片">
            <button class="remove-image" data-index="${index}">
                <i class="fas fa-times"></i>
            </button>
        `;
        imageList.insertBefore(previewItem, uploadBox);
    });
    
    // 控制上传框显示
    uploadBox.style.display = uploadedImages.length >= 9 ? 'none' : 'flex';
    
    // 绑定删除事件
    document.querySelectorAll('.remove-image').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const index = parseInt(e.currentTarget.dataset.index);
            uploadedImages.splice(index, 1);
            renderImages();
            updatePreviewImages();
        });
    });
}

// 更新预览图片
function updatePreviewImages() {
    if (uploadedImages.length > 0) {
        previewImages.innerHTML = `<img src="${uploadedImages[0].url}" alt="预览">`;
    } else {
        previewImages.innerHTML = `
            <div class="preview-placeholder">
                <i class="fas fa-image"></i>
                <p>图片预览</p>
            </div>
        `;
    }
}

// 标题输入
const titleInput = document.getElementById('titleInput');
const titleCount = document.getElementById('titleCount');
const previewTitle = document.getElementById('previewTitle');

titleInput.addEventListener('input', (e) => {
    const length = e.target.value.length;
    titleCount.textContent = length;
    
    if (e.target.value) {
        previewTitle.textContent = e.target.value;
    } else {
        previewTitle.textContent = '标题将在这里显示';
    }
});

// 内容输入
const contentInput = document.getElementById('contentInput');
const contentCount = document.getElementById('contentCount');
const previewContent = document.getElementById('previewContent');

contentInput.addEventListener('input', (e) => {
    const length = e.target.value.length;
    contentCount.textContent = length;
    
    if (e.target.value) {
        previewContent.textContent = e.target.value;
    } else {
        previewContent.textContent = '内容将在这里显示...';
    }
});

// 标签管理
const tagInput = document.getElementById('tagInput');
const addTagBtn = document.getElementById('addTagBtn');
const selectedTags = document.getElementById('selectedTags');
const previewTags = document.getElementById('previewTags');
let tags = [];

// 添加标签
function addTag(tagName) {
    if (!tagName) return;
    
    if (tags.length >= 5) {
        showToast('最多只能添加5个话题');
        return;
    }
    
    if (tags.includes(tagName)) {
        showToast('该话题已添加');
        return;
    }
    
    tags.push(tagName);
    renderTags();
}

// 渲染标签
function renderTags() {
    selectedTags.innerHTML = tags.map((tag, index) => `
        <span class="selected-tag">
            #${tag}
            <button class="remove-tag" data-index="${index}">
                <i class="fas fa-times"></i>
            </button>
        </span>
    `).join('');
    
    previewTags.innerHTML = tags.map(tag => `
        <span class="preview-tag">#${tag}</span>
    `).join('');
    
    // 绑定删除事件
    document.querySelectorAll('.remove-tag').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const index = parseInt(e.currentTarget.dataset.index);
            tags.splice(index, 1);
            renderTags();
        });
    });
}

// 添加标签按钮
addTagBtn.addEventListener('click', () => {
    const tagName = tagInput.value.trim().replace(/^#/, '');
    if (tagName) {
        addTag(tagName);
        tagInput.value = '';
    }
});

// 回车添加标签
tagInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        const tagName = tagInput.value.trim().replace(/^#/, '');
        if (tagName) {
            addTag(tagName);
            tagInput.value = '';
        }
    }
});

// 热门标签点击
document.querySelectorAll('.hot-tag').forEach(tag => {
    tag.addEventListener('click', function() {
        const tagName = this.dataset.tag;
        addTag(tagName);
    });
});

// AI 辅助功能
const aiOptimizeBtn = document.getElementById('aiOptimizeBtn');
const aiTemplateBtn = document.getElementById('aiTemplateBtn');

// AI 优化文案
aiOptimizeBtn.addEventListener('click', () => {
    const content = contentInput.value.trim();
    if (!content) {
        showToast('请先输入内容');
        return;
    }
    
    showLoading('AI正在优化文案...');
    
    // 模拟AI处理
    setTimeout(() => {
        const optimized = content + '\n\n✨ 以上内容已经过AI优化，更加生动有趣！';
        contentInput.value = optimized;
        contentInput.dispatchEvent(new Event('input'));
        hideLoading();
        showToast('文案优化完成');
    }, 2000);
});

// AI 模板
aiTemplateBtn.addEventListener('click', () => {
    showTemplateModal();
});

// 显示模板选择弹窗
function showTemplateModal() {
    const modal = document.createElement('div');
    modal.className = 'template-modal';
    modal.innerHTML = `
        <div class="template-modal-content">
            <div class="template-modal-header">
                <h3>选择内容模板</h3>
                <button class="btn-close-modal">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="template-list">
                <div class="template-item" data-template="study">
                    <i class="fas fa-graduation-cap"></i>
                    <h4>学习经验分享</h4>
                    <p>分享学习心得、备考经验等</p>
                </div>
                <div class="template-item" data-template="food">
                    <i class="fas fa-utensils"></i>
                    <h4>美食探店</h4>
                    <p>推荐好吃的餐厅、食堂</p>
                </div>
                <div class="template-item" data-template="activity">
                    <i class="fas fa-calendar-alt"></i>
                    <h4>活动通知</h4>
                    <p>发布社团或校园活动信息</p>
                </div>
                <div class="template-item" data-template="secondhand">
                    <i class="fas fa-shopping-bag"></i>
                    <h4>二手交易</h4>
                    <p>出售闲置物品</p>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    setTimeout(() => modal.classList.add('show'), 10);
    
    // 关闭按钮
    modal.querySelector('.btn-close-modal').addEventListener('click', () => {
        modal.classList.remove('show');
        setTimeout(() => modal.remove(), 300);
    });
    
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('show');
            setTimeout(() => modal.remove(), 300);
        }
    });
    
    // 选择模板
    modal.querySelectorAll('.template-item').forEach(item => {
        item.addEventListener('click', function() {
            const template = this.dataset.template;
            applyTemplate(template);
            modal.classList.remove('show');
            setTimeout(() => modal.remove(), 300);
        });
    });
}

// 应用模板
function applyTemplate(template) {
    const templates = {
        study: {
            title: '高效学习方法分享 📚',
            content: '📖 学习心得分享\n\n今天给大家分享一些我的学习方法和经验...\n\n💡 核心要点：\n1. 制定合理的学习计划\n2. 保持专注和效率\n3. 及时复习巩固\n\n希望对大家有帮助！'
        },
        food: {
            title: '校园美食探店 🍜',
            content: '🍽️ 今日探店\n\n店铺名称：\n地址位置：\n人均消费：\n\n✨ 推荐菜品：\n\n📝 总体评价：\n味道、环境、服务都很不错，值得一试！'
        },
        activity: {
            title: '活动通知 📢',
            content: '🎉 活动名称：\n📅 时间：\n📍 地点：\n\n活动内容：\n\n报名方式：\n\n期待大家的参与！'
        },
        secondhand: {
            title: '闲置物品出售 🛍️',
            content: '📦 物品名称：\n💰 价格：\n📍 交易地点：\n\n物品描述：\n• 购买时间：\n• 使用情况：\n• 出售原因：\n\n联系方式：\n\n诚心出售，欢迎联系！'
        }
    };
    
    const selectedTemplate = templates[template];
    if (selectedTemplate) {
        titleInput.value = selectedTemplate.title;
        contentInput.value = selectedTemplate.content;
        titleInput.dispatchEvent(new Event('input'));
        contentInput.dispatchEvent(new Event('input'));
        showToast('模板已应用');
    }
}

// 保存草稿
document.querySelector('.btn-draft').addEventListener('click', () => {
    const draft = {
        title: titleInput.value,
        content: contentInput.value,
        tags: tags,
        images: uploadedImages.map(img => img.url),
        category: document.querySelector('input[name="category"]:checked')?.value,
        time: new Date().toISOString()
    };
    
    localStorage.setItem('noteDraft', JSON.stringify(draft));
    showToast('草稿已保存');
});

// 加载草稿
function loadDraft() {
    const draft = localStorage.getItem('noteDraft');
    if (draft) {
        const data = JSON.parse(draft);
        
        // 询问是否加载草稿
        if (confirm('检测到未发布的草稿，是否继续编辑？')) {
            titleInput.value = data.title || '';
            contentInput.value = data.content || '';
            tags = data.tags || [];
            
            titleInput.dispatchEvent(new Event('input'));
            contentInput.dispatchEvent(new Event('input'));
            renderTags();
            
            if (data.category) {
                const categoryInput = document.querySelector(`input[name="category"][value="${data.category}"]`);
                if (categoryInput) categoryInput.checked = true;
            }
        }
    }
}

// 发布笔记
document.querySelector('.btn-publish-main').addEventListener('click', () => {
    // 验证必填项
    if (uploadedImages.length === 0) {
        showToast('请至少上传一张图片');
        return;
    }
    
    if (!titleInput.value.trim()) {
        showToast('请填写标题');
        return;
    }
    
    if (!contentInput.value.trim()) {
        showToast('请填写内容');
        return;
    }
    
    const category = document.querySelector('input[name="category"]:checked');
    if (!category) {
        showToast('请选择分类');
        return;
    }
    
    showLoading('正在发布...');
    
    // 模拟发布
    setTimeout(() => {
        hideLoading();
        localStorage.removeItem('noteDraft');
        showSuccessModal();
    }, 2000);
});

// 显示发布成功弹窗
function showSuccessModal() {
    const modal = document.createElement('div');
    modal.className = 'success-modal';
    modal.innerHTML = `
        <div class="success-modal-content">
            <div class="success-icon">
                <i class="fas fa-check-circle"></i>
            </div>
            <h3>发布成功！</h3>
            <p>你的笔记已成功发布到社区</p>
            <div class="success-actions">
                <button class="btn-view-note" onclick="window.location.href='detail.html?id=1'">查看笔记</button>
                <button class="btn-back-home" onclick="window.location.href='index.html'">返回首页</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    setTimeout(() => modal.classList.add('show'), 10);
}

// 显示加载提示
function showLoading(message) {
    const loading = document.createElement('div');
    loading.className = 'loading-overlay';
    loading.id = 'loadingOverlay';
    loading.innerHTML = `
        <div class="loading-content">
            <i class="fas fa-spinner fa-spin"></i>
            <p>${message}</p>
        </div>
    `;
    document.body.appendChild(loading);
    setTimeout(() => loading.classList.add('show'), 10);
}

// 隐藏加载提示
function hideLoading() {
    const loading = document.getElementById('loadingOverlay');
    if (loading) {
        loading.classList.remove('show');
        setTimeout(() => loading.remove(), 300);
    }
}

// 提示消息
function showToast(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => toast.classList.add('show'), 10);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 2000);
}

// 添加样式
const style = document.createElement('style');
style.textContent = `
    /* 模板弹窗 */
    .template-modal {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 2000;
        opacity: 0;
        transition: opacity 0.3s;
    }
    
    .template-modal.show {
        opacity: 1;
    }
    
    .template-modal-content {
        background: white;
        border-radius: 16px;
        width: 90%;
        max-width: 600px;
        max-height: 80vh;
        overflow-y: auto;
    }
    
    .template-modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 25px;
        border-bottom: 1px solid var(--border-color);
    }
    
    .template-modal-header h3 {
        font-size: 18px;
    }
    
    .btn-close-modal {
        width: 32px;
        height: 32px;
        border: none;
        background: transparent;
        color: var(--text-light);
        font-size: 20px;
        cursor: pointer;
        border-radius: 50%;
        transition: all 0.3s;
    }
    
    .btn-close-modal:hover {
        background: var(--background-color);
        color: var(--text-primary);
    }
    
    .template-list {
        padding: 20px;
        display: grid;
        gap: 15px;
    }
    
    .template-item {
        padding: 20px;
        border: 2px solid var(--border-color);
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s;
    }
    
    .template-item:hover {
        border-color: var(--primary-color);
        background: var(--hover-color);
        transform: translateY(-2px);
    }
    
    .template-item i {
        font-size: 32px;
        color: var(--primary-color);
        margin-bottom: 10px;
    }
    
    .template-item h4 {
        font-size: 16px;
        margin-bottom: 5px;
    }
    
    .template-item p {
        font-size: 13px;
        color: var(--text-secondary);
    }
    
    /* 加载遮罩 */
    .loading-overlay {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.7);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 3000;
        opacity: 0;
        transition: opacity 0.3s;
    }
    
    .loading-overlay.show {
        opacity: 1;
    }
    
    .loading-content {
        text-align: center;
        color: white;
    }
    
    .loading-content i {
        font-size: 48px;
        margin-bottom: 15px;
    }
    
    .loading-content p {
        font-size: 16px;
    }
    
    /* 成功弹窗 */
    .success-modal {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 3000;
        opacity: 0;
        transition: opacity 0.3s;
    }
    
    .success-modal.show {
        opacity: 1;
    }
    
    .success-modal-content {
        background: white;
        border-radius: 16px;
        padding: 40px;
        text-align: center;
        max-width: 400px;
    }
    
    .success-icon {
        font-size: 64px;
        color: var(--success-color);
        margin-bottom: 20px;
    }
    
    .success-modal-content h3 {
        font-size: 24px;
        margin-bottom: 10px;
    }
    
    .success-modal-content p {
        color: var(--text-secondary);
        margin-bottom: 25px;
    }
    
    .success-actions {
        display: flex;
        gap: 15px;
    }
    
    .btn-view-note, .btn-back-home {
        flex: 1;
        padding: 12px;
        border: none;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s;
    }
    
    .btn-view-note {
        background: var(--primary-color);
        color: white;
    }
    
    .btn-view-note:hover {
        background: var(--primary-dark);
    }
    
    .btn-back-home {
        background: var(--background-color);
        color: var(--text-primary);
    }
    
    .btn-back-home:hover {
        background: var(--border-color);
    }
    
    /* Toast提示 */
    .toast {
        position: fixed;
        top: 100px;
        left: 50%;
        transform: translateX(-50%) translateY(-20px);
        background: rgba(0, 0, 0, 0.8);
        color: white;
        padding: 12px 24px;
        border-radius: 8px;
        font-size: 14px;
        z-index: 4000;
        opacity: 0;
        transition: all 0.3s;
    }
    
    .toast.show {
        opacity: 1;
        transform: translateX(-50%) translateY(0);
    }
`;
document.head.appendChild(style);

// 页面加载时尝试加载草稿
window.addEventListener('DOMContentLoaded', loadDraft);


