export default (request, $i, modal, JavaClass) => {
	let findResources
	// 设置代码提示
	JavaClass.setExtensionAttribute('org.ssssssss.magicapi.redis.RedisModule', () => {
		return findResources && (findResources('redis')[0]?.children || []).filter(it => it.key).map(it => {
			return {
				name: it.key,
				type: 'org.ssssssss.magicapi.redis.redis',
				comment: it.name
			}
		}) || []
	})
	return {
		injectResources: fn => findResources = fn,
		requireScript: false,
		doTest: info => {
			request.sendJson('/redis/jdbc/test', info).success(res => {
				if (res === 'ok') {
					modal.alert($i('mqtt.connected'), $i('mqtt.test'))
				} else {
					modal.alert($i('mqtt.connectFailed', res), $i('mqtt.test'))
				}
			})
		}
	}
}
