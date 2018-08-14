;(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
	typeof define === 'function' && define.amd ? define(factory) :
	(global.JSBridge = factory());
}(this || window, (function () { 
	'use strict';
	const isFn = (target) => typeof target === 'function';
	const doc = document || window.document || {};
	const win = window || {};
	const store = {
		// [handler]: {
		//     eventName: ev__handler,
		//     params: {},
		//     callback: () => {}
		// }
	};
	/**
	 * on/emit/off SDK内部使用
	 * H5 在接收到唯一标识后初始化对应的自定义事件，挂载数据后触发，这里涉及的就是 emit 这个函数
	 */
	const on = (id) => {
		const { eventName = id, callback } = store[id] || {};
		win.addEventListener(eventName, callback)
	};
	const emit = (id, data) => {
		const { eventName = id } = store[id] || {};
		// 创建自定义对象事件;
		let eventItem;
		if (isFn(doc.CustomEvent)) {
			eventItem = new doc.CustomEvent(eventName, {
				bubbles: true,
				cancelable: true
			})
		} else if (isFn(doc.createEvent)) {
			eventItem = doc.createEvent('Event');
			eventItem.initEvent(eventName, true, true);
		}
		
		// 将数据挂载到事件对象中
		if (data && eventItem) {
			eventItem.data = data;
		}
		
		// 触发自定义事件
		if (eventItem) {
			win.dispatchEvent(eventItem);
		} else {
			console.log('Bridge Error: dispatchEvent');
		}
	}
	const off = (id) => {
		const { eventName, callback } = store[id] || {};
		win.removeEventListener(eventName, callback);
		delete store[id];
	};
	/**
	 * send SDK内部使用
	 * 发送协议
	 * @param  id
	 * @return this
	 */
	const send = (id = throwError(), scheme) => {
		const { eventName } = store[id] || {};
		setTimeout(() => {
			// 创建 iframe 并设置src
			const iframe = document.createElement('iframe');
			iframe.src = `command://${scheme}?id=${id}`;
			iframe.style.display = 'none';
			document.body.appendChild(iframe);
			
			// 延迟删除节点
			setTimeout(() => {
				iframe.parentNode.removeChild(iframe);
			}, 300);
		}, 0);
	}

	let JSBridge = {
	};
	/**
	 * on 业务层调用
	 * @param  string   scheme   协议
	 * @param  object   params   参数
	 * @param  function callback 回调
	 * @return null
	 */
	let count = 0;
	JSBridge.on = (scheme = throwError(), params, callback = () => {}) => {
		// 对参数进行字符串化，并进行编码；
		params = params ? decodeURIComponent(JSON.stringify(params)) : '';
		
		// 生成唯一 id 标识
		const id = count++;
		store[id] = {
			params,
			callback: e => {
				const { data, id } = e.data;
				// 完成一次完整的交互时，将该自定义事件解绑；
				off(id);
				callback(data);
			},
			eventName: `${scheme}__${id}`
		};
		// 注册自定义事件，并绑定回调
		// 回调会在接收到`JSBridge.emit`时被触发执行；
		on(id);
		send(id, scheme);
	}

	/**
	 * emit Native层调用
	 * Native 完成功能后，直接调用 JSBridge.emit(id, data)，将执行结果和之前nativeCall传过来的标识回传给H5
	 * @param  object e 
	 * @return this
	 */
	JSBridge.emit = (id, data) => {
		// 创建并触发自定义事件
		emit(id, data);
	}
	/**
	 * getParam Native层调用
	 * 客户端接收到请求后，会使用 id 调用 getParam 从参数池中获取对应的参数
	 * @param  number id 唯一标识符
	 * @return string
	 */
	JSBridge.getParam = (id) => {
        const { params } = store[id] || {};
        return params || {};
	};
	return JSBridge;
})));
