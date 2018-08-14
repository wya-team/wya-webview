
/**
 * bridge.js 注入的异步性，我们需要由客户端在注入完成后通知H5
 */
let isLoaded = false; // 标记位用于避免事件被重复触发(如加载iframe), 因此需要双方各做一层防御性措施(移动端未做)
window.addEventListener('_init_', (e = {}) => {
	// 1. 初始化机制
	if (isLoaded) return false;
	isLoaded = true;
	// 直接初始化传给H5一些环境参数和系统信息等
	e.data = {
		// 应用名；
		name: 'CloudFilter',
		// app版本号
		appVersion: '1.0.0',
		// H5包版本号;
		version: '1.1.0',
		// 平台： 1: ios, 2: android,
		platform: 2,
		// 地区；
		area: '中国大陆',
		// 语言；
		language: 'zh',
		// 当前App环境； 0: release; 1: pre; 2: dev;
		env: 2,
		data: {}
	};

	// App很经常性的隐藏到后台，H5页面被激活
	window.addEventListener('_resume_', e => {

	});

	// 2. 包更新机制, 这里要获取线上的接口，根据当前版本，确定版本号
	JSBridge.on('downloadModule', {
		// 应用名称；
		module: 'wya-yzws',
		// 最后包线上提示；
		url: 'https://www.weiyian.com/bridge.js',
	});

	/**
	 * 3. 事件中转机制
	 */
	// 事件中心, 如单页系统（SPA）返回
	window.addEventListener('_eventListeners_', e => {
		const { type } = e.data.type;
		switch (type) {
			case 'back':
				// 关闭弹窗：this.closeDialog();
				// 退回页面：this.goLastPage();
				break;
			case 'hideLoading':
				// 隐藏loading: this.hideLoading();
				break
			defalut:
				break
		}
	})

	/**
	 * 4. 数据传递机制
	 */
	// Native 与 H5 保持数据的同步
	// 推送数据；
	JSBridge.on('putData', {
		a: 1,
		b: 2,
		c: 3,
	});
	// 监听数据通道；
	window.addEventListener('getData', e => {
		// tyoe: 代表数据类型，可自行约定；
		// data: 数据
		const { type, data } = e.data;
		switch (type) {
			case 'list':
				// 获取客服端传递过来的列表数据 data;
				// ...
				break;
			default:
				break;
		}
	});

	/**
	 * 5. 代理请求机制
	 * 定制4个协议: getProxy，postProxy， getProxyLogined，postProxyLogined
	 * 其中带有 Logined 的协议代表着在请求时会自动携带已登录用户的 token 和 uid 等参数，使用在一些需要登录信息的接口上
	 */
	JSBridge.on('getProxy', {
		url: '',
		data: '',
		headers: ''
	}, e => {
		if (e.data && e.data.code == 110) {
			// 请求失败；
		} else {
			// 请求成功，返回数据 e.data;
		}
	});

	// getNetwork：获取网络状态；
	// openApp：唤起其它 App；
	// setShareInfo与callShare：分享内容到第三方平台；
	// link：使用新的 WebView 打开页面；
	// closeWebview：关闭 WebView；
	// setStorage 与 getStorage：设置与获取缓存数据；
	// loading：调用客户端通用 Loading；
	// setWebviewTitle：设置 WebView 标题；
	// saveImage：保存图片到本地；
	// 
	// 测试
	document.addEventListener('click', () => {
		try {
			// 代码写这里
            JSBridge.on('getNetWork', { test: 1 }, (e) => {
				alert(e);
			});
		} catch (e) {
			alert('错误');
		}
	});
}, false);
