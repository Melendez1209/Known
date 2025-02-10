//TIP With Search Everywhere, you can find any action, file, or symbol in your project. Press <shortcut actionId="Shift"/> <shortcut actionId="Shift"/>, type in <b>terminal</b>, and press <shortcut actionId="EditorEnter"/>. Then run <shortcut raw="npm run dev"/> in the terminal and click the link in its output to open the app in the browser.
import locales from './utils/locales.js';

export function setupCounter(element) {
  //TIP Try <shortcut actionId="GotoDeclaration"/> on <shortcut raw="counter"/> to see its usages. You can also use this shortcut to jump to a declaration – try it on <shortcut raw="counter"/> on line 13.
  let counter = 0;

  const adjustCounterValue = (value) => {
    if (value >= 100) return value - 100;
    if (value <= -100) return value + 100;
    return value;
  };

  const setCounter = (value) => {
    counter = adjustCounterValue(value);
    //TIP WebStorm has lots of inspections to help you catch issues in your project. It also has quick fixes to help you resolve them. Press <shortcut actionId="ShowIntentionActions"/> on <shortcut raw="text"/> and choose <b>Inline variable</b> to clean up the redundant code.
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

  //TIP Let's see how to review and commit your changes. Press <shortcut actionId="GotoAction"/> and look for <b>commit</b>. Try checking the diff for a file – double-click main.js to do that.
  setCounter(0);
}

//TIP To find text strings in your project, you can use the <shortcut actionId="FindInPath"/> shortcut. Press it and type in <b>counter</b> – you'll get all matches in one place.
setupCounter(document.getElementById('counter-value'));

//TIP There's much more in WebStorm to help you be more productive. Press <shortcut actionId="Shift"/> <shortcut actionId="Shift"/> and search for <b>Learn WebStorm</b> to open our learning hub with more things for you to try.

function getDefaultLocale() {
    // Getting Saved Language Settings
    const savedLocale = localStorage.getItem('lang');
    if (savedLocale && locales[savedLocale]) {
        return savedLocale;
    }
    
    // Get system language
    const userLanguage = navigator.language;
    
    // Check for direct matching of supported languages
    if (locales[userLanguage]) {
        return userLanguage;
    }
    
    // Check the base language section for matches
    const baseLanguage = userLanguage.split('-')[0];
    const matchingLocales = Object.keys(locales).filter(locale => 
        locale.startsWith(baseLanguage)
    );
    
    // If a matching language is found, return the first match
    if (matchingLocales.length > 0) {
        return matchingLocales[0];
    }
    
    // If there is no matching language, American English is used by default
    return 'en-US';
}

// Language Switching Handling Functions
function updateLanguage(locale) {
    document.documentElement.lang = locale;
    const translations = locales[locale];
    
    // Update all elements with data-i18n attribute
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const keys = element.getAttribute('data-i18n').split('.');
        let value = translations;
        for (const key of keys) {
            value = value[key];
        }
        if (value) {
            element.textContent = value;
        }
    });

    // Updating page titles
    document.title = translations.appName || '知学情';

    // Updated icon text for theme switch buttons
    document.querySelectorAll('.theme-dropdown button').forEach(button => {
        const themeKey = button.getAttribute('data-theme');
        if (themeKey && translations.theme[themeKey]) {
            const icon = button.querySelector('i');
            const text = document.createTextNode(translations.theme[themeKey]);
            button.innerHTML = '';
            if (icon) button.appendChild(icon);
            button.appendChild(text);
        }
    });
}

// Initialise language settings
document.addEventListener('DOMContentLoaded', () => {
    const defaultLocale = getDefaultLocale();
    updateLanguage(defaultLocale);
    
    // If this is the first visit (no saved language settings), save the currently used language to localStorage
    if (!localStorage.getItem('lang')) {
        localStorage.setItem('lang', defaultLocale);
    }
    
    // Listen for language switching button clicks
    document.querySelector('.lang-dropdown').addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;
        
        const newLocale = button.getAttribute('data-lang');
        if (newLocale) {
            localStorage.setItem('lang', newLocale);
            updateLanguage(newLocale);
            
            // Update selected state
            document.querySelectorAll('.lang-dropdown button').forEach(btn => {
                btn.removeAttribute('data-selected');
            });
            button.setAttribute('data-selected', 'true');
        }
    });
});
