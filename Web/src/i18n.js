const translations = {
    zh: {
        'error.heading': '抱歉，页面走丢了~',
        'error.message': '您访问的页面可能已被移除。',
        'error.home': '返回首页',
        'error.refresh': '刷新页面'
    },
    en: {
        'error.heading': 'Not Found',
        'error.message': 'Sorry, The page you visited may have been removed.',
        'error.home': 'Back to Home',
        'error.refresh': 'Refresh'
    }
};

function changeLanguage(lang) {
    // If language is not supported, use English
    const supportedLang = translations[lang] ? lang : 'en';
    document.documentElement.setAttribute('lang', supportedLang);
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        element.textContent = translations[supportedLang][key];
    });
}

// Get system language
const systemLang = navigator.language.toLowerCase().split('-')[0];
changeLanguage(systemLang);

// Detect system theme mode
function detectColorScheme() {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        document.body.classList.add('dark-mode');
    }
}

// Listen for system theme changes
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
    document.body.classList.toggle('dark-mode', e.matches);
});

// Initialize theme
detectColorScheme(); 