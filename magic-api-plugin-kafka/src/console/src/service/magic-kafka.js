export default (request, $i, modal, JavaClass) => {
	let findResources
	// 设置代码提示
	JavaClass.setExtensionAttribute('org.ssssssss.magicapi.kafka.KafkaModule', () => {
		return findResources && (findResources('kafka')[0]?.children || []).filter(it => it.key).map(it => {
			return {
				name: it.key,
				type: 'org.ssssssss.magicapi.kafka.KafkaModule',
				comment: it.name
			}
		}) || []
	})
	return {
		injectResources: fn => findResources = fn,
		requireScript: false,
		doTest: info => {
			request.sendJson('/kafka/jdbc/test', info).success(res => {
				if (res === 'ok') {
					modal.alert($i('kafka.connected'), $i('kafka.test'))
				} else {
					modal.alert($i('kafka.connectFailed', res), $i('kafka.test'))
				}
			})
		}
	}
}
