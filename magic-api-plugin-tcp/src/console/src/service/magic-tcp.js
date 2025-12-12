export default (request, $i, modal, JavaClass) => {
	let findResources
	// 设置代码提示
	JavaClass.setExtensionAttribute('org.ssssssss.magicapi.tcp.TcpModule', () => {
		return findResources && (findResources('tcp')[0]?.children || []).filter(it => it.key).map(it => {
			return {
				name: it.key,
				type: 'org.ssssssss.magicapi.tcp.TcpModule',
				comment: it.name
			}
		}) || []
	})
	return {
		injectResources: fn => findResources = fn,
		requireScript: false,
		doTest: info => {
			request.sendJson('/tcp/jdbc/test', info).success(res => {
				if (res === 'ok') {
					modal.alert($i('tcp.connected'), $i('tcp.test'))
				} else {
					modal.alert($i('tcp.connectFailed', res), $i('tcp.test'))
				}
			})
		}
	}
}
