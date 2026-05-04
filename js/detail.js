// 详情页交互功能

// 点赞功能
const likeBtn = document.getElementById('likeBtn');
let isLiked = false;

likeBtn.addEventListener('click', function() {
    isLiked = !isLiked;
    const icon = this.querySelector('i');
    
    if (isLiked) {
        icon.classList.remove('far');
        icon.classList.add('fas');
        this.classList.add('active');
        this.style.color = '#e91e63';
        
        // 添加点赞动画
        this.style.transform = 'scale(1.2)';
        setTimeout(() => {
            this.style.transform = 'scale(1)';
        }, 200);
    } else {
        icon.classList.remove('fas');
        icon.classList.add('far');
        this.classList.remove('active');
        this.style.color = '';
    }
});

// 收藏功能
const collectBtn = document.getElementById('collectBtn');
let isCollected = false;

collectBtn.addEventListener('click', function() {
    isCollected = !isCollected;
    const icon = this.querySelector('i');
    
    if (isCollected) {
        icon.classList.remove('far');
        icon.classList.add('fas');
        this.classList.add('active');
        this.style.color = '#ffc107';
        
        // 添加收藏动画
        this.style.transform = 'scale(1.2)';
        setTimeout(() => {
            this.style.transform = 'scale(1)';
        }, 200);
    } else {
        icon.classList.remove('fas');
        icon.classList.add('far');
        this.classList.remove('active');
        this.style.color = '';
    }
});

// 评论功能
const commentBtn = document.getElementById('commentBtn');
commentBtn.addEventListener('click', function() {
    const commentInput = document.querySelector('.comment-input');
    commentInput.focus();
    commentInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
});

// 分享功能
const shareBtn = document.getElementById('shareBtn');
shareBtn.addEventListener('click', function() {
    // 创建分享弹窗
    const shareModal = document.createElement('div');
    shareModal.className = 'share-modal';
    shareModal.innerHTML = `
        <div class="share-modal-content">
            <h3>分享到</h3>
            <div class="share-options">
                <button class="share-option">
                    <i class="fab fa-weixin" style="color: #07c160;"></i>
                    <span>微信</span>
                </button>
                <button class="share-option">
                    <i class="fab fa-qq" style="color: #12b7f5;"></i>
                    <span>QQ</span>
                </button>
                <button class="share-option">
                    <i class="fab fa-weibo" style="color: #e6162d;"></i>
                    <span>微博</span>
                </button>
                <button class="share-option" onclick="copyLink()">
                    <i class="fas fa-link" style="color: #1e88e5;"></i>
                    <span>复制链接</span>
                </button>
            </div>
            <button class="btn-close-modal">取消</button>
        </div>
    `;
    
    document.body.appendChild(shareModal);
    setTimeout(() => shareModal.classList.add('show'), 10);
    
    // 关闭分享弹窗
    shareModal.querySelector('.btn-close-modal').addEventListener('click', () => {
        shareModal.classList.remove('show');
        setTimeout(() => shareModal.remove(), 300);
    });
    
    shareModal.addEventListener('click', (e) => {
        if (e.target === shareModal) {
            shareModal.classList.remove('show');
            setTimeout(() => shareModal.remove(), 300);
        }
    });
});

// 复制链接功能
function copyLink() {
    const url = window.location.href;
    navigator.clipboard.writeText(url).then(() => {
        showToast('链接已复制到剪贴板');
    }).catch(() => {
        showToast('复制失败，请手动复制');
    });
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

// 关注按钮
const followBtn = document.querySelector('.btn-follow-large');
followBtn.addEventListener('click', function() {
    if (this.textContent === '+ 关注') {
        this.textContent = '✓ 已关注';
        this.style.background = '#e0e0e0';
        this.style.color = '#666';
        showToast('关注成功');
    } else {
        this.textContent = '+ 关注';
        this.style.background = 'var(--primary-color)';
        this.style.color = 'white';
        showToast('已取消关注');
    }
});

// 发布评论
const commentInput = document.querySelector('.comment-input');
const btnComment = document.querySelector('.btn-comment');

btnComment.addEventListener('click', () => {
    const content = commentInput.value.trim();
    if (content) {
        addComment(content);
        commentInput.value = '';
        showToast('评论发布成功');
    }
});

commentInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        const content = commentInput.value.trim();
        if (content) {
            addComment(content);
            commentInput.value = '';
            showToast('评论发布成功');
        }
    }
});

