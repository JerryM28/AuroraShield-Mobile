/**
 * AuroraShield Mobile - Main App
 * Capacitor-based privacy browser
 */

import ShieldsEngine from './shields-engine.js';

class AuroraShieldApp {
  constructor() {
    this.shields = new ShieldsEngine();
    this.tabs = [];
    this.activeTabId = null;
    this.pageBlocked = 0;
    
    this.init();
  }

  async init() {
    this.bindElements();
    this.bindEvents();
    this.loadSettings();
    this.updateStats();
    
    // Create initial tab
    this.createTab('aurora://newtab');
    
    // Initialize Capacitor plugins if available
    await this.initCapacitor();
  }

  async initCapacitor() {
    if (typeof Capacitor !== 'undefined') {
      try {
        const { StatusBar } = await import('@capacitor/status-bar');
        await StatusBar.setBackgroundColor({ color: '#1a1a2e' });
        await StatusBar.setStyle({ style: 'DARK' });
      } catch (e) {
        console.log('StatusBar not available');
      }
    }
  }

  bindElements() {
    this.backBtn = document.getElementById('backBtn');
    this.forwardBtn = document.getElementById('forwardBtn');
    this.refreshBtn = document.getElementById('refreshBtn');
    this.urlInput = document.getElementById('urlInput');
    this.securityIcon = document.getElementById('securityIcon');
    this.shieldsBtn = document.getElementById('shieldsBtn');
    this.shieldsCount = document.getElementById('shieldsCount');
    this.menuBtn = document.getElementById('menuBtn');
    this.tabsContainer = document.getElementById('tabsContainer');
    this.newTabBtn = document.getElementById('newTabBtn');
    this.browserContent = document.getElementById('browserContent');
    this.welcomePage = document.getElementById('welcomePage');
    this.settingsPage = document.getElementById('settingsPage');
    this.webviewFrame = document.getElementById('webviewFrame');
    this.shieldsPanel = document.getElementById('shieldsPanel');
    this.menuDropdown = document.getElementById('menuDropdown');
    this.shieldsToggle = document.getElementById('shieldsToggle');
    this.totalBlocked = document.getElementById('totalBlocked');
    this.pageBlockedEl = document.getElementById('pageBlocked');
    this.totalBlockedPanel = document.getElementById('totalBlockedPanel');
  }

