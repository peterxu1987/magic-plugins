import MagicRedis from './service/magic-redis.js'
import localZhCN from './i18n/zh-cn.js'
import localEn from './i18n/en.js'
import MagicRedisInfo from './components/magic-redis-info.vue'
import 'vite-plugin-svg-icons/register'
export default (opt) => {
	const i18n = opt.i18n
	// 添加i18n 国际化信息
	i18n.add('zh-cn', localZhCN)
	i18n.add('en', localEn)

	return {
		datasources: [{
			// 资源类型，和后端存储结构一致
			type: 'redis',
			// 展示图标
			icon: '#magic-redis-redis',   // #开头表示图标在插件中
			// 展示标题
			title: 'Redis',
			// 展示名称
			name: i18n.format('redis.name'),
			// 运行服务 request, $i, modal, JavaClass
			service: MagicRedis(opt.request, i18n.format , opt.modal, opt.JavaClass),
			// 表单组件
			component: MagicRedisInfo,

		}]
	}
}