// ===== Custom Cursor (design.google style) =====
(function () {
  function initCustomCursor() {
    const finePointer = window.matchMedia('(hover: hover) and (pointer: fine)');
    if (!finePointer.matches) return;

    document.documentElement.classList.add('custom-cursor');

    const dot = document.createElement('div');
    dot.className = 'cursor-dot';
    dot.setAttribute('aria-hidden', 'true');
    document.body.appendChild(dot);

    const FILL_TARGETS = 'a, button';
    const GROW_TARGETS = '.feature-card, .timeline-dot';
    const CURSOR_STATES = ['is-hover'];

    let pointerX = 0;
    let pointerY = 0;
    let fillTarget = null;
    let fillRaf = 0;
    let fillRadius = '12px';

    function updatePosition(x, y) {
      pointerX = x;
      pointerY = y;
      dot.style.setProperty('--cursor-x', x + 'px');
      dot.style.setProperty('--cursor-y', y + 'px');
    }

    function radiusFor(el) {
      const radius = getComputedStyle(el).borderRadius;
      if (radius === '0px') return el.tagName === 'A' ? '999px' : radius;
      return radius;
    }

    function setState(state) {
      CURSOR_STATES.forEach((name) =>
        dot.classList.toggle(name, name === state),
      );
    }

    function trackFill() {
      if (!fillTarget) return;
      const rect = fillTarget.getBoundingClientRect();
      dot.style.left = rect.left + 'px';
      dot.style.top = rect.top + 'px';
      dot.style.width = rect.width + 'px';
      dot.style.height = rect.height + 'px';
      dot.style.borderRadius = fillRadius;
      dot.style.transform = 'translate3d(0, 0, 0)';
      fillRaf = requestAnimationFrame(trackFill);
    }

    function stopFill() {
      if (!fillTarget && !fillRaf) return;
      fillTarget = null;
      cancelAnimationFrame(fillRaf);
      fillRaf = 0;
      dot.classList.remove('is-fill');
      dot.style.left = '';
      dot.style.top = '';
      dot.style.width = '';
      dot.style.height = '';
      dot.style.borderRadius = '';
      dot.style.transform = '';
    }

    function startFill(el) {
      if (fillTarget === el) return;
      stopFill();
      fillTarget = el;
      fillRadius = radiusFor(el);
      dot.classList.add('is-fill');
      const prevTransition = dot.style.transition;
      dot.style.transition = 'none';
      dot.style.transform = 'translate3d(0, 0, 0)';
      dot.style.left = pointerX + 'px';
      dot.style.top = pointerY + 'px';
      dot.style.width = '24px';
      dot.style.height = '24px';
      void dot.offsetWidth;
      dot.style.transition = prevTransition;
      trackFill();
    }

    function stateFor(el) {
      if (!el || !el.closest) return { state: '' };
      if (el.closest(FILL_TARGETS)) {
        return { state: 'is-fill', target: el.closest(FILL_TARGETS) };
      }
      if (el.closest(GROW_TARGETS)) return { state: 'is-hover' };
      return { state: '' };
    }

    function applyState(info) {
      if (info.state === 'is-fill') {
        startFill(info.target);
      } else {
        stopFill();
        setState(info.state);
      }
    }

    function resetCursor() {
      applyState({ state: '' });
    }

    window.addEventListener('pointermove', (e) => {
      if (e.pointerType !== 'mouse') return;
      updatePosition(e.clientX, e.clientY);
    });

    document.addEventListener('mouseover', (e) => {
      applyState(stateFor(e.target));
    });

    document.addEventListener('mouseout', (e) => {
      applyState(stateFor(e.relatedTarget));
    });

    document.addEventListener('mouseleave', () => {
      stopFill();
      dot.classList.add('is-away');
    });

    document.addEventListener('mouseenter', () => {
      dot.classList.remove('is-away');
    });

    document.addEventListener('click', resetCursor);

    window.addEventListener(
      'scroll',
      () => {
        if (fillTarget && getComputedStyle(fillTarget).position !== 'fixed') {
          resetCursor();
        }
      },
      { passive: true },
    );
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCustomCursor);
  } else {
    initCustomCursor();
  }
})();
