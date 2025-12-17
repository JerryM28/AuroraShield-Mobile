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
            // CSS to hide ad elements
            "var css = '" +
            "[class*=\"ad-\"], [class*=\"ads-\"], [class*=\"advert\"], " +
            "[id*=\"ad-\"], [id*=\"ads-\"], [id*=\"advert\"], " +
            "[class*=\"banner\"], [id*=\"banner\"], " +
            "iframe[src*=\"ad\"], iframe[src*=\"doubleclick\"], " +
            ".adsbygoogle, .ad-container, .ad-wrapper, .ad-slot, " +
            ".blox.mlb.kln, " +
            "[style*=\"z-index: 2147483647\"], " +
            "div[style*=\"position: fixed\"][style*=\"z-index\"]:not([class]) " +
            "{ display: none !important; visibility: hidden !important; height: 0 !important; }'; " +
            
            "var style = document.createElement('style');" +
            "style.type = 'text/css';" +
            "style.appendChild(document.createTextNode(css));" +
            "document.head.appendChild(style);" +
            
            // Remove elements with high z-index (popups)
            "var all = document.querySelectorAll('*');" +
            "for (var i = 0; i < all.length; i++) {" +
            "  var z = window.getComputedStyle(all[i]).zIndex;" +
            "  if (z && parseInt(z) >= 2147483640) {" +
            "    all[i].remove();" +
            "  }" +
            "}" +
            
            // Remove iframes with about:blank in fixed containers
            "document.querySelectorAll('div[style*=\"position: fixed\"] iframe[src=\"about:blank\"]').forEach(function(el) {" +
            "  el.parentElement.remove();" +
            "});" +
            
            // Block alert/confirm/prompt
            "window.alert = function() {};" +
            "window.confirm = function() { return false; };" +
            "window.prompt = function() { return null; };" +
            
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
