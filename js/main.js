// 模拟笔记数据 - 使用高质量在线图片
const notesData = [
    {
        id: 1,
        title: '新生必看！校园生活完全指南📚',
        image: 'https://picsum.photos/seed/campus-books/400/500',
        author: '学长学姐说',
        avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=学长&backgroundColor=667eea',
        likes: 1234,
        tags: ['新生指南', '校园生活']
    },
    {
        id: 2,
        title: '🏸羽毛球社招新啦！零基础也能加入我们',
        image: 'https://picsum.photos/seed/badminton-club/400/500',
        author: '羽毛球社官方',
        avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35',
        likes: 2341,
        tags: ['社团招新', '羽毛球']
    },
    {
        id: 3,
        title: '📢【通知】本周六羽毛球社友谊赛，欢迎观战！',
        image: 'https://picsum.photos/seed/badminton-match/400/500',
        author: '羽毛球社官方',
        avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35',
        likes: 1567,
        tags: ['友谊赛', '活动通知']
    },
    {
        id: 4,
        title: '🎉羽毛球社团建活动精彩回顾｜汗水与欢笑并存',
        image: 'https://picsum.photos/seed/team-building/400/500',
        author: '羽毛球社官方',
        avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35',
        likes: 3210,
        tags: ['团建活动', '社团生活']
    },
    {
        id: 5,
        title: '🏆校园羽毛球赛冠军专访：训练心得大公开',
        image: 'https://picsum.photos/seed/badminton-champion/400/500',
        author: '羽毛球社官方',
        avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=羽球社&backgroundColor=FF6B35',
        likes: 2890,
        tags: ['比赛', '经验分享']
    },
    {
        id: 6,
        title: '食堂隐藏美食大盘点！第三食堂绝了🍜',
        image: 'https://picsum.photos/seed/chinese-food/400/500',
        author: '吃货小王',
        avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=小王&backgroundColor=f093fb',
        likes: 3456,
        tags: ['美食', '食堂']
    },
    {
        id: 7,
        title: '考研经验分享：从二本到985的逆袭之路💪',
        image: 'https://picsum.photos/seed/study-success/400/500',
        author: '上岸学长',
        avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=上岸&backgroundColor=fa709a',
        likes: 5678,
        tags: ['考研', '经验分享']
    }
];

// 生成头像SVG（备用方案）
function generateAvatarSVG(name, color) {
    const initial = name.charAt(0);
    return `data:image/svg+xml,${encodeURIComponent(`
        <svg width="128" height="128" xmlns="http://www.w3.org/2000/svg">
            <rect width="128" height="128" fill="${color}"/>
            <text x="64" y="80" font-size="48" text-anchor="middle" fill="white" font-family="Arial, sans-serif">${initial}</text>
        </svg>
    `)}`;
}

