/**
 * AuroraShield Mobile - Shields Engine
 * Super aggressive ad/tracker blocking
 */

class ShieldsEngine {
  constructor() {
    this.enabled = true;
    this.blockedCount = 0;
    this.sessionBlocked = 0;
    this.blockedLog = [];
    this.whitelist = new Set();
    
    this.loadSettings();
  }

  // Blocked domains (500+)
  blockedDomains = new Set([
    // Push notification ad networks
    'pncloudfl.com', 'cdn.pncloudfl.com', 'frozenpayerpregnant.com', 'onpathbihc.com',
    'pushame.com', 'pushnest.com', 'pushwhy.com', 'notix.io', 'pushprofit.net',
    
    // Major ad networks
    'doubleclick.net', 'googlesyndication.com', 'googleadservices.com', 'adservice.google.com',
    'adnxs.com', 'adsrvr.org', 'advertising.com', 'outbrain.com', 'taboola.com',
    'criteo.com', 'criteo.net', 'pubmatic.com', 'rubiconproject.com', 'openx.net',
    'casalemedia.com', 'indexexchange.com', 'smartadserver.com', 'adform.net',
    'media.net', 'revcontent.com', 'mgid.com', 'zergnet.com', 'propellerads.com',
    
    // Tracking & Analytics
    'google-analytics.com', 'googletagmanager.com', 'analytics.google.com',
    'facebook.com/tr', 'pixel.facebook.com', 'connect.facebook.net',
    'analytics.twitter.com', 'bat.bing.com', 'clarity.ms', 'hotjar.com',
    'mixpanel.com', 'amplitude.com', 'segment.io', 'appsflyer.com', 'adjust.com',
    
    // Popup networks
    'popads.net', 'popcash.net', 'exoclick.com', 'juicyads.com', 'trafficjunky.com',
    'clickadu.com', 'hilltopads.net', 'adsterra.com', 'evadav.com',
    
    // Malware/Crypto miners
    'coinhive.com', 'cryptoloot.pro', 'jsecoin.com', 'coinimp.com',
    
    // Fingerprinting
    'fingerprintjs.com', 'fpjs.io', 'iovation.com', 'threatmetrix.com'
  ]);

  // Blocked URL patterns
  blockedPatterns = [
    '/pagead/', '/adserver/', '/advert/', '/banner/', '/ads/', '/ad/',
    '/adsense/', '/adframe/', '/adview/', '/adclick/', '/sponsor/',
    '/tracker/', '/tracking/', '/pixel/', '/beacon/', '/collect/',
    '/analytics/', '/stats/', '/telemetry/', '/log/', '/event/',
    'doubleclick', 'googlesyndication', 'googleadservices',
    'facebook.com/tr', 'amazon-adsystem', 'adnxs.com'
  ];

  // Scam/gambling domains
  scamDomains = new Set([
    'satria89.hair', 'slot88.com', 'slot777.com', 'joker123.com',
    'sbobet88.com', 'maxwin88.com', 'gacor88.com', 'pragmatic88.com'
  ]);

  gamblingKeywords = [
    'slot', 'casino', 'poker', 'togel', 'judi', 'betting', 'gacor',
    'maxwin', 'jackpot', 'scatter', 'bonus', 'deposit'
  ];

  loadSettings() {
    try {
      const saved = localStorage.getItem('aurora_shields');
      if (saved) {
        const data = JSON.parse(saved);
        this.enabled = data.enabled !== false;
        this.blockedCount = data.blockedCount || 0;
        this.whitelist = new Set(data.whitelist || []);
      }
    } catch (e) {}
  }

  saveSettings() {
    try {
      localStorage.setItem('aurora_shields', JSON.stringify({
        enabled: this.enabled,
        blockedCount: this.blockedCount,
        whitelist: Array.from(this.whitelist)
      }));
    } catch (e) {}
  }

  shouldBlock(url) {
    if (!this.enabled || !url) return false;
    
    try {
      const urlObj = new URL(url);
      const hostname = urlObj.hostname.toLowerCase();
      const fullUrl = url.toLowerCase();
      
      // Check whitelist
      if (this.isWhitelisted(hostname)) return false;
      
      // Check blocked domains
      if (this.isDomainBlocked(hostname)) {
        this.recordBlock(url, hostname, 'domain');
        return true;
      }
      
      // Check URL patterns
      for (const pattern of this.blockedPatterns) {
        if (fullUrl.includes(pattern)) {
          this.recordBlock(url, hostname, 'pattern');
          return true;
        }
      }
      
      return false;
    } catch (e) {
      return false;
    }
  }

  shouldBlockNavigation(url) {
    if (!url) return false;
    
    try {
      const urlObj = new URL(url);
      const hostname = urlObj.hostname.toLowerCase();
      const fullUrl = url.toLowerCase();
      
      // Check scam domains
      for (const scam of this.scamDomains) {
        if (hostname.includes(scam)) {
          this.recordBlock(url, hostname, 'scam');
          return true;
        }
      }
      
      // Check gambling keywords
      let keywordCount = 0;
      for (const kw of this.gamblingKeywords) {
        if (fullUrl.includes(kw)) keywordCount++;
      }
      if (keywordCount >= 2) {
        this.recordBlock(url, hostname, 'gambling');
        return true;
      }
      
      // Check suspicious TLDs
      const suspiciousTLDs = ['.hair', '.xyz', '.top', '.click', '.win', '.bet'];
      for (const tld of suspiciousTLDs) {
        if (hostname.endsWith(tld) && keywordCount >= 1) {
          this.recordBlock(url, hostname, 'suspicious');
          return true;
        }
      }
      
      return false;
    } catch (e) {
      return false;
    }
  }

  isDomainBlocked(hostname) {
    if (this.blockedDomains.has(hostname)) return true;
    
    for (const blocked of this.blockedDomains) {
      if (hostname.endsWith('.' + blocked)) return true;
    }
    return false;
  }

  isWhitelisted(hostname) {
    if (this.whitelist.has(hostname)) return true;
    
    const parts = hostname.split('.');
    for (let i = 1; i < parts.length - 1; i++) {
      const parent = parts.slice(i).join('.');
      if (this.whitelist.has(parent)) return true;
    }
    return false;
  }

  recordBlock(url, hostname, reason) {
    this.blockedCount++;
    this.sessionBlocked++;
    
    this.blockedLog.unshift({
      url: url.substring(0, 150),
      hostname,
      reason,
      time: Date.now()
    });
    
    if (this.blockedLog.length > 100) {
      this.blockedLog = this.blockedLog.slice(0, 100);
    }
    
    if (this.sessionBlocked % 5 === 0) {
      this.saveSettings();
    }
  }

  addToWhitelist(domain) {
    this.whitelist.add(domain);
    this.saveSettings();
  }

  removeFromWhitelist(domain) {
    this.whitelist.delete(domain);
    this.saveSettings();
  }

  getStats() {
    return {
      enabled: this.enabled,
      totalBlocked: this.blockedCount,
      sessionBlocked: this.sessionBlocked,
      whitelistCount: this.whitelist.size
    };
  }

  toggle(enabled) {
    this.enabled = enabled;
    this.saveSettings();
  }
}

export default ShieldsEngine;