// 添加评论到列表
function addComment(content) {
    const commentsList = document.querySelector('.comments-list');
    const newComment = document.createElement('div');
    newComment.className = 'comment-item';
    newComment.innerHTML = `
        <img src="files/ec67477e79c0450f92ef8dbd8d657e9f.jpg" alt="用户头像" class="comment-avatar" onerror="this.src='files/default-avatar.svg'">
        <div class="comment-content">
            <div class="comment-header">
                <span class="comment-author">我</span>
                <span class="comment-time">刚刚</span>
            </div>
            <p class="comment-text">${content}</p>
            <div class="comment-actions">
                <button class="comment-btn">
                    <i class="far fa-heart"></i>
                    <span>0</span>
                </button>
                <button class="comment-btn">
                    <i class="far fa-comment"></i>
                    <span>回复</span>
                </button>
            </div>
        </div>
    `;
    
    commentsList.insertBefore(newComment, commentsList.firstChild);
    
    // 更新评论数量
    const commentCount = document.querySelector('.comment-count');
    const currentCount = parseInt(commentCount.textContent);
    commentCount.textContent = currentCount + 1;
}

// 评论点赞功能
document.addEventListener('click', (e) => {
    if (e.target.closest('.comment-btn') && e.target.closest('.comment-btn').querySelector('.fa-heart')) {
        const btn = e.target.closest('.comment-btn');
        const icon = btn.querySelector('i');
        const count = btn.querySelector('span');
        
        if (icon.classList.contains('far')) {
            icon.classList.remove('far');
            icon.classList.add('fas');
            icon.style.color = '#e91e63';
            count.textContent = parseInt(count.textContent) + 1;
        } else {
            icon.classList.remove('fas');
            icon.classList.add('far');
            icon.style.color = '';
            count.textContent = parseInt(count.textContent) - 1;
        }
    }
});

// 图片查看器
const detailImages = document.querySelectorAll('.detail-images img');
detailImages.forEach((img, index) => {
    img.style.cursor = 'pointer';
    img.addEventListener('click', () => {
        openImageViewer(index);
    });
});

function openImageViewer(startIndex) {
    const images = Array.from(detailImages).map(img => img.src);
    
    const viewer = document.createElement('div');
    viewer.className = 'image-viewer';
    viewer.innerHTML = `
        <div class="image-viewer-content">
            <button class="viewer-close"><i class="fas fa-times"></i></button>
            <button class="viewer-prev"><i class="fas fa-chevron-left"></i></button>
            <button class="viewer-next"><i class="fas fa-chevron-right"></i></button>
            <img src="${images[startIndex]}" alt="查看图片">
            <div class="viewer-indicator">${startIndex + 1} / ${images.length}</div>
        </div>
    `;
    
    document.body.appendChild(viewer);
    setTimeout(() => viewer.classList.add('show'), 10);
    
    let currentIndex = startIndex;
    const viewerImg = viewer.querySelector('img');
    const indicator = viewer.querySelector('.viewer-indicator');
    
    // 关闭查看器
    viewer.querySelector('.viewer-close').addEventListener('click', () => {
        viewer.classList.remove('show');
        setTimeout(() => viewer.remove(), 300);
    });
    
    viewer.addEventListener('click', (e) => {
        if (e.target === viewer) {
            viewer.classList.remove('show');
            setTimeout(() => viewer.remove(), 300);
        }
    });
    
    // 上一张
    viewer.querySelector('.viewer-prev').addEventListener('click', () => {
        currentIndex = (currentIndex - 1 + images.length) % images.length;
        viewerImg.src = images[currentIndex];
        indicator.textContent = `${currentIndex + 1} / ${images.length}`;
    });
    
    // 下一张
    viewer.querySelector('.viewer-next').addEventListener('click', () => {
        currentIndex = (currentIndex + 1) % images.length;
        viewerImg.src = images[currentIndex];
        indicator.textContent = `${currentIndex + 1} / ${images.length}`;
    });
    
    // 键盘控制
    document.addEventListener('keydown', function onKeydown(e) {
        if (e.key === 'Escape') {
            viewer.classList.remove('show');
            setTimeout(() => viewer.remove(), 300);
            document.removeEventListener('keydown', onKeydown);
        } else if (e.key === 'ArrowLeft') {
            viewer.querySelector('.viewer-prev').click();
        } else if (e.key === 'ArrowRight') {
            viewer.querySelector('.viewer-next').click();
        }
    });
}

