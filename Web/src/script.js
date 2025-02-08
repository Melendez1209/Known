document.addEventListener('DOMContentLoaded', () => {
    const themeDropdown = document.querySelector('.theme-dropdown');
    
    // 获取系统主题
    function getSystemTheme() {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    
    // 应用主题
    function applyTheme(theme) {
        const actualTheme = theme === 'system' ? getSystemTheme() : theme;
        document.documentElement.setAttribute('data-theme', actualTheme);
        document.documentElement.setAttribute('data-theme-setting', theme);
        
        // 更新图标显示
        updateThemeIcon(theme);
    }
    
    // 更新主题图标
    function updateThemeIcon(theme) {
        const darkIcon = document.querySelector('.dark-icon');
        const lightIcon = document.querySelector('.light-icon');
        const systemIcon = document.querySelector('.system-icon');
        
        darkIcon.style.display = 'none';
        lightIcon.style.display = 'none';
        systemIcon.style.display = 'none';
        
        if (theme === 'system') {
            systemIcon.style.display = 'inline-block';
        } else if (theme === 'dark') {
            darkIcon.style.display = 'inline-block';
        } else {
            lightIcon.style.display = 'inline-block';
        }
    }
    
    // 初始化主题
    const savedTheme = localStorage.getItem('theme') || 'system';
    applyTheme(savedTheme);
    
    // 监听主题选择
    themeDropdown.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;
        
        const theme = button.dataset.theme;
        localStorage.setItem('theme', theme);
        applyTheme(theme);
    });
    
    // 监听系统主题变化
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        const currentThemeSetting = document.documentElement.getAttribute('data-theme-setting');
        if (currentThemeSetting === 'system') {
            applyTheme('system');
        }
    });

    const downloadBtn = document.querySelector('.download-btn');

    downloadBtn.addEventListener('mouseenter', () => {
        const downloadText = downloadBtn.querySelector('.download-text');
        const versionText = downloadBtn.querySelector('.version-text');

        downloadText.style.opacity = '0';
        versionText.style.opacity = '1';
    });

    downloadBtn.addEventListener('mouseleave', () => {
        const downloadText = downloadBtn.querySelector('.download-text');
        const versionText = downloadBtn.querySelector('.version-text');
        
        downloadText.style.opacity = '1';
        versionText.style.opacity = '0';
    });
}); 