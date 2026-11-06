package com.mayatours.intelkg;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.*;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

public class MainActivity extends Activity {
    private WebView web;
    private ValueCallback<Uri[]> upload;
    private static final int FILE_PICK = 42;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();

        web.setWebViewClient(new WebViewClientCompat() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                return openExternalIfNeeded(request.getUrl());
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openExternalIfNeeded(Uri.parse(url));
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (upload != null) upload.onReceiveValue(null);
                upload = callback;
                try {
                    startActivityForResult(params.createIntent(), FILE_PICK);
                    return true;
                } catch (Exception ex) {
                    upload = null;
                    callback.onReceiveValue(null);
                    return false;
                }
            }
        });
        web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    private boolean openExternalIfNeeded(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null) return false;
        String s = scheme.toLowerCase(java.util.Locale.ROOT);
        String h = host == null ? "" : host.toLowerCase(java.util.Locale.ROOT);
        boolean whatsapp = "whatsapp".equals(s)
            || ( ("https".equals(s) || "http".equals(s))
                 && ("wa.me".equals(h) || "api.whatsapp.com".equals(h)
                     || "web.whatsapp.com".equals(h) || "www.whatsapp.com".equals(h)) );
        if (whatsapp) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE));
            } catch (Exception ignored) {
                // Allow the user to open a standard web WhatsApp URL when possible.
                if ("whatsapp".equals(s)) {
                    try {
                        String text = uri.getQueryParameter("text");
                        if (text != null) {
                            Uri fallback = Uri.parse("https://api.whatsapp.com/send").buildUpon()
                                .appendQueryParameter("text", text).build();
                            startActivity(new Intent(Intent.ACTION_VIEW, fallback));
                        }
                    } catch (Exception ignoredAgain) { }
                }
            }
            return true;
        }
        if ("http".equals(s) || "https".equals(s)) {
            if ("appassets.androidplatform.net".equals(h)) return false;
            try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
            catch (Exception ignored) { }
            return true;
        }
        if ("tel".equals(s) || "mailto".equals(s) || "sms".equals(s)) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
            catch (Exception ignored) { }
            return true;
        }
        return true; // Do not load unsupported schemes inside WebView.
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == FILE_PICK && upload != null) {
            upload.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result, data));
            upload = null;
        }
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}

