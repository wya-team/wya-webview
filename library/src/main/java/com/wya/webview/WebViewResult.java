package com.wya.webview;

/**
 * 创建日期：2018/8/14 11:08
 * 作者： Mao Chunjiang
 * 文件名称： WebViewResult
 * 类说明：WebViewResult返回数据回调
 */

public interface WebViewResult {

    void paramResult(String value);

    void onPageFinished();

    void onPageStarted();

    void shouldOverrideUrlLoading(String url);

    void emitResult(String value);
}
