import MagicTcp from './service/magic-tcp.js'
import localZhCN from './i18n/zh-cn.js'
import localEn from './i18n/en.js'
import MagicTcpInfo from './components/magic-tcp-info.vue'
import 'vite-plugin-svg-icons/register'
export default (opt) => {
	const i18n = opt.i18n
	// 添加i18n 国际化信息
	i18n.add('zh-cn', localZhCN)
	i18n.add('en', localEn)

	return {
		datasources: [{
			// 资源类型，和后端存储结构一致
			type: 'tcp',
			// 展示图标
			icon: '#magic-tcp-tcp',   // #开头表示图标在插件中
			// 展示标题
			title: 'Tcp',
			// 展示名称
			name: i18n.format('mqtt.name'),
			// 运行服务 request, $i, modal, JavaClass
			service: MagicTcp(opt.request, i18n.format , opt.modal, opt.JavaClass),
			// 表单组件
			component: MagicTcpInfo,

		}]
	}
}

