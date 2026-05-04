// 注册页面交互功能

// 密码显示/隐藏
document.querySelectorAll('.toggle-password').forEach(btn => {
    btn.addEventListener('click', function() {
        const input = this.parentElement.querySelector('input');
        const icon = this.querySelector('i');
        
        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('fa-eye');
            icon.classList.add('fa-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('fa-eye-slash');
            icon.classList.add('fa-eye');
        }
    });
});

// 发送验证码
const regSendSmsBtn = document.getElementById('regSendSmsBtn');
let countdown = 60;
let timer = null;

regSendSmsBtn.addEventListener('click', function() {
    const phone = document.getElementById('regPhone').value;
    
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
            regSendSmsBtn.textContent = `${countdown}秒后重试`;
            countdown--;
        } else {
            regSendSmsBtn.textContent = '发送验证码';
            regSendSmsBtn.disabled = false;
            clearInterval(timer);
        }
    };
    
    updateCountdown();
    timer = setInterval(updateCountdown, 1000);
    
    // 模拟发送验证码
    showToast('验证码已发送');
});

// 实时密码强度检测
const regPassword = document.getElementById('regPassword');
let strengthIndicator = null;

regPassword.addEventListener('input', function() {
    const password = this.value;
    
    // 创建强度指示器
    if (!strengthIndicator) {
        strengthIndicator = document.createElement('div');
        strengthIndicator.className = 'password-strength';
        strengthIndicator.innerHTML = `
            <div class="strength-bar">
                <div class="strength-fill"></div>
            </div>
            <span class="strength-text"></span>
        `;
        this.parentElement.parentElement.appendChild(strengthIndicator);
    }
    
    // 计算密码强度
    let strength = 0;
    if (password.length >= 6) strength++;
    if (password.length >= 10) strength++;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++;
    if (/\d/.test(password)) strength++;
    if (/[^a-zA-Z0-9]/.test(password)) strength++;
    
    const fill = strengthIndicator.querySelector('.strength-fill');
    const text = strengthIndicator.querySelector('.strength-text');
    
    if (password.length === 0) {
        fill.style.width = '0';
        text.textContent = '';
        fill.style.background = '';
    } else if (strength <= 1) {
        fill.style.width = '33%';
        fill.style.background = '#f44336';
        text.textContent = '弱';
        text.style.color = '#f44336';
    } else if (strength <= 3) {
        fill.style.width = '66%';
        fill.style.background = '#ff9800';
        text.textContent = '中';
        text.style.color = '#ff9800';
    } else {
        fill.style.width = '100%';
        fill.style.background = '#4caf50';
        text.textContent = '强';
        text.style.color = '#4caf50';
    }
});

// 表单提交
const registerForm = document.getElementById('registerForm');

registerForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    const phone = document.getElementById('regPhone').value;
    const smsCode = document.getElementById('regSmsCode').value;
    const nickname = document.getElementById('nickname').value;
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const school = document.getElementById('school').value;
    const agreeTerms = document.getElementById('agreeTerms').checked;
    
    // 验证手机号
    if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
        showToast('请输入正确的手机号');
        return;
    }
    
    // 验证验证码
    if (!smsCode || smsCode.length !== 6) {
        showToast('请输入6位验证码');
        return;
    }
    
    // 验证昵称
    if (!nickname || nickname.trim().length < 2) {
        showToast('昵称至少需要2个字符');
        return;
    }
    
    if (nickname.trim().length > 20) {
        showToast('昵称最多20个字符');
        return;
    }
    
    // 验证密码
    if (!password || password.length < 6) {
        showToast('密码长度不能少于6位');
        return;
    }
    
    if (password.length > 20) {
        showToast('密码长度不能超过20位');
        return;
    }
    
    // 验证确认密码
    if (password !== confirmPassword) {
        showToast('两次输入的密码不一致');
        return;
    }
    
    // 验证协议
    if (!agreeTerms) {
        showToast('请阅读并同意用户协议和隐私政策');
        return;
    }
    
    // 执行注册
    performRegister({
        phone,
        smsCode,
        nickname: nickname.trim(),
        password,
        school: school.trim()
    });
});

// 执行注册
function performRegister(data) {
    const btnRegister = document.querySelector('.btn-login');
    const btnText = btnRegister.querySelector('span');
    const btnIcon = btnRegister.querySelector('i');
    
    // 显示加载状态
    btnRegister.classList.add('loading');
    btnText.textContent = '注册中';
    btnIcon.className = 'fas fa-spinner';
    
    // 模拟注册请求
    setTimeout(() => {
        console.log('注册数据:', data);
        
        // 模拟注册成功
        showSuccessModal();
        
    }, 2000);
}

// 显示注册成功弹窗
function showSuccessModal() {
    const modal = document.createElement('div');
    modal.className = 'success-modal';
    modal.innerHTML = `
        <div class="success-modal-content">
            <div class="success-icon">
                <i class="fas fa-check-circle"></i>
            </div>
            <h3>注册成功！</h3>
            <p>欢迎加入校园蓝珊笔记社区</p>
            <p class="countdown-text">
                <span id="countdown">3</span> 秒后自动跳转到登录页面
            </p>
            <button class="btn-go-login" onclick="window.location.href='login.html'">
                立即登录
            </button>
        </div>
    `;
    
    document.body.appendChild(modal);
    setTimeout(() => modal.classList.add('show'), 10);
    
    // 倒计时跳转
    let count = 3;
    const countdownEl = modal.querySelector('#countdown');
    const timer = setInterval(() => {
        count--;
        if (count > 0) {
            countdownEl.textContent = count;
        } else {
            clearInterval(timer);
            window.location.href = 'login.html';
        }
    }, 1000);
}

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
    
    .password-strength {
        margin-top: 8px;
        display: flex;
        align-items: center;
        gap: 10px;
    }
    
    .strength-bar {
        flex: 1;
        height: 4px;
        background: var(--border-color);
        border-radius: 2px;
        overflow: hidden;
    }
    
    .strength-fill {
        height: 100%;
        width: 0;
        transition: all 0.3s;
    }
    
    .strength-text {
        font-size: 12px;
        font-weight: 600;
        min-width: 20px;
    }
    
    .success-modal {
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
    
    .success-modal.show {
        opacity: 1;
    }
    
    .success-modal-content {
        background: white;
        border-radius: 16px;
        padding: 50px 40px;
        text-align: center;
        max-width: 400px;
    }
    
    .success-icon {
        font-size: 80px;
        color: var(--success-color);
        margin-bottom: 20px;
    }
    
    .success-modal-content h3 {
        font-size: 28px;
        margin-bottom: 10px;
        color: var(--text-primary);
    }
    
    .success-modal-content p {
        color: var(--text-secondary);
        margin-bottom: 15px;
        font-size: 15px;
    }
    
    .countdown-text {
        font-size: 14px;
        margin-bottom: 25px;
    }
    
    .countdown-text span {
        color: var(--primary-color);
        font-weight: 700;
        font-size: 18px;
    }
    
    .btn-go-login {
        width: 100%;
        padding: 14px;
        border: none;
        background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
        color: white;
        border-radius: 8px;
        font-size: 16px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s;
    }
    
    .btn-go-login:hover {
        transform: translateY(-2px);
        box-shadow: var(--shadow-md);
    }
    
    .form-options a {
        color: var(--primary-color);
        text-decoration: none;
    }
    
    .form-options a:hover {
        text-decoration: underline;
    }
`;
document.head.appendChild(style);


