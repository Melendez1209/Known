document.addEventListener('DOMContentLoaded', () => {
    // ===== Theme System =====
    const themeDropdown = document.querySelector('.theme-dropdown');

    function getSystemTheme() {
        return window.matchMedia('(prefers-color-scheme: dark)').matches
            ? 'dark'
            : 'light';
    }

    function applyTheme(theme) {
        const actualTheme = theme === 'system' ? getSystemTheme() : theme;
        document.documentElement.setAttribute('data-theme', actualTheme);
        document.documentElement.setAttribute('data-theme-setting', theme);
        updateThemeIcon(theme);
        updateSelectedTheme(theme);
    }

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

    function updateSelectedTheme(theme) {
        document.querySelectorAll('.theme-dropdown button').forEach((btn) => {
            btn.removeAttribute('data-selected');
        });
        const selectedButton = document.querySelector(
            `.theme-dropdown button[data-theme="${theme}"]`
        );
        if (selectedButton) {
            selectedButton.setAttribute('data-selected', 'true');
        }
    }

    const savedTheme = localStorage.getItem('theme') || 'system';
    applyTheme(savedTheme);

    themeDropdown.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;
        const theme = button.dataset.theme;
        localStorage.setItem('theme', theme);
        applyTheme(theme);
    });

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
        const currentThemeSetting = document.documentElement.getAttribute(
            'data-theme-setting'
        );
        if (currentThemeSetting === 'system') {
            applyTheme('system');
        }
    });

    // ===== Language Selection =====
    const langDropdown = document.querySelector('.lang-dropdown');
    const savedLang = localStorage.getItem('lang') || 'zh';
    updateSelectedLang(savedLang);

    function updateSelectedLang(lang) {
        document.querySelectorAll('.lang-dropdown button').forEach((btn) => {
            btn.removeAttribute('data-selected');
        });
        const selectedButton = document.querySelector(
            `.lang-dropdown button[data-lang="${lang}"]`
        );
        if (selectedButton) {
            selectedButton.setAttribute('data-selected', 'true');
        }
    }

    // ===== Particle System =====
    function createParticles() {
        const container = document.getElementById('particles');
        if (!container) return;

        const particleCount = window.innerWidth < 768 ? 15 : 30;

        for (let i = 0; i < particleCount; i++) {
            const particle = document.createElement('div');
            particle.classList.add('particle');
            const size = Math.random() * 10 + 4;
            particle.style.width = size + 'px';
            particle.style.height = size + 'px';
            particle.style.left = Math.random() * 100 + '%';
            particle.style.animationDuration = Math.random() * 10 + 8 + 's';
            particle.style.animationDelay = Math.random() * 10 + 's';
            container.appendChild(particle);
        }
    }
    createParticles();

    // ===== Scroll Reveal (Intersection Observer) =====
    const revealElements = document.querySelectorAll('.scroll-reveal');

    const revealObserver = new IntersectionObserver(
        (entries) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    const delay = entry.target.dataset.delay || 0;
                    setTimeout(() => {
                        entry.target.classList.add('revealed');
                    }, parseInt(delay));
                    revealObserver.unobserve(entry.target);
                }
            });
        },
        {
            threshold: 0.15,
            rootMargin: '0px 0px -50px 0px',
        }
    );

    revealElements.forEach((el) => revealObserver.observe(el));

    // ===== Active Nav Link on Scroll =====
    const sections = document.querySelectorAll('section[id]');
    const navLinks = document.querySelectorAll('.nav-link');

    function updateActiveNav() {
        const scrollY = window.scrollY + 100;

        sections.forEach((section) => {
            const sectionTop = section.offsetTop;
            const sectionHeight = section.offsetHeight;
            const sectionId = section.getAttribute('id');

            if (scrollY >= sectionTop && scrollY < sectionTop + sectionHeight) {
                navLinks.forEach((link) => {
                    link.classList.remove('active');
                    if (link.getAttribute('href') === '#' + sectionId) {
                        link.classList.add('active');
                    }
                });
            }
        });
    }

    // ===== Nav Shadow on Scroll =====
    const nav = document.querySelector('nav');

    function updateNavStyle() {
        if (window.scrollY > 50) {
            nav.classList.add('scrolled');
        } else {
            nav.classList.remove('scrolled');
        }
    }

    // ===== Parallax Effect for Hero =====
    const hero = document.querySelector('.hero');

    function updateParallax() {
        if (!hero) return;
        const scrollY = window.scrollY;
        const heroHeight = hero.offsetHeight;

        if (scrollY < heroHeight) {
            const heroContent = hero.querySelector('.hero-content');
            if (heroContent) {
                heroContent.style.transform = `translateY(${scrollY * 0.3}px)`;
                heroContent.style.opacity = 1 - scrollY / heroHeight;
            }
        }
    }

    // ===== Smooth Scroll for Nav Links =====
    navLinks.forEach((link) => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            if (targetSection) {
                const offsetTop = targetSection.offsetTop - 60;
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth',
                });
            }
        });
    });

    // CTA buttons smooth scroll
    document.querySelectorAll('.cta-primary, .cta-secondary').forEach((btn) => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = btn.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            if (targetSection) {
                const offsetTop = targetSection.offsetTop - 60;
                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth',
                });
            }
        });
    });

    // ===== Scroll Event Handler (throttled) =====
    let ticking = false;

    window.addEventListener('scroll', () => {
        if (!ticking) {
            window.requestAnimationFrame(() => {
                updateActiveNav();
                updateNavStyle();
                updateParallax();
                ticking = false;
            });
            ticking = true;
        }
    });

    // Initial calls
    updateActiveNav();
    updateNavStyle();
});
