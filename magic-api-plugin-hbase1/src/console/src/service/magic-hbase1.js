export default (request, $i, modal, JavaClass) => {
	let findResources
	// 设置代码提示
	JavaClass.setExtensionAttribute('org.ssssssss.magicapi.hbase1.HbaseModule', () => {
		return findResources && (findResources('hbase1')[0]?.children || []).filter(it => it.key).map(it => {
			return {
				name: it.key,
				type: 'org.ssssssss.magicapi.hbase1.HbaseModule',
				comment: it.name
			}
		}) || []
	})
	return {
		injectResources: fn => findResources = fn,
		requireScript: false,
		doTest: info => {
			request.sendJson('/hbase1/jdbc/test', info).success(res => {
				if (res === 'ok') {
					modal.alert($i('hbase1.connected'), $i('hbase1.test'))
				} else {
					modal.alert($i('hbase1.connectFailed', res), $i('hbase1.test'))
				}
			})
		}
	}
}