// 渲染笔记卡片
function renderNotes(notes) {
    const waterfall = document.getElementById('waterfall');
    
    notes.forEach(note => {
        const card = document.createElement('div');
        card.className = 'note-card';
        card.onclick = () => goToDetail(note.id);
        
        card.innerHTML = `
            <img src="${note.image}" alt="${note.title}" class="note-image" onerror="handleImageError(this)">
            <div class="note-content">
                <h3 class="note-title">${note.title}</h3>
                <div class="note-tags">
                    ${note.tags.map(tag => `<span class="note-tag">#${tag}</span>`).join('')}
                </div>
                <div class="note-author">
                    <div class="author-info">
                        <img src="${note.avatar}" alt="${note.author}" class="author-avatar" onerror="handleAvatarError(this)">
                        <span class="author-name">${note.author}</span>
                    </div>
                    <div class="note-stats">
                        <i class="fas fa-heart"></i>
                        <span>${formatNumber(note.likes)}</span>
                    </div>
                </div>
            </div>
        `;
        
        waterfall.appendChild(card);
    });
}

// 处理图片加载错误
function handleImageError(img) {
    img.onerror = null; // 防止无限循环
    
    // 获取笔记标题以生成相关的占位图
    const card = img.closest('.note-card');
    const title = card ? card.querySelector('.note-title')?.textContent : '';
    
    // 根据标题生成主题色
    const gradients = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
    ];
    
    const randomGradient = gradients[Math.floor(Math.random() * gradients.length)];
    
    // 创建一个美观的占位图
    const placeholder = document.createElement('div');
    placeholder.className = 'note-image-placeholder';
    placeholder.style.cssText = `
        width: 100%;
        height: 300px;
        background: ${randomGradient};
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 16px;
        border-radius: 8px 8px 0 0;
        gap: 15px;
        position: relative;
        overflow: hidden;
    `;
    
    // 添加背景图案
    placeholder.innerHTML = `
        <div style="position: absolute; top: 0; left: 0; right: 0; bottom: 0; opacity: 0.1;">
            <div style="width: 200%; height: 200%; background: radial-gradient(circle, white 1px, transparent 1px); background-size: 30px 30px;"></div>
        </div>
        <i class="fas fa-image" style="font-size: 64px; opacity: 0.9; position: relative; z-index: 1;"></i>
        <div style="font-size: 16px; font-weight: 500; opacity: 0.95; position: relative; z-index: 1;">精彩内容</div>
    `;
    
    img.replaceWith(placeholder);
}

// 处理头像加载错误
function handleAvatarError(img) {
    img.onerror = null;
    
    // 获取作者名字生成个性化头像
    const authorName = img.closest('.author-info')?.querySelector('.author-name')?.textContent || '用';
    const initial = authorName.charAt(0);
    
    // 使用彩色渐变背景作为默认头像
    const colors = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
    ];
    
    // 根据名字首字符选择颜色
    const colorIndex = initial.charCodeAt(0) % colors.length;
    const selectedColor = colors[colorIndex];
    
    const placeholder = document.createElement('div');
    placeholder.className = 'author-avatar';
    placeholder.style.cssText = `
        width: 32px;
        height: 32px;
        border-radius: 50%;
        background: ${selectedColor};
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 16px;
        font-weight: 600;
        flex-shrink: 0;
    `;
    placeholder.textContent = initial;
    
    img.replaceWith(placeholder);
}

// 格式化数字
function formatNumber(num) {
    if (num >= 10000) {
        return (num / 10000).toFixed(1) + 'w';
    } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
}

// 跳转到详情页
function goToDetail(id) {
    window.location.href = `detail.html?id=${id}`;
}

// 回到顶部功能
const backToTopBtn = document.getElementById('backToTop');

window.addEventListener('scroll', () => {
    if (window.pageYOffset > 300) {
        backToTopBtn.classList.add('show');
    } else {
        backToTopBtn.classList.remove('show');
    }
});

backToTopBtn.addEventListener('click', () => {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
});

// 分类筛选功能
document.querySelectorAll('.category-item').forEach(item => {
    item.addEventListener('click', function() {
        document.querySelectorAll('.category-item').forEach(i => i.classList.remove('active'));
        this.classList.add('active');
        
        // 这里可以根据分类加载不同的笔记
        console.log('切换分类:', this.textContent.trim());
    });
});

// 筛选按钮功能
document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        
        // 这里可以根据筛选条件加载不同的笔记
        console.log('切换筛选:', this.textContent);
    });
});

// 搜索功能
const searchInput = document.querySelector('.search-box input');
searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        const keyword = e.target.value.trim();
        if (keyword) {
            console.log('搜索关键词:', keyword);
            // 这里可以实现搜索功能
        }
    }
});

// 模拟无限滚动加载
let isLoading = false;
let page = 1;

window.addEventListener('scroll', () => {
    if (isLoading) return;
    
    const scrollHeight = document.documentElement.scrollHeight;
    const scrollTop = document.documentElement.scrollTop;
    const clientHeight = document.documentElement.clientHeight;
    
    if (scrollTop + clientHeight >= scrollHeight - 500) {
        loadMoreNotes();
    }
});

function loadMoreNotes() {
    isLoading = true;
    const loading = document.getElementById('loading');
    loading.classList.add('show');
    
    // 模拟加载延迟
    setTimeout(() => {
        // 复制现有笔记数据作为新数据
        const newNotes = notesData.map(note => ({
            ...note,
            id: note.id + page * 100
        }));
        
        renderNotes(newNotes);
        loading.classList.remove('show');
        isLoading = false;
        page++;
    }, 1000);
}

// 关注按钮功能
document.querySelectorAll('.btn-follow').forEach(btn => {
    btn.addEventListener('click', function(e) {
        e.stopPropagation();
        if (this.textContent === '关注') {
            this.textContent = '已关注';
            this.style.background = 'var(--primary-color)';
            this.style.color = 'white';
        } else {
            this.textContent = '关注';
            this.style.background = 'transparent';
            this.style.color = 'var(--primary-color)';
        }
    });
});

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    renderNotes(notesData);
    
    // 添加加载动画
    setTimeout(() => {
        document.querySelectorAll('.note-card').forEach((card, index) => {
            setTimeout(() => {
                card.style.opacity = '0';
                card.style.transform = 'translateY(20px)';
                setTimeout(() => {
                    card.style.transition = 'all 0.5s ease';
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, 50);
            }, index * 50);
        });
    }, 100);
});


