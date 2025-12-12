import MagicEs from './service/magic-es.js'
import localZhCN from './i18n/zh-cn.js'
import localEn from './i18n/en.js'
import MagicEsInfo from './components/magic-es-info.vue'
import 'vite-plugin-svg-icons/register'
export default (opt) => {
	const i18n = opt.i18n
	// 添加i18n 国际化信息
	i18n.add('zh-cn', localZhCN)
	i18n.add('en', localEn)

	return {
		datasources: [{
			// 资源类型，和后端存储结构一致
			type: 'es',
			// 展示图标
			icon: '#magic-es-es',   // #开头表示图标在插件中
			// 展示标题
			title: 'Es',
			// 展示名称
			name: i18n.format('es.name'),
			// 运行服务 request, $i, modal, JavaClass
			service: MagicEs(opt.request, i18n.format , opt.modal, opt.JavaClass),
			// 表单组件
			component: MagicEsInfo,

		}]
	}
}

