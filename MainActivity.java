package com.mayatours.intelkg;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;
public class MainActivity extends Activity {
 WebView web; ValueCallback<Uri[]> upload; static final int FILE_PICK=42;
 @Override public void onCreate(Bundle b){super.onCreate(b);web=new WebView(this);setContentView(web);
 web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);
 web.getSettings().setAllowFileAccess(true);web.setWebViewClient(new WebViewClient());
 web.setWebChromeClient(new WebChromeClient(){@Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> callback,FileChooserParams params){upload=callback;startActivityForResult(params.createIntent(),FILE_PICK);return true;}});
 web.loadUrl("file:///android_asset/index.html");}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==FILE_PICK&&upload!=null){upload.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));upload=null;}}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}