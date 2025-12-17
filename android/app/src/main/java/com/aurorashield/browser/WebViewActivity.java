package com.aurorashield.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebViewActivity extends Activity {
    private WebView webView;
    private ProgressBar progressBar;
    private TextView urlText;
    private ImageButton backBtn, forwardBtn, closeBtn, refreshBtn;
    private String currentUrl;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        setContentView(R.layout.activity_webview);
        
        // Get URL from intent
        currentUrl = getIntent().getStringExtra("url");
        if (currentUrl == null) currentUrl = "https://duckduckgo.com";
        
        // Initialize views
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        urlText = findViewById(R.id.urlText);
        backBtn = findViewById(R.id.backBtn);
        forwardBtn = findViewById(R.id.forwardBtn);
        closeBtn = findViewById(R.id.closeBtn);
        refreshBtn = findViewById(R.id.refreshBtn);
        
        // Setup WebView
        setupWebView();
        
        // Setup buttons
        setupButtons();
        
        // Load URL
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
        settings.setUserAgentString(settings.getUserAgentString() + " AuroraShield/1.0");
        
        // Block ads via WebViewClient
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (shouldBlockUrl(url)) {
                    return true; // Block
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
                
                // Inject ad blocking CSS
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
        
        backBtn.setOnClickListener(v -> {
            if (webView.canGoBack()) webView.goBack();
        });
        
        forwardBtn.setOnClickListener(v -> {
            if (webView.canGoForward()) webView.goForward();
        });
        
        refreshBtn.setOnClickListener(v -> webView.reload());
    }
    
    private void updateNavButtons() {
        backBtn.setEnabled(webView.canGoBack());
        forwardBtn.setEnabled(webView.canGoForward());
        backBtn.setAlpha(webView.canGoBack() ? 1.0f : 0.4f);
        forwardBtn.setAlpha(webView.canGoForward() ? 1.0f : 0.4f);
    }
    
    private boolean shouldBlockUrl(String url) {
        String[] blockedDomains = {
            "doubleclick.net", "googlesyndication.com", "googleadservices.com",
            "adnxs.com", "adsrvr.org", "advertising.com", "outbrain.com", "taboola.com",
            "criteo.com", "pubmatic.com", "rubiconproject.com", "openx.net",
            "google-analytics.com", "googletagmanager.com", "facebook.com/tr",
            "popads.net", "popcash.net", "exoclick.com", "pncloudfl.com",
            "onpathbihc.com", "frozenpayerpregnant.com", "pushame.com"
        };
        
        String lowerUrl = url.toLowerCase();
        for (String domain : blockedDomains) {
            if (lowerUrl.contains(domain)) return true;
        }
        
        String[] blockedPatterns = {"/pagead/", "/adserver/", "/ads/", "/banner/", "/tracker/"};
        for (String pattern : blockedPatterns) {
            if (lowerUrl.contains(pattern)) return true;
        }
        
        return false;
    }
    
    private void injectAdBlocker(WebView view) {
        String css = "div[class*='ad-'], div[class*='ads-'], div[id*='ad-'], " +
                    "div[id*='ads-'], iframe[src*='ad'], .adsbygoogle, " +
                    ".blox.mlb.kln { display: none !important; }";
        String js = "var style = document.createElement('style');" +
                   "style.innerHTML = '" + css + "';" +
                   "document.head.appendChild(style);";
        view.evaluateJavascript(js, null);
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
