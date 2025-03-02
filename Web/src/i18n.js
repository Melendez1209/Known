let translations = {};

// Load translations from translations.json
fetch('/assets/values/translations.json')
  .then((response) => response.json())
  .then((data) => {
    translations = data;
    // Initialize language settings
    const systemLang = navigator.language.toLowerCase();
    changeLanguage(systemLang);
    createLanguageSwitcher();
  })
  .catch((error) => console.error('Error loading translations:', error));

const LANGUAGE_MAPPINGS = {
  'zh-cn': 'zh',
  'zh-hk': 'zh-tw',
  'zh-tw': 'zh-tw',
  'en-gb': 'en-GB',
  'en-us': 'en'
};

function changeLanguage(lang) {
  const normalizedLang = lang.toLowerCase();
  const supportedLang = LANGUAGE_MAPPINGS[normalizedLang] || 
                       (translations[normalizedLang] ? normalizedLang : 'en');

  document.documentElement.setAttribute('lang', supportedLang);
  document.querySelectorAll('[data-i18n]').forEach((element) => {
    const key = element.getAttribute('data-i18n');
    element.textContent = translations[supportedLang][key];
  });
}

// Detect system theme mode
function detectColorScheme() {
  if (
    window.matchMedia &&
    window.matchMedia('(prefers-color-scheme: dark)').matches
  ) {
    document.body.classList.add('dark-mode');
  }
}

// Listen for system theme changes
window
  .matchMedia('(prefers-color-scheme: dark)')
  .addEventListener('change', (e) => {
    document.body.classList.toggle('dark-mode', e.matches);
  });

// Initialize theme
detectColorScheme();

// Add language switcher
const languageOptions = {
  'zh': '简体中文',
  'zh-tw': '繁體中文',
  'en': 'English (US)',
  'en-GB': 'English (UK)'
};

function createLanguageSwitcher() {
  // 移除旧的实现
  const existingSelector = document.querySelector('.language-selector');
  if (existingSelector) {
    existingSelector.remove();
  }

  // 为每个语言按钮添加点击事件
  document.querySelectorAll('.lang-dropdown button').forEach(button => {
    const lang = button.getAttribute('data-lang');
    button.addEventListener('click', () => {
      changeLanguage(lang);
      // 更新选中状态
      document.querySelectorAll('.lang-dropdown button').forEach(btn => {
        btn.setAttribute('data-selected', btn.getAttribute('data-lang') === lang);
      });
    });
  });
}