  bindEvents() {
    // Navigation
    this.backBtn.addEventListener('click', () => this.goBack());
    this.forwardBtn.addEventListener('click', () => this.goForward());
    this.refreshBtn.addEventListener('click', () => this.refresh());
    
    // URL input
    this.urlInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        this.navigate(this.urlInput.value);
        this.urlInput.blur();
      }
    });
    
    this.urlInput.addEventListener('focus', () => this.urlInput.select());
    
    // Shields
    this.shieldsBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      this.togglePanel('shields');
    });
    
    this.shieldsToggle.addEventListener('change', () => {
      this.shields.toggle(this.shieldsToggle.checked);
    });
    
    document.getElementById('closePanelBtn').addEventListener('click', () => {
      this.shieldsPanel.classList.add('hidden');
    });
    
    document.getElementById('openSettingsBtn').addEventListener('click', () => {
      this.navigate('aurora://settings');
      this.shieldsPanel.classList.add('hidden');
    });
    
    // Menu
    this.menuBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      this.togglePanel('menu');
    });
    
    document.querySelectorAll('.menu-item').forEach(item => {
      item.addEventListener('click', () => {
        this.handleMenuAction(item.dataset.action);
        this.menuDropdown.classList.add('hidden');
      });
    });
    
    // Tabs
    this.newTabBtn.addEventListener('click', () => this.createTab('aurora://newtab'));
    
    // Quick links
    document.querySelectorAll('.quick-link').forEach(link => {
      link.addEventListener('click', () => this.navigate(link.dataset.url));
    });
    
    // Close panels on outside click
    document.addEventListener('click', (e) => {
      if (!this.shieldsPanel.contains(e.target) && !this.shieldsBtn.contains(e.target)) {
        this.shieldsPanel.classList.add('hidden');
      }
      if (!this.menuDropdown.contains(e.target) && !this.menuBtn.contains(e.target)) {
        this.menuDropdown.classList.add('hidden');
      }
    });
    
    // Handle back button on Android
    document.addEventListener('backbutton', () => {
      if (!this.shieldsPanel.classList.contains('hidden')) {
        this.shieldsPanel.classList.add('hidden');
      } else if (!this.menuDropdown.classList.contains('hidden')) {
        this.menuDropdown.classList.add('hidden');
      } else {
        this.goBack();
      }
    });
  }

  loadSettings() {
    this.shieldsToggle.checked = this.shields.enabled;
  }

  updateStats() {
    const stats = this.shields.getStats();
    this.totalBlocked.textContent = this.formatNumber(stats.totalBlocked);
    this.totalBlockedPanel.textContent = this.formatNumber(stats.totalBlocked);
    this.shieldsCount.textContent = this.pageBlocked;
    this.pageBlockedEl.textContent = this.pageBlocked;
  }

  formatNumber(num) {
    if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
    if (num >= 1000) return (num / 1000).toFixed(1) + 'K';
    return num.toString();
  }

  togglePanel(panel) {
    if (panel === 'shields') {
      this.shieldsPanel.classList.toggle('hidden');
      this.menuDropdown.classList.add('hidden');
    } else {
      this.menuDropdown.classList.toggle('hidden');
      this.shieldsPanel.classList.add('hidden');
    }
  }

  // Tab Management
  createTab(url = 'aurora://newtab') {
    const tabId = 'tab-' + Date.now();
    const tab = { id: tabId, url, title: 'New Tab', history: [], historyIndex: -1 };
    
    this.tabs.push(tab);
    this.renderTab(tab);
    this.activateTab(tabId);
    
    if (!url.startsWith('aurora://')) {
      this.loadUrl(url);
    }
    
    return tabId;
  }

  renderTab(tab) {
    const tabEl = document.createElement('div');
    tabEl.className = 'tab';
    tabEl.id = tab.id;
    tabEl.innerHTML = `
      <span class="tab-title">${tab.title}</span>
      <button class="tab-close">×</button>
    `;
    
    tabEl.addEventListener('click', (e) => {
      if (!e.target.classList.contains('tab-close')) {
        this.activateTab(tab.id);
      }
    });
    
    tabEl.querySelector('.tab-close').addEventListener('click', (e) => {
      e.stopPropagation();
      this.closeTab(tab.id);
    });
    
    this.tabsContainer.appendChild(tabEl);
  }

  activateTab(tabId) {
    // Deactivate current
    if (this.activeTabId) {
      const currentEl = document.getElementById(this.activeTabId);
      if (currentEl) currentEl.classList.remove('active');
    }
    
    this.activeTabId = tabId;
    const tabEl = document.getElementById(tabId);
    if (tabEl) tabEl.classList.add('active');
    
    const tab = this.tabs.find(t => t.id === tabId);
    if (tab) {
      this.urlInput.value = tab.url === 'aurora://newtab' ? '' : tab.url;
      this.pageBlocked = 0;
      this.updateStats();
      
      if (tab.url.startsWith('aurora://')) {
        this.showInternalPage(tab.url);
      } else if (tab.url && tab.url !== 'aurora://newtab') {
        this.loadUrl(tab.url);
      } else {
        this.showWelcome();
      }
    }
  }

  closeTab(tabId) {
    const index = this.tabs.findIndex(t => t.id === tabId);
    if (index === -1) return;
    
    document.getElementById(tabId)?.remove();
    this.tabs.splice(index, 1);
    
    if (this.tabs.length === 0) {
      this.createTab('aurora://newtab');
    } else if (this.activeTabId === tabId) {
      const newIndex = Math.min(index, this.tabs.length - 1);
      this.activateTab(this.tabs[newIndex].id);
    }
  }

  // Navigation
  navigate(input) {
    if (!input || !input.trim()) return;
    
    let url = input.trim();
    
    if (url.startsWith('aurora://')) {
      this.showInternalPage(url);
      return;
    }
    
    // Add protocol
    if (!url.match(/^https?:\/\//)) {
      if (url.includes('.') && !url.includes(' ')) {
        url = 'https://' + url;
      } else {
        url = 'https://duckduckgo.com/?q=' + encodeURIComponent(url);
      }
    }
    
    // Check if should block navigation
    if (this.shields.shouldBlockNavigation(url)) {
      alert('🛡️ AuroraShield blocked this site as potentially harmful.');
      return;
    }
    
    this.loadUrl(url);
  }

  loadUrl(url) {
    const tab = this.tabs.find(t => t.id === this.activeTabId);
    if (!tab) return;
    
    tab.url = url;
    tab.title = new URL(url).hostname;
    this.urlInput.value = url;
    this.updateTabTitle(tab);
    this.updateSecurityIcon(url);
    
    // Reset page blocked count
    this.pageBlocked = 0;
    this.updateStats();
    
    // Show webview
    this.welcomePage.classList.add('hidden');
    this.settingsPage.classList.add('hidden');
    this.webviewFrame.classList.remove('hidden');
    
    // Load URL in iframe (with ad blocking via proxy or service worker)
    this.webviewFrame.src = url;
    
    // Add to history
    tab.history.push(url);
    tab.historyIndex = tab.history.length - 1;
    this.updateNavButtons();
  }

  showWelcome() {
    this.welcomePage.classList.remove('hidden');
    this.settingsPage.classList.add('hidden');
    this.webviewFrame.classList.add('hidden');
  }

  showInternalPage(url) {
    const tab = this.tabs.find(t => t.id === this.activeTabId);
    if (tab) {
      tab.url = url;
      this.urlInput.value = url;
    }
    
    this.welcomePage.classList.add('hidden');
    this.webviewFrame.classList.add('hidden');
    
    if (url === 'aurora://newtab') {
      this.showWelcome();
    } else if (url.startsWith('aurora://settings')) {
      this.showSettings();
    }
  }

  showSettings() {
    this.settingsPage.classList.remove('hidden');
    this.settingsPage.innerHTML = this.renderSettings();
    this.bindSettingsEvents();
  }

  renderSettings() {
    const stats = this.shields.getStats();
    return `
      <h1>⚙️ Settings</h1>
      
      <div class="settings-section">
        <h2>🛡️ Shields</h2>
        <div class="setting-item">
          <div class="setting-info">
            <h4>Enable Shields</h4>
            <p>Block ads and trackers</p>
          </div>
          <label class="switch">
            <input type="checkbox" id="settingsShieldsToggle" ${this.shields.enabled ? 'checked' : ''}>
            <span class="slider"></span>
          </label>
        </div>
        <div class="setting-item">
          <div class="setting-info">
            <h4>Total Blocked</h4>
            <p>${stats.totalBlocked} ads and trackers</p>
          </div>
        </div>
      </div>
      
      <div class="settings-section">
        <h2>🔐 Privacy</h2>
        <div class="setting-item">
          <div class="setting-info">
            <h4>Search Engine</h4>
            <p>Default search provider</p>
          </div>
          <select id="searchEngine">
            <option value="duckduckgo">DuckDuckGo</option>
            <option value="google">Google</option>
            <option value="bing">Bing</option>
          </select>
        </div>
      </div>
      
      <div class="settings-section">
        <h2>ℹ️ About</h2>
        <div class="setting-item">
          <div class="setting-info">
            <h4>AuroraShield Browser</h4>
            <p>Version 1.0.0</p>
          </div>
        </div>
      </div>
    `;
  }

  bindSettingsEvents() {
    const toggle = document.getElementById('settingsShieldsToggle');
    if (toggle) {
      toggle.addEventListener('change', () => {
        this.shields.toggle(toggle.checked);
        this.shieldsToggle.checked = toggle.checked;
      });
    }
  }

  goBack() {
    const tab = this.tabs.find(t => t.id === this.activeTabId);
    if (tab && tab.historyIndex > 0) {
      tab.historyIndex--;
      this.loadUrl(tab.history[tab.historyIndex]);
    }
  }

  goForward() {
    const tab = this.tabs.find(t => t.id === this.activeTabId);
    if (tab && tab.historyIndex < tab.history.length - 1) {
      tab.historyIndex++;
      this.loadUrl(tab.history[tab.historyIndex]);
    }
  }

  refresh() {
    if (this.webviewFrame.src) {
      this.webviewFrame.src = this.webviewFrame.src;
    }
  }

  updateNavButtons() {
    const tab = this.tabs.find(t => t.id === this.activeTabId);
    this.backBtn.disabled = !tab || tab.historyIndex <= 0;
    this.forwardBtn.disabled = !tab || tab.historyIndex >= tab.history.length - 1;
  }

  updateTabTitle(tab) {
    const tabEl = document.getElementById(tab.id);
    if (tabEl) {
      tabEl.querySelector('.tab-title').textContent = tab.title;
    }
  }

  updateSecurityIcon(url) {
    try {
      const urlObj = new URL(url);
      this.securityIcon.textContent = urlObj.protocol === 'https:' ? '🔒' : '⚠️';
    } catch {
      this.securityIcon.textContent = '📄';
    }
  }

  handleMenuAction(action) {
    switch (action) {
      case 'new-tab':
        this.createTab('aurora://newtab');
        break;
      case 'settings':
        this.navigate('aurora://settings');
        break;
      case 'history':
        alert('History feature coming soon!');
        break;
      case 'bookmarks':
        alert('Bookmarks feature coming soon!');
        break;
      case 'share':
        this.shareCurrentPage();
        break;
      case 'about':
        alert('AuroraShield Browser v1.0.0\n\nPrivacy-focused browser with Shields.');
        break;
    }
  }

  async shareCurrentPage() {
    const tab = this.tabs.find(t => t.id === this.activeTabId);
    if (tab && navigator.share) {
      try {
        await navigator.share({ title: tab.title, url: tab.url });
      } catch (e) {}
    }
  }
}

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
  window.app = new AuroraShieldApp();
});
