package com.kombuchafinder;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private WebView zeptoWebView, blinkitWebView;
    private TextView statusText, zeptoPriceText, blinkitPriceText, resultText;
    private Button searchBtn, addToCartBtn;
    private ProgressBar progressBar;
    private LinearLayout resultCard;

    private double zeptoPrice = -1;
    private double blinkitPrice = -1;
    private String cheapestSource = "";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        zeptoPriceText = findViewById(R.id.zeptoPriceText);
        blinkitPriceText = findViewById(R.id.blinkitPriceText);
        resultText = findViewById(R.id.resultText);
        searchBtn = findViewById(R.id.searchBtn);
        addToCartBtn = findViewById(R.id.addToCartBtn);
        progressBar = findViewById(R.id.progressBar);
        resultCard = findViewById(R.id.resultCard);
        zeptoWebView = findViewById(R.id.zeptoWebView);
        blinkitWebView = findViewById(R.id.blinkitWebView);

        setupWebView(zeptoWebView, "zepto");
        setupWebView(blinkitWebView, "blinkit");

        searchBtn.setOnClickListener(v -> startSearch());
        addToCartBtn.setOnClickListener(v -> addCheapestToCart());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView webView, String source) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 12; Pixel 6) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36"
        );
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.addJavascriptInterface(new WebAppInterface(source), "Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mainHandler.postDelayed(() -> {
                    if (source.equals("zepto")) injectZeptoSearch(view);
                    else injectBlinkitSearch(view);
                }, 3000);
            }
        });
    }

    private void startSearch() {
        zeptoPrice = -1;
        blinkitPrice = -1;
        cheapestSource = "";
        searchBtn.setEnabled(false);
        addToCartBtn.setVisibility(View.GONE);
        resultCard.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        zeptoPriceText.setText("Zepto: Searching...");
        blinkitPriceText.setText("Blinkit: Searching...");
        updateStatus("🔍 Loading Zepto & Blinkit...");

        zeptoWebView.loadUrl("https://www.zeptonow.com/search?query=kombucha");
        blinkitWebView.loadUrl("https://blinkit.com/s/?q=kombucha");
    }

    private void injectZeptoSearch(WebView view) {
        updateStatus("🟣 Scraping Zepto prices...");
        String js =
            "(function() {" +
            "  try {" +
            "    var items = document.querySelectorAll('[data-testid=\"product-card\"], .product-card, [class*=\"ProductCard\"]');" +
            "    if (!items || items.length === 0) {" +
            "      items = document.querySelectorAll('div[class*=\"product\"], div[class*=\"item\"]');" +
            "    }" +
            "    var minPrice = 99999; var bestName = ''; var bestEl = null;" +
            "    for (var i = 0; i < Math.min(items.length, 10); i++) {" +
            "      var el = items[i];" +
            "      var text = el.innerText || '';" +
            "      var matches = text.match(/₹\\s*([0-9]+(?:\\.[0-9]+)?)/g);" +
            "      if (matches) {" +
            "        for (var m of matches) {" +
            "          var p = parseFloat(m.replace(/[₹\\s]/g,''));" +
            "          if (p > 10 && p < minPrice) { minPrice = p; bestEl = el; }" +
            "        }" +
            "      }" +
            "    }" +
            "    if (minPrice < 99999) {" +
            "      var nameEl = bestEl ? bestEl.querySelector('h3,h4,p,[class*=\"name\"],[class*=\"title\"]') : null;" +
            "      bestName = nameEl ? nameEl.innerText.substring(0,50) : 'Kombucha';" +
            "      Android.onPriceFound('zepto', minPrice, bestName);" +
            "    } else {" +
            "      Android.onPriceFound('zepto', -1, 'Not found');" +
            "    }" +
            "  } catch(e) { Android.onPriceFound('zepto', -1, 'Error: '+e.message); }" +
            "})();";
        view.evaluateJavascript(js, null);
    }

    private void injectBlinkitSearch(WebView view) {
        updateStatus("🟡 Scraping Blinkit prices...");
        String js =
            "(function() {" +
            "  try {" +
            "    var items = document.querySelectorAll('[data-testid=\"product-card\"], .product-item, [class*=\"Product\"], [class*=\"product\"]');" +
            "    var minPrice = 99999; var bestName = ''; var bestEl = null;" +
            "    for (var i = 0; i < Math.min(items.length, 10); i++) {" +
            "      var el = items[i];" +
            "      var text = el.innerText || '';" +
            "      var matches = text.match(/₹\\s*([0-9]+(?:\\.[0-9]+)?)/g);" +
            "      if (matches) {" +
            "        for (var m of matches) {" +
            "          var p = parseFloat(m.replace(/[₹\\s]/g,''));" +
            "          if (p > 10 && p < minPrice) { minPrice = p; bestEl = el; }" +
            "        }" +
            "      }" +
            "    }" +
            "    if (minPrice < 99999) {" +
            "      var nameEl = bestEl ? bestEl.querySelector('h3,h4,p,[class*=\"name\"],[class*=\"title\"]') : null;" +
            "      bestName = nameEl ? nameEl.innerText.substring(0,50) : 'Kombucha';" +
            "      Android.onPriceFound('blinkit', minPrice, bestName);" +
            "    } else {" +
            "      Android.onPriceFound('blinkit', -1, 'Not found');" +
            "    }" +
            "  } catch(e) { Android.onPriceFound('blinkit', -1, 'Error: '+e.message); }" +
            "})();";
        view.evaluateJavascript(js, null);
    }

    private void addCheapestToCart() {
        if (cheapestSource.isEmpty()) return;
        addToCartBtn.setEnabled(false);
        updateStatus("🛒 Adding to cart on " + cheapestSource + "...");

        String js =
            "(function() {" +
            "  try {" +
            "    var btns = document.querySelectorAll('button');" +
            "    for (var b of btns) {" +
            "      var t = b.innerText.toLowerCase();" +
            "      if (t.includes('add') || t === '+') {" +
            "        b.click();" +
            "        Android.onCartResult('success');" +
            "        return;" +
            "      }" +
            "    }" +
            "    Android.onCartResult('not_found');" +
            "  } catch(e) { Android.onCartResult('error'); }" +
            "})();";

        if (cheapestSource.equals("Zepto")) {
            zeptoWebView.evaluateJavascript(js, null);
        } else {
            blinkitWebView.evaluateJavascript(js, null);
        }
    }

    private void compareAndShowResult() {
        if (zeptoPrice == -1 && blinkitPrice == -1) {
            updateStatus("❌ Kombucha not found on either app. Try again.");
            searchBtn.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (zeptoPrice != -1 && blinkitPrice != -1) {
            cheapestSource = zeptoPrice <= blinkitPrice ? "Zepto" : "Blinkit";
            double cheapestPrice = Math.min(zeptoPrice, blinkitPrice);
            double savings = Math.abs(zeptoPrice - blinkitPrice);
            resultText.setText("🏆 Best price on " + cheapestSource +
                " at ₹" + cheapestPrice +
                "\nYou save ₹" + String.format("%.0f", savings) + " vs the other app!");
        } else if (zeptoPrice != -1) {
            cheapestSource = "Zepto";
            resultText.setText("🏆 Found on Zepto at ₹" + zeptoPrice + "\n(Blinkit: not available)");
        } else {
            cheapestSource = "Blinkit";
            resultText.setText("🏆 Found on Blinkit at ₹" + blinkitPrice + "\n(Zepto: not available)");
        }

        progressBar.setVisibility(View.GONE);
        resultCard.setVisibility(View.VISIBLE);
        addToCartBtn.setVisibility(View.VISIBLE);
        addToCartBtn.setEnabled(true);
        searchBtn.setEnabled(true);
        updateStatus("✅ Comparison complete!");
    }

    private void updateStatus(String msg) {
        mainHandler.post(() -> statusText.setText(msg));
    }

    class WebAppInterface {
        private final String source;
        private String productName = "";

        WebAppInterface(String source) { this.source = source; }

        @JavascriptInterface
        public void onPriceFound(String src, double price, String name) {
            mainHandler.post(() -> {
                if (src.equals("zepto")) {
                    zeptoPrice = price;
                    zeptoPriceText.setText(price > 0
                        ? "🟣 Zepto: ₹" + price + "\n" + name
                        : "🟣 Zepto: Not found");
                } else {
                    blinkitPrice = price;
                    blinkitPriceText.setText(price > 0
                        ? "🟡 Blinkit: ₹" + price + "\n" + name
                        : "🟡 Blinkit: Not found");
                }
                if (zeptoPrice != -1 || blinkitPrice != -1) {
                    boolean bothDone = (zeptoPrice >= 0 || zeptoPriceText.getText().toString().contains("Not found")) &&
                                      (blinkitPrice >= 0 || blinkitPriceText.getText().toString().contains("Not found"));
                    // Give 8s max for both to respond
                    mainHandler.postDelayed(() -> compareAndShowResult(), 1000);
                }
            });
        }

        @JavascriptInterface
        public void onCartResult(String result) {
            mainHandler.post(() -> {
                if (result.equals("success")) {
                    Toast.makeText(MainActivity.this,
                        "✅ Added to cart on " + cheapestSource + "!", Toast.LENGTH_LONG).show();
                    updateStatus("✅ Added to cart on " + cheapestSource + "!");
                } else {
                    Toast.makeText(MainActivity.this,
                        "⚠️ Please tap 'Add' manually in the app below", Toast.LENGTH_LONG).show();
                    updateStatus("⚠️ Tap 'Add to Cart' manually in the WebView below");
                }
                addToCartBtn.setEnabled(true);
            });
        }
    }
}
