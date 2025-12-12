import MagicHbase1 from './service/magic-hbase1.js'
import localZhCN from './i18n/zh-cn.js'
import localEn from './i18n/en.js'
import MagicHbase1Info from './components/magic-hbase1-info.vue'
import 'vite-plugin-svg-icons/register'
export default (opt) => {
	const i18n = opt.i18n
	// 添加i18n 国际化信息
	i18n.add('zh-cn', localZhCN)
	i18n.add('en', localEn)

	return {
		datasources: [{
			// 资源类型，和后端存储结构一致
			type: 'hbase1',
			// 展示图标
			icon: '#magic-hbase1-hbase1',   // #开头表示图标在插件中
			// 展示标题
			title: 'Hbase1.x',
			// 展示名称
			name: i18n.format('hbase1.name'),
			// 运行服务 request, $i, modal, JavaClass
			service: MagicHbase1(opt.request, i18n.format , opt.modal, opt.JavaClass),
			// 表单组件
			component: MagicHbase1Info,

		}]
	}
}