// 添加样式到页面
const style = document.createElement('style');
style.textContent = `
    /* 分享弹窗 */
    .share-modal {
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
    
    .share-modal.show {
        opacity: 1;
    }
    
    .share-modal-content {
        background: white;
        border-radius: 16px;
        padding: 30px;
        width: 90%;
        max-width: 400px;
        transform: translateY(20px);
        transition: transform 0.3s;
    }
    
    .share-modal.show .share-modal-content {
        transform: translateY(0);
    }
    
    .share-modal-content h3 {
        font-size: 18px;
        margin-bottom: 20px;
        text-align: center;
    }
    
    .share-options {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 15px;
        margin-bottom: 20px;
    }
    
    .share-option {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 15px;
        border: none;
        background: var(--background-color);
        border-radius: 12px;
        cursor: pointer;
        transition: all 0.3s;
    }
    
    .share-option:hover {
        transform: translateY(-5px);
        box-shadow: var(--shadow-md);
    }
    
    .share-option i {
        font-size: 28px;
    }
    
    .share-option span {
        font-size: 12px;
        color: var(--text-secondary);
    }
    
    .btn-close-modal {
        width: 100%;
        padding: 12px;
        border: 1px solid var(--border-color);
        background: transparent;
        border-radius: 8px;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s;
    }
    
    .btn-close-modal:hover {
        background: var(--background-color);
    }
    
    /* 提示消息 */
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
        z-index: 3000;
        opacity: 0;
        transition: all 0.3s;
    }
    
    .toast.show {
        opacity: 1;
        transform: translateX(-50%) translateY(0);
    }
    
    /* 图片查看器 */
    .image-viewer {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.95);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 2000;
        opacity: 0;
        transition: opacity 0.3s;
    }
    
    .image-viewer.show {
        opacity: 1;
    }
    
    .image-viewer-content {
        position: relative;
        max-width: 90%;
        max-height: 90%;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    .image-viewer-content img {
        max-width: 1200px;
        max-height: 80vh;
        width: auto;
        height: auto;
        border-radius: 8px;
        object-fit: contain;
    }
    
    .viewer-close, .viewer-prev, .viewer-next {
        position: absolute;
        background: rgba(255, 255, 255, 0.2);
        border: none;
        color: white;
        width: 50px;
        height: 50px;
        border-radius: 50%;
        font-size: 20px;
        cursor: pointer;
        transition: all 0.3s;
        backdrop-filter: blur(10px);
    }
    
    .viewer-close {
        top: -60px;
        right: 0;
    }
    
    .viewer-prev {
        left: -70px;
        top: 50%;
        transform: translateY(-50%);
    }
    
    .viewer-next {
        right: -70px;
        top: 50%;
        transform: translateY(-50%);
    }
    
    .viewer-close:hover, .viewer-prev:hover, .viewer-next:hover {
        background: rgba(255, 255, 255, 0.3);
    }
    
    .viewer-indicator {
        position: absolute;
        bottom: -50px;
        left: 50%;
        transform: translateX(-50%);
        color: white;
        font-size: 14px;
        background: rgba(255, 255, 255, 0.2);
        padding: 8px 16px;
        border-radius: 20px;
        backdrop-filter: blur(10px);
    }
`;
document.head.appendChild(style);


