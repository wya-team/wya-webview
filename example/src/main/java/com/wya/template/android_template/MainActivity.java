package com.wya.template.android_template;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wya.webview.WYAWebView;
import com.wya.webview.WebViewResult;

public class MainActivity extends AppCompatActivity {

    private TextView tv_get_param, tv_emit;
    private WYAWebView wyaWebView;
    private FrameLayout parent;

    private String init_js_str;
    private String sdk_js_str;
    private String js_str;

    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWyaWebView();

        initClick();


    }

    private void initClick() {
        tv_emit = (TextView) findViewById(R.id.tv_emit);
        tv_get_param = (TextView) findViewById(R.id.tv_get_param);

        tv_get_param.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wyaWebView.getParam("javascript:JSBridge.getParam(" + id + ")");
            }
        });
        tv_emit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wyaWebView.emit("javascript:JSBridge.emit(" + id +","+ 123 +")");
            }
        });
    }

    /**
     * 初始化WyaWebView
     */
    private void initWyaWebView() {
        parent = (FrameLayout) findViewById(R.id.parent);
        wyaWebView = new WYAWebView(this, new WebViewResult() {
            @Override
            public void paramResult(String value) {
                Log.e("paramResult", value);
                Toast.makeText(MainActivity.this, "paramResult:" + value, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished() {
                wyaWebView.initJs(init_js_str);
                wyaWebView.initSdkJs(sdk_js_str);
                js_str = "JSBridge.emit('_init_',{ version: '2.0.0' })";
                wyaWebView.loadUrlAfter(js_str, 500);
            }

            @Override
            public void onPageStarted() {

            }

            @Override
            public void shouldOverrideUrlLoading(String url) {
                Uri uri = Uri.parse(url);
                id = uri.getQueryParameter("id");
                Log.e("url", url);
                Log.e("id", id);
                Toast.makeText(MainActivity.this, "url:" + url, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void emitResult(String value) {
                Log.e("emitResult", value);
                Toast.makeText(MainActivity.this, "emitResult:" + value, Toast.LENGTH_SHORT).show();
            }
        }).init("file:///android_asset/index.html");
        parent.addView(wyaWebView);
    }
}
