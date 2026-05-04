// 登录页面交互功能

// 标签切换
const tabBtns = document.querySelectorAll('.tab-btn');
const tabContents = document.querySelectorAll('.tab-content');

tabBtns.forEach(btn => {
    btn.addEventListener('click', function() {
        // 移除所有active类
        tabBtns.forEach(b => b.classList.remove('active'));
        tabContents.forEach(c => c.classList.remove('active'));
        
        // 添加active类
        this.classList.add('active');
        const tabId = this.dataset.tab + '-tab';
        document.getElementById(tabId).classList.add('active');
    });
});

// 密码显示/隐藏
const togglePasswordBtn = document.querySelector('.toggle-password');
const passwordInput = document.getElementById('password');

togglePasswordBtn?.addEventListener('click', function() {
    const icon = this.querySelector('i');
    if (passwordInput.type === 'password') {
        passwordInput.type = 'text';
        icon.classList.remove('fa-eye');
        icon.classList.add('fa-eye-slash');
    } else {
        passwordInput.type = 'password';
        icon.classList.remove('fa-eye-slash');
        icon.classList.add('fa-eye');
    }
});

// 发送验证码
const sendSmsBtn = document.getElementById('sendSmsBtn');
let countdown = 60;
let timer = null;

sendSmsBtn?.addEventListener('click', function() {
    const phone = document.getElementById('phone').value;
    
    // 验证手机号
    if (!phone) {
        showToast('请输入手机号');
        return;
    }
    
    if (!/^1[3-9]\d{9}$/.test(phone)) {
        showToast('请输入正确的手机号');
        return;
    }
    
    // 开始倒计时
    this.disabled = true;
    countdown = 60;
    
    const updateCountdown = () => {
        if (countdown > 0) {
            sendSmsBtn.textContent = `${countdown}秒后重试`;
            countdown--;
        } else {
            sendSmsBtn.textContent = '发送验证码';
            sendSmsBtn.disabled = false;
            clearInterval(timer);
        }
    };
    
    updateCountdown();
    timer = setInterval(updateCountdown, 1000);
    
    // 模拟发送验证码
    showToast('验证码已发送');
});

// 表单提交
const loginForm = document.getElementById('loginForm');

loginForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    const activeTab = document.querySelector('.tab-btn.active').dataset.tab;
    
    if (activeTab === 'password') {
        // 密码登录
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        
        if (!username) {
            showToast('请输入手机号或学号');
            return;
        }
        
        if (!password) {
            showToast('请输入密码');
            return;
        }
        
        if (password.length < 6) {
            showToast('密码长度不能少于6位');
            return;
        }
        
        // 执行登录
        performLogin({ username, password, type: 'password' });
        
    } else if (activeTab === 'sms') {
        // 短信登录
        const phone = document.getElementById('phone').value;
        const smsCode = document.getElementById('smsCode').value;
        
        if (!phone) {
            showToast('请输入手机号');
            return;
        }
        
        if (!/^1[3-9]\d{9}$/.test(phone)) {
            showToast('请输入正确的手机号');
            return;
        }
        
        if (!smsCode) {
            showToast('请输入验证码');
            return;
        }
        
        if (smsCode.length !== 6) {
            showToast('请输入6位验证码');
            return;
        }
        
        // 执行登录
        performLogin({ phone, smsCode, type: 'sms' });
    }
});

// 执行登录
function performLogin(data) {
    const btnLogin = document.querySelector('.btn-login');
    const btnText = btnLogin.querySelector('span');
    const btnIcon = btnLogin.querySelector('i');
    
    // 显示加载状态
    btnLogin.classList.add('loading');
    btnText.textContent = '登录中';
    btnIcon.className = 'fas fa-spinner';
    
    // 模拟登录请求
    setTimeout(() => {
        // 模拟登录成功
        console.log('登录数据:', data);
        
        // 保存登录状态
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('loginTime', new Date().toISOString());
        
        // 显示成功消息
        showToast('登录成功！');
        
        // 跳转到首页
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1000);
        
    }, 2000);
}

// 第三方登录
document.querySelectorAll('.social-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        const platform = this.classList.contains('wechat') ? '微信' :
                        this.classList.contains('qq') ? 'QQ' : '微博';
        showToast(`正在跳转${platform}登录...`);
        
        // 模拟第三方登录
        setTimeout(() => {
            showToast('第三方登录功能开发中');
        }, 1000);
    });
});

// 忘记密码
document.querySelector('.forgot-link')?.addEventListener('click', (e) => {
    e.preventDefault();
    showModal('忘记密码', `
        <p>请联系管理员重置密码</p>
        <p>邮箱：support@blueshanbi.com</p>
        <p>电话：400-123-4567</p>
    `);
});

// 提示消息
function showToast(message) {
    // 移除现有toast
    const existingToast = document.querySelector('.toast');
    if (existingToast) {
        existingToast.remove();
    }
    
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

// 显示模态框
function showModal(title, content) {
    const modal = document.createElement('div');
    modal.className = 'info-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>${title}</h3>
                <button class="btn-close-modal">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="modal-body">
                ${content}
            </div>
            <div class="modal-footer">
                <button class="btn-modal-confirm">确定</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    setTimeout(() => modal.classList.add('show'), 10);
    
    // 关闭模态框
    const closeModal = () => {
        modal.classList.remove('show');
        setTimeout(() => modal.remove(), 300);
    };
    
    modal.querySelector('.btn-close-modal').addEventListener('click', closeModal);
    modal.querySelector('.btn-modal-confirm').addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
}

// 添加样式
const style = document.createElement('style');
style.textContent = `
    .toast {
        position: fixed;
        top: 30px;
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
    
    .info-modal {
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
    
    .info-modal.show {
        opacity: 1;
    }
    
    .modal-content {
        background: white;
        border-radius: 12px;
        width: 90%;
        max-width: 400px;
        overflow: hidden;
    }
    
    .modal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 25px;
        border-bottom: 1px solid var(--border-color);
    }
    
    .modal-header h3 {
        font-size: 18px;
        color: var(--text-primary);
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
    
    .modal-body {
        padding: 25px;
        color: var(--text-secondary);
        line-height: 1.8;
    }
    
    .modal-body p {
        margin-bottom: 10px;
    }
    
    .modal-footer {
        padding: 20px 25px;
        border-top: 1px solid var(--border-color);
        display: flex;
        justify-content: flex-end;
    }
    
    .btn-modal-confirm {
        padding: 10px 30px;
        border: none;
        background: var(--primary-color);
        color: white;
        border-radius: 8px;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s;
    }
    
    .btn-modal-confirm:hover {
        background: var(--primary-dark);
    }
`;
document.head.appendChild(style);

// 页面加载动画
document.addEventListener('DOMContentLoaded', () => {
    // 检查是否已登录
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    if (isLoggedIn === 'true') {
        const loginTime = localStorage.getItem('loginTime');
        const hoursPassed = (new Date() - new Date(loginTime)) / (1000 * 60 * 60);
        
        // 如果登录时间超过24小时，清除登录状态
        if (hoursPassed > 24) {
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('loginTime');
        } else {
            // 自动跳转到首页
            showToast('已登录，正在跳转...');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
        }
    }
});


