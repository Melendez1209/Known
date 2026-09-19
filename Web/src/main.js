export function setupCounter(element) {
  let counter = 0;

  const adjustCounterValue = (value) => {
    if (value >= 100) return value - 100;
    if (value <= -100) return value + 100;
    return value;
  };

  const setCounter = (value) => {
    counter = adjustCounterValue(value);
    element.innerHTML = `${counter}`;
  };

  document
    .getElementById('increaseByOne')
    .addEventListener('click', () => setCounter(counter + 1));
  document
    .getElementById('decreaseByOne')
    .addEventListener('click', () => setCounter(counter - 1));
  document
    .getElementById('increaseByTwo')
    .addEventListener('click', () => setCounter(counter + 2));
  //TIP In the app running in the browser, you'll find that clicking <b>-2</b> doesn't work. To fix that, rewrite it using the code from lines 19 - 21 as examples of the logic.
  document.getElementById('decreaseByTwo');

  setCounter(0);
}

setupCounter(document.getElementById('counter-value'));

document.addEventListener('DOMContentLoaded', () => {
  const themeToggle = document.querySelector('.theme-toggle');
  const themeDropdown = document.querySelector('.theme-dropdown');

  if (themeToggle && themeDropdown) {
    themeToggle.addEventListener('click', () => {
      themeDropdown.classList.toggle('show');
    });

    themeDropdown.addEventListener('click', (e) => {
      const button = e.target.closest('button');
      if (!button) return;

      const newTheme = button.getAttribute('data-theme');
      if (newTheme) {
        document.documentElement.setAttribute('data-theme', newTheme);
        localStorage.setItem('theme', newTheme);
      }
    });
  }
});
