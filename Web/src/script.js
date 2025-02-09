document.addEventListener('DOMContentLoaded', () => {
    const themeDropdown = document.querySelector('.theme-dropdown');
    
    // Get system theme
    function getSystemTheme() {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }
    
    // Apply theme
    function applyTheme(theme) {
        const actualTheme = theme === 'system' ? getSystemTheme() : theme;
        document.documentElement.setAttribute('data-theme', actualTheme);
        document.documentElement.setAttribute('data-theme-setting', theme);
        
        // Update icon display
        updateThemeIcon(theme);
        
        // Update selected state
        updateSelectedTheme(theme);
    }
    
    // Update theme icon
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
    
    // Update selected state
    function updateSelectedTheme(theme) {
        // Remove all selected states first
        document.querySelectorAll('.theme-dropdown button').forEach(btn => {
            btn.removeAttribute('data-selected');
        });
        
        // Set selected state for current theme
        const selectedButton = document.querySelector(`.theme-dropdown button[data-theme="${theme}"]`);
        if (selectedButton) {
            selectedButton.setAttribute('data-selected', 'true');
        }
    }
    
    // Initialise theme
    const savedTheme = localStorage.getItem('theme') || 'system';
    applyTheme(savedTheme);
    
    // Listen for theme selection
    themeDropdown.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;
        
        const theme = button.dataset.theme;
        localStorage.setItem('theme', theme);
        applyTheme(theme);
    });
    
    // Listen for system theme changes
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
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

    // Language selection
    const langDropdown = document.querySelector('.lang-dropdown');
    
    // Initialize language
    const savedLang = localStorage.getItem('lang') || 'zh-CN';
    document.documentElement.setAttribute('lang', savedLang);
    updateSelectedLang(savedLang);
    
    function updateSelectedLang(lang) {
        // Remove all selected states first
        document.querySelectorAll('.lang-dropdown button').forEach(btn => {
            btn.removeAttribute('data-selected');
        });
        
        // Set selected state for current language
        const selectedButton = document.querySelector(`.lang-dropdown button[data-lang="${lang}"]`);
        if (selectedButton) {
            selectedButton.setAttribute('data-selected', 'true');
        }
    }
    
    // Listen for language selection
    langDropdown.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;
        
        const lang = button.dataset.lang;
        localStorage.setItem('lang', lang);
        document.documentElement.setAttribute('lang', lang);
        updateSelectedLang(lang);
        
        // Reload page to apply new language
        // window.location.reload();  // Uncomment this when i18n is implemented
    });
}); 