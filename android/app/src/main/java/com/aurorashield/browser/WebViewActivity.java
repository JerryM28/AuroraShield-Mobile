package com.aurorashield.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

public class WebViewActivity extends Activity {
    private WebView webView;
    private ProgressBar progressBar;
    private TextView urlText;
    private ImageButton backBtn, forwardBtn, closeBtn, refreshBtn;
    private String currentUrl;
    private int blockedCount = 0;
    
    // Blocked domains set for fast lookup
    private static final Set<String> BLOCKED_DOMAINS = new HashSet<>();
    static {
        // Major ad networks
        BLOCKED_DOMAINS.add("doubleclick.net");
        BLOCKED_DOMAINS.add("googlesyndication.com");
        BLOCKED_DOMAINS.add("googleadservices.com");
        BLOCKED_DOMAINS.add("adservice.google.com");
        BLOCKED_DOMAINS.add("pagead2.googlesyndication.com");
        BLOCKED_DOMAINS.add("adnxs.com");
        BLOCKED_DOMAINS.add("adsrvr.org");
        BLOCKED_DOMAINS.add("advertising.com");
        BLOCKED_DOMAINS.add("outbrain.com");
        BLOCKED_DOMAINS.add("taboola.com");
        BLOCKED_DOMAINS.add("criteo.com");
        BLOCKED_DOMAINS.add("criteo.net");
        BLOCKED_DOMAINS.add("pubmatic.com");
        BLOCKED_DOMAINS.add("rubiconproject.com");
        BLOCKED_DOMAINS.add("openx.net");
        BLOCKED_DOMAINS.add("casalemedia.com");
        BLOCKED_DOMAINS.add("indexexchange.com");
        BLOCKED_DOMAINS.add("smartadserver.com");
        BLOCKED_DOMAINS.add("adform.net");
        BLOCKED_DOMAINS.add("media.net");
        BLOCKED_DOMAINS.add("revcontent.com");
        BLOCKED_DOMAINS.add("mgid.com");
        BLOCKED_DOMAINS.add("zergnet.com");
        BLOCKED_DOMAINS.add("propellerads.com");
        
        // Tracking & Analytics
        BLOCKED_DOMAINS.add("google-analytics.com");
        BLOCKED_DOMAINS.add("googletagmanager.com");
        BLOCKED_DOMAINS.add("analytics.google.com");
        BLOCKED_DOMAINS.add("pixel.facebook.com");
        BLOCKED_DOMAINS.add("connect.facebook.net");
        BLOCKED_DOMAINS.add("analytics.twitter.com");
        BLOCKED_DOMAINS.add("bat.bing.com");
        BLOCKED_DOMAINS.add("clarity.ms");
        BLOCKED_DOMAINS.add("hotjar.com");
        BLOCKED_DOMAINS.add("mixpanel.com");
        BLOCKED_DOMAINS.add("amplitude.com");
        BLOCKED_DOMAINS.add("segment.io");
        BLOCKED_DOMAINS.add("appsflyer.com");
        BLOCKED_DOMAINS.add("adjust.com");
        BLOCKED_DOMAINS.add("branch.io");
        BLOCKED_DOMAINS.add("app.link");
        
        // Popup/Push networks
        BLOCKED_DOMAINS.add("pncloudfl.com");
        BLOCKED_DOMAINS.add("cdn.pncloudfl.com");
        BLOCKED_DOMAINS.add("onpathbihc.com");
        BLOCKED_DOMAINS.add("frozenpayerpregnant.com");
        BLOCKED_DOMAINS.add("pushame.com");
        BLOCKED_DOMAINS.add("pushnest.com");
        BLOCKED_DOMAINS.add("pushwhy.com");
        BLOCKED_DOMAINS.add("notix.io");
        BLOCKED_DOMAINS.add("pushprofit.net");
        BLOCKED_DOMAINS.add("popads.net");
        BLOCKED_DOMAINS.add("popcash.net");
        BLOCKED_DOMAINS.add("exoclick.com");
        BLOCKED_DOMAINS.add("juicyads.com");
        BLOCKED_DOMAINS.add("trafficjunky.com");
        BLOCKED_DOMAINS.add("clickadu.com");
        BLOCKED_DOMAINS.add("hilltopads.net");
        BLOCKED_DOMAINS.add("adsterra.com");
        BLOCKED_DOMAINS.add("evadav.com");
        
        // Crypto miners
        BLOCKED_DOMAINS.add("coinhive.com");
        BLOCKED_DOMAINS.add("cryptoloot.pro");
        BLOCKED_DOMAINS.add("jsecoin.com");
        BLOCKED_DOMAINS.add("coinimp.com");
        
        // Fingerprinting
        BLOCKED_DOMAINS.add("fingerprintjs.com");
        BLOCKED_DOMAINS.add("fpjs.io");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_webview);
        
