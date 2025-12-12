export default {
     redis:{
        title: 'Redis',
        name: '缓存数据源',
        copySuccess: '复制{0}成功',
        copyFailed: '复制{0}失败',
        test: '测试连接',
        connected: '连接成功',
        connectFailed: '连接失败，错误原因：\r\n{0}',
        type:'类型',
        form: {
            placeholder: {
                name: '数据源名称，仅做显示使用',
                key: '数据源Key，后续代码中使用',
                type: '模式类型：single、sentinel、cluster'
            },
            type: '类型',
        }
    }
}