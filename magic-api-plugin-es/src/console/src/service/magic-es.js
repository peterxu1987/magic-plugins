export default (request, $i, modal, JavaClass) => {
	let findResources
	// 设置代码提示
	JavaClass.setExtensionAttribute('org.ssssssss.magicapi.elasticsearch.ESModule', () => {
		return findResources && (findResources('es')[0]?.children || []).filter(it => it.key).map(it => {
			return {
				name: it.key,
				type: 'org.ssssssss.magicapi.elasticsearch.ESModule',
				comment: it.name
			}
		}) || []
	})
	return {
		injectResources: fn => findResources = fn,
		requireScript: false,
		doTest: info => {
			request.sendJson('/es/jdbc/test', info).success(res => {
				if (res === 'ok') {
					modal.alert($i('es.connected'), $i('es.test'))
				} else {
					modal.alert($i('es.connectFailed', res), $i('es.test'))
				}
			})
		}
	}
}
