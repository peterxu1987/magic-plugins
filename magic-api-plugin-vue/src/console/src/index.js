import MagicVue from './service/magic-vue.js'
import localZhCN from './i18n/zh-cn.js'
import localEn from './i18n/en.js'
import MagicVueInfo from './components/magic-vue-info.vue'
import 'vite-plugin-svg-icons/register'
import setVue from './service/vue.js'
export default (opt) => {
	const i18n = opt.i18n
	// 添加i18n 国际化信息
	i18n.add('zh-cn', localZhCN)
	i18n.add('en', localEn)
	
    setVue(opt.monaco)

	return {
		resource: [{
			// 资源类型，和后端存储结构一致
			type: 'vue',
			// 展示图标
			icon: '#magic-vue-vue',   // #开头表示图标在插件中
			// 展示标题
			title: 'vue.name',
			service: MagicVue(opt.bus, opt.constants, i18n.format, opt.Message, opt.request),

		}],
		// 底部工具条
        toolbars: [{
            // 当打开的资源类型为 vue 时显示
            type: 'vue',
            // 工具条展示的标题
            title: 'vue.title',
            // 展示图标
            icon: 'vue',
            // 对应的组件
            component: MagicVueInfo,
        }]
	}
}

