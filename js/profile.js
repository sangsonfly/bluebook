// 个人中心交互功能

// 标签切换
const menuItems = document.querySelectorAll('.menu-item');
const tabContents = document.querySelectorAll('.tab-content');

menuItems.forEach(item => {
    item.addEventListener('click', function(e) {
        e.preventDefault();
        
        // 移除所有active类
        menuItems.forEach(i => i.classList.remove('active'));
        tabContents.forEach(t => t.classList.remove('active'));
        
        // 添加active类
        this.classList.add('active');
        
        // 显示对应内容
        const tabId = this.dataset.tab + '-tab';
        const targetTab = document.getElementById(tabId);
        if (targetTab) {
            targetTab.classList.add('active');
        }
    });
});

// 筛选按钮
document.querySelectorAll('.filter-button').forEach(btn => {
    btn.addEventListener('click', function() {
        const parent = this.parentElement;
        parent.querySelectorAll('.filter-button').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
    });
});

// 编辑资料
document.querySelector('.btn-edit-profile').addEventListener('click', () => {
    // 切换到设置标签
    document.querySelector('.menu-item[data-tab="settings"]').click();
    showToast('正在打开编辑页面...');
});

// 编辑头像
document.querySelector('.btn-edit-avatar').addEventListener('click', () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                document.querySelector('.profile-avatar').src = e.target.result;
                showToast('头像已更新');
            };
            reader.readAsDataURL(file);
        }
    };
    input.click();
});

// 笔记操作
document.addEventListener('click', (e) => {
    // 编辑笔记
    if (e.target.closest('.action-icon') && e.target.closest('.action-icon').querySelector('.fa-edit')) {
        e.stopPropagation();
        showToast('正在打开编辑器...');
        setTimeout(() => {
            window.location.href = 'publish.html';
        }, 500);
    }
    
    // 删除笔记
    if (e.target.closest('.action-icon') && e.target.closest('.action-icon').querySelector('.fa-trash')) {
        e.stopPropagation();
        if (confirm('确定要删除这篇笔记吗？')) {
            const card = e.target.closest('.note-card');
            card.style.opacity = '0';
            card.style.transform = 'scale(0.8)';
            setTimeout(() => {
                card.remove();
                showToast('笔记已删除');
            }, 300);
        }
    }
});

// 草稿操作
document.addEventListener('click', (e) => {
    // 继续编辑草稿
    if (e.target.classList.contains('btn-continue')) {
        showToast('正在加载草稿...');
        setTimeout(() => {
            window.location.href = 'publish.html';
        }, 500);
    }
    
    // 删除草稿
    if (e.target.classList.contains('btn-delete')) {
        if (confirm('确定要删除这份草稿吗？')) {
            const draft = e.target.closest('.draft-item');
            draft.style.opacity = '0';
            setTimeout(() => {
                draft.remove();
                showToast('草稿已删除');
            }, 300);
        }
    }
});

// 关注/取消关注
document.querySelectorAll('.btn-following').forEach(btn => {
    btn.addEventListener('click', function() {
        if (this.textContent === '已关注') {
            this.textContent = '关注';
            this.style.background = 'var(--primary-color)';
            this.style.color = 'white';
            this.style.borderColor = 'var(--primary-color)';
            showToast('已取消关注');
        } else {
            this.textContent = '已关注';
            this.style.background = 'transparent';
            this.style.color = 'var(--text-secondary)';
            this.style.borderColor = 'var(--border-color)';
            showToast('关注成功');
        }
    });
});

// 保存设置
document.querySelector('.btn-save-settings')?.addEventListener('click', () => {
    showLoading('正在保存设置...');
    
    // 模拟保存
    setTimeout(() => {
        hideLoading();
        showToast('设置已保存');
        
        // 更新头部显示的用户信息
        const nickname = document.querySelector('.setting-item input[type="text"]').value;
        if (nickname) {
            document.querySelector('.username').textContent = nickname;
        }
    }, 1000);
});

// 退出登录
document.querySelector('.btn-logout')?.addEventListener('click', () => {
    if (confirm('确定要退出登录吗？')) {
        showLoading('正在退出...');
        setTimeout(() => {
            hideLoading();
            window.location.href = 'login.html';
        }, 1000);
    }
});

// 统计数据点击
document.querySelectorAll('.stat-box').forEach(box => {
    box.addEventListener('click', function() {
        const label = this.querySelector('.stat-label').textContent;
        let targetTab = '';
        
        switch(label) {
            case '笔记':
                targetTab = 'notes';
                break;
            case '获赞':
            case '收藏':
                targetTab = 'liked';
                break;
            case '关注':
                targetTab = 'following';
                break;
            case '粉丝':
                targetTab = 'fans';
                break;
        }
        
        if (targetTab) {
            document.querySelector(`.menu-item[data-tab="${targetTab}"]`).click();
        }
    });
});

// 查看活动详情
document.querySelectorAll('.btn-view-detail').forEach(btn => {
    btn.addEventListener('click', () => {
        showToast('正在加载活动详情...');
        setTimeout(() => {
            window.location.href = 'detail.html?type=activity&id=1';
        }, 500);
    });
});

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

// 添加样式
const style = document.createElement('style');
style.textContent = `
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
`;
document.head.appendChild(style);

// 页面加载动画
document.addEventListener('DOMContentLoaded', () => {
    // 统计数字动画
    document.querySelectorAll('.stat-number').forEach(stat => {
        const target = stat.textContent;
        const number = parseFloat(target.replace(/[^\d.]/g, ''));
        const suffix = target.replace(/[\d.]/g, '');
        let current = 0;
        const increment = number / 50;
        const timer = setInterval(() => {
            current += increment;
            if (current >= number) {
                stat.textContent = target;
                clearInterval(timer);
            } else {
                stat.textContent = Math.floor(current) + suffix;
            }
        }, 20);
    });
});


