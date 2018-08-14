package com.wya.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * 创建日期：2018/8/13 17:41
 * 作者： Mao Chunjiang
 * 文件名称： WYAWebView
 * 类说明：
 */

public class WYAWebView extends WebView {

    private Context context;
    private WebViewResult webViewResult;

    public WYAWebView(Context context, WebViewResult webViewResult) {
        super(context);
        this.context = context;
        this.webViewResult = webViewResult;
    }

    /**
     * 初始化WYAWebView
     *
     * @param html_path
     * @return
     */
    public WYAWebView init(String html_path) {
        //解决点击链接跳转浏览器问题
        this.setWebViewClient(new WebViewClient());
        //js支持
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        //允许访问assets目录
        settings.setAllowFileAccess(true);
        //设置WebView排版算法, 实现单列显示, 不允许横向移动
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //加载Html页面
        loadUrl(html_path);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webViewResult.onPageFinished();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webViewResult.onPageStarted();
                Log.e("onPageStarted", url);
            }

            //返回值：true 不会显示网页资源，需要等待你的处理，false 就认为系统没有做处理，会显示网页资源
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!TextUtils.isEmpty(url) && url.contains("command")) {
                    webViewResult.shouldOverrideUrlLoading(url);
                    return true;
                }
                return false;
            }


            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Log.e("shouldInterceptRequest", url);
                return super.shouldInterceptRequest(view, url);
            }
        });
        return this;
    }

    /**
     * 加载JS
     * @param url
     */
    public void wyaLoadUrl(String url) {
        loadUrl("javascript:" + url);
    }

    /**
     * 获取init-test.js
     *
     * @return
     */
    private String getInitJsStr() {
        String str = "";
        str = "var newscript = document.createElement(\"script\");";
        str += "newscript.src=\"file:///android_asset/init-test.js\";";
        str += "document.body.appendChild(newscript);";
        return str;
    }

    /**
     * 获取bridge-sdk.js
     *
     * @return
     */
    private String getSdkJsStr() {
        String str = "";
        str = "var newscript = document.createElement(\"script\");";
        str += "newscript.src=\"file:///android_asset/bridge-sdk.js\";";
        str += "document.body.appendChild(newscript);";
        return str;
    }

    /**
     * 注入初始化JS
     *
     * @param init_js_str
     */
    public void initJs(String init_js_str) {
        if (init_js_str != null && !init_js_str.equals("")) {
            wyaLoadUrl(init_js_str);
        } else {
            wyaLoadUrl(getInitJsStr());
        }
    }

    /**
     * 注入JS SDK
     *
     * @param sdk_js_str
     */
    public void initSdkJs(String sdk_js_str) {
        if (sdk_js_str != null && !sdk_js_str.equals("")) {
            wyaLoadUrl(sdk_js_str);
        } else {
            wyaLoadUrl(getSdkJsStr());
        }
    }

    /**
     * 延迟多久执行
     *
     * @param time
     */
    public void loadUrlAfter(final String sj_str, long time) {
        if (sj_str != null && !sj_str.equals("")) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    wyaLoadUrl(sj_str);
                }
            }, time);
        }
    }

    /**
     * 获取参数
     * @param js_get_param
     */
    public void getParam(String js_get_param) {
       getJsResult(js_get_param,1);
    }

    /**
     * @param js_str
     * @param type 1获取参数， 2提交数据
     * @return
     */
    @SuppressLint("NewApi")
    private void getJsResult(String js_str, final int type) {
        evaluateJavascript(js_str, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                //此处为 js 返回的结果
                if(type == 1){
                    webViewResult.paramResult(value);
                } else if(type == 2){
                    webViewResult.emitResult(value);
                }
            }
        });
    }


    /**
     * 提交数据
     * @param js_emit
     */
    public void emit(String js_emit) {
        getJsResult(js_emit,2);
    }
}
