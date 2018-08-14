# wya-webview
### 1、[功能描述]：
##### android与JS的交互


### 2、[项目结构简介]：
##### （1）example为demo,里面主要包括使用方式，如何调用；
##### （2）library为依赖Module，第三方库引用的文件都在library文件夹里
##### （3）其他文件默认提交，不做说明


### 3、[测试DEMO]：
##### 调用WyaWebView代码如下
      
        代码调用
          wyaWebView = new WYAWebView(this, new WebViewResult() {
          /**
           * 获取参数回调
           * @param value
           */
          @Override
          public void paramResult(String value) {
              Log.e("paramResult", value);
              Toast.makeText(MainActivity.this, "paramResult:" + value, Toast.LENGTH_SHORT).show();
          }

          /**
           * 页面加载完毕回调
           */
          @Override
          public void onPageFinished() {
              wyaWebView.initJs(init_js_str);
              wyaWebView.initSdkJs(sdk_js_str);
              js_str = "JSBridge.emit('_init_',{ version: '2.0.0' })";
              wyaWebView.loadUrlAfter(js_str, 500);
          }

          /**
           * 加载网页前的回调
           */
          @Override
          public void onPageStarted() {

          }

          /**
           * 拦截url, 包含command
           * @param url
           */
          @Override
          public void shouldOverrideUrlLoading(String url) {
              Uri uri = Uri.parse(url);
              id = uri.getQueryParameter("id");
              Log.e("url", url);
              Log.e("id", id);
              Toast.makeText(MainActivity.this, "url:" + url, Toast.LENGTH_SHORT).show();
          }

          /**
           * 提交数据后回调
           * @param value
           */
          @Override
          public void emitResult(String value) {
              Log.e("emitResult", value);
              Toast.makeText(MainActivity.this, "emitResult:" + value, Toast.LENGTH_SHORT).show();
          }
        }).init("file:///android_asset/index.html");
        //将webview添加到布局中
        parent = (FrameLayout) findViewById(R.id.parent);
        parent.addView(wyaWebView);
        
        
##### WyaWebView说明

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

### 4、[历史版本]：
##### 2018.8.14
    *完成自定义导航栏功能，其中包括Fragment切换功能，版本v1.0.0
    *引用方式：   
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
    dependencies {
	        implementation 'com.github.wya-team:wya-webview:v1.0.0'
	}


### 5、[联系方式]：
##### email：550612711@qq.com 对这个工程不明白的地方可以通过该联系方式与我联系。