        currentUrl = getIntent().getStringExtra("url");
        if (currentUrl == null) currentUrl = "https://duckduckgo.com";
        
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        urlText = findViewById(R.id.urlText);
        backBtn = findViewById(R.id.backBtn);
        forwardBtn = findViewById(R.id.forwardBtn);
        closeBtn = findViewById(R.id.closeBtn);
        refreshBtn = findViewById(R.id.refreshBtn);
        
        setupWebView();
        setupButtons();
        webView.loadUrl(currentUrl);
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setUserAgentString(settings.getUserAgentString().replace("wv", "") + " AuroraShield/1.0");
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (shouldBlockUrl(url)) {
                    blockedCount++;
                    // Return empty response to block
                    return new WebResourceResponse(
                        "text/plain", 
                        "UTF-8", 
                        new ByteArrayInputStream("".getBytes())
                    );
                }
                return super.shouldInterceptRequest(view, request);
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Block popup/redirect to ad sites
                if (shouldBlockNavigation(url)) {
                    return true;
                }
                return false;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                currentUrl = url;
                urlText.setText(url);
                progressBar.setVisibility(View.VISIBLE);
                updateNavButtons();
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                updateNavButtons();
                injectAdBlocker(view);
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });
    }
    
    private void setupButtons() {
        closeBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        forwardBtn.setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        refreshBtn.setOnClickListener(v -> webView.reload());
    }
    
    private void updateNavButtons() {
        backBtn.setEnabled(webView.canGoBack());
        forwardBtn.setEnabled(webView.canGoForward());
        backBtn.setAlpha(webView.canGoBack() ? 1.0f : 0.4f);
        forwardBtn.setAlpha(webView.canGoForward() ? 1.0f : 0.4f);
    }
    
    private boolean shouldBlockUrl(String url) {
        if (url == null) return false;
        String lowerUrl = url.toLowerCase();
        
        // Check blocked domains
        for (String domain : BLOCKED_DOMAINS) {
            if (lowerUrl.contains(domain)) return true;
        }
        
        // Check URL patterns
        String[] blockedPatterns = {
            "/pagead/", "/adserver/", "/advert/", "/banner/", "/ads/", "/ad/",
            "/adsense/", "/adframe/", "/adview/", "/adclick/", "/sponsor/",
            "/tracker/", "/tracking/", "/pixel/", "/beacon/", "/collect/",
            "/analytics/", "/stats/", "/telemetry/", "/log?", "/event?",
            "doubleclick", "googlesyndication", "googleadservices",
            "amazon-adsystem", "facebook.com/tr", "fbcdn.net/signals"
        };
        
        for (String pattern : blockedPatterns) {
            if (lowerUrl.contains(pattern)) return true;
        }
        
        return false;
    }
    
    private boolean shouldBlockNavigation(String url) {
        if (url == null) return false;
        String lowerUrl = url.toLowerCase();
        
        // Block scam/gambling redirects
        String[] scamKeywords = {"slot", "casino", "poker", "togel", "judi", "betting", "gacor", "maxwin"};
        String[] suspiciousTLDs = {".hair", ".xyz", ".top", ".click", ".win", ".bet", ".loan"};
        
        int keywordCount = 0;
        for (String kw : scamKeywords) {
            if (lowerUrl.contains(kw)) keywordCount++;
        }
        
        if (keywordCount >= 2) return true;
        
        for (String tld : suspiciousTLDs) {
            if (lowerUrl.contains(tld) && keywordCount >= 1) return true;
        }
        
        return false;
    }
    
    private void injectAdBlocker(WebView view) {
        String js = "(function() {" +
            // Aggressive CSS to hide ad elements
            "var css = '" +
            // Standard ad selectors
            "[class*=\"ad-\"], [class*=\"ads-\"], [class*=\"advert\"], [class*=\"adsbox\"], " +
            "[id*=\"ad-\"], [id*=\"ads-\"], [id*=\"advert\"], " +
            "[class*=\"banner\"], [id*=\"banner\"], [class*=\"sponsor\"], " +
            "iframe[src*=\"ad\"], iframe[src*=\"doubleclick\"], " +
            ".adsbygoogle, .ad-container, .ad-wrapper, .ad-slot, .ad-unit, " +
            ".blox.mlb.kln, " +
            // Popup/overlay selectors
            "[style*=\"z-index: 2147483647\"], " +
            "[style*=\"z-index:2147483647\"], " +
            "div[style*=\"position: fixed\"][style*=\"z-index\"]:not([class]), " +
            // Gambling/slot keywords in links and images
            "a[href*=\"slot\"], a[href*=\"casino\"], a[href*=\"togel\"], a[href*=\"judi\"], " +
            "a[href*=\"poker\"], a[href*=\"betting\"], a[href*=\"gacor\"], a[href*=\"maxwin\"], " +
            "a[href*=\"sbobet\"], a[href*=\"bonus\"], a[href*=\"daftar\"], " +
            "img[src*=\"slot\"], img[src*=\"casino\"], img[src*=\"bonus\"], img[src*=\"gacor\"], " +
            "img[alt*=\"slot\"], img[alt*=\"casino\"], img[alt*=\"bonus\"], " +
            // Common ad container classes
            ".iklan, .ads, .advertisement, .sponsored, .promo-banner, " +
            "div[data-ad], div[data-ads], div[data-advertisement], " +
            "aside[class*=\"widget\"], .sidebar-ads, .floating-ads, " +
            // Specific site patterns
            "a[target=\"_blank\"][rel*=\"nofollow\"] img, " +
            "div[onclick*=\"window.open\"], " +
            "a[href*=\"bit.ly\"], a[href*=\"tinyurl\"], a[href*=\"shorturl\"] " +
            "{ display: none !important; visibility: hidden !important; height: 0 !important; width: 0 !important; overflow: hidden !important; }'; " +
            
            "var style = document.createElement('style');" +
            "style.type = 'text/css';" +
            "style.appendChild(document.createTextNode(css));" +
            "document.head.appendChild(style);" +
            
            // Remove gambling/slot images by content
            "document.querySelectorAll('img').forEach(function(img) {" +
            "  var src = (img.src || '').toLowerCase();" +
            "  var alt = (img.alt || '').toLowerCase();" +
            "  var keywords = ['slot', 'casino', 'poker', 'togel', 'judi', 'gacor', 'maxwin', 'bonus', 'jackpot', 'scatter', 'rtp', 'sbobet', 'pragmatic', 'joker', 'deposit'];" +
            "  for (var i = 0; i < keywords.length; i++) {" +
            "    if (src.indexOf(keywords[i]) !== -1 || alt.indexOf(keywords[i]) !== -1) {" +
            "      img.remove();" +
            "      break;" +
            "    }" +
            "  }" +
            "});" +
            
            // Remove links to gambling sites
            "document.querySelectorAll('a').forEach(function(a) {" +
            "  var href = (a.href || '').toLowerCase();" +
            "  var text = (a.textContent || '').toLowerCase();" +
            "  var keywords = ['slot', 'casino', 'poker', 'togel', 'judi', 'gacor', 'maxwin', 'sbobet', 'daftar', 'bonus 100'];" +
            "  for (var i = 0; i < keywords.length; i++) {" +
            "    if (href.indexOf(keywords[i]) !== -1 || text.indexOf(keywords[i]) !== -1) {" +
            "      a.remove();" +
            "      break;" +
            "    }" +
            "  }" +
            "});" +
            
            // Remove elements with high z-index (popups)
            "document.querySelectorAll('*').forEach(function(el) {" +
            "  var z = window.getComputedStyle(el).zIndex;" +
            "  if (z && parseInt(z) >= 999999) {" +
            "    el.remove();" +
            "  }" +
            "});" +
            
            // Remove fixed position overlays
            "document.querySelectorAll('div[style*=\"position: fixed\"], div[style*=\"position:fixed\"]').forEach(function(el) {" +
            "  if (el.querySelector('iframe') || el.querySelector('img[src*=\"ad\"]')) {" +
            "    el.remove();" +
            "  }" +
            "});" +
            
            // Block alert/confirm/prompt
            "window.alert = function() {};" +
            "window.confirm = function() { return false; };" +
            "window.prompt = function() { return null; };" +
            
            // Block window.open popups
            "window.open = function() { return null; };" +
            
            // Mutation observer to catch dynamically added ads
            "var observer = new MutationObserver(function(mutations) {" +
            "  mutations.forEach(function(m) {" +
            "    m.addedNodes.forEach(function(node) {" +
            "      if (node.nodeType === 1) {" +
            "        var html = node.outerHTML ? node.outerHTML.toLowerCase() : '';" +
            "        if (html.indexOf('slot') !== -1 || html.indexOf('casino') !== -1 || html.indexOf('gacor') !== -1 || html.indexOf('bonus') !== -1) {" +
            "          node.remove();" +
            "        }" +
            "      }" +
            "    });" +
            "  });" +
            "});" +
            "observer.observe(document.body, {childList: true, subtree: true});" +
            
            "})();";
        
        view.evaluateJavascript(js, null);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
