(function () {
  const MD_BASE = 'assets/docs/privacy/privacy.';
  const FALLBACK_LANG = 'en';
  const FALLBACK_ERROR = 'Failed to load the policy content. Please try again later.';

  let requestSeq = 0;

  function errorText(lang) {
    if (typeof translations === 'object' && translations) {
      const data = translations[lang] || translations[FALLBACK_LANG];
      if (data && data['privacy.loadError']) {
        return data['privacy.loadError'];
      }
    }
    return FALLBACK_ERROR;
  }

  async function render(lang) {
    if (!lang) return;
    const seq = ++requestSeq;
    const box = document.querySelector('.policy-article');
    if (!box) return;

    try {
      const response = await fetch(MD_BASE + lang + '.md');
      if (!response.ok) throw new Error('HTTP ' + response.status);
      const markdown = await response.text();
      // Ignore the response if a newer language request has been issued
      if (seq !== requestSeq) return;
      box.innerHTML = marked.parse(markdown);
    } catch (error) {
      if (seq !== requestSeq) return;
      console.error('Error loading privacy policy:', error);
      if (lang !== FALLBACK_LANG) {
        render(FALLBACK_LANG);
        return;
      }
      box.innerHTML = '<p class="policy-error">' + errorText(lang) + '</p>';
    }
  }

  document.addEventListener('langchange', (event) => {
    render(event.detail && event.detail.lang);
  });
})();
