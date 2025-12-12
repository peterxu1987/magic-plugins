<template>
	<div class="magic-tcp-info">
		<form>
			<div class="magic-form-row">
			        <label>{{$i('message.name')}}</label>
			        <magic-input v-model:value="info.name" :placeholder="$i('tcp.form.placeholder.name')"/>
			    </div>
			    <div class="magic-form-row">
			       <label>key</label>
			        <magic-input v-model:value="info.key" :placeholder="$i('tcp.form.placeholder.name')"/>
			    </div>
			    <div class="magic-form-row">
			        <label>{{$i('tcp.form.type')}}</label>
			        <magic-select inputable  @update:value="handleChange" v-model:value="info.type" width="100%" :options="constants.TCP_TYPES.map(it => { return {text: it, value: it} })" :placeholder="$i('tcp.form.placeholder.type')"/>
			    </div>
			 
			    <div class="magic-form-row">
			        <label>{{$i('datasource.form.other')}}</label>
			        <magic-monaco-editor language="json" v-model:value="properties" style="height:150px"/>
			    </div>
		</form>
	</div>
</template>
<script setup>
import { ref, watch, inject,defineProps } from 'vue'
const $i = inject('i18n.format')
const constants = inject('constants')
const { info } = defineProps({
    info: Object
})

const properties = ref(JSON.stringify(__props.info.properties || {}, null, 2))

const handleChange = (value) => {
    if(value == "client"){
        properties.value = JSON.stringify({
            "ip": "连接服务端的ip地址",
            "port": "服务端，端口",
            "reconnect": "端口是否重连，true/false",
            "vehId": "签发证书时候的设备唯一编号(如果不认证，以下配置都设置为空就行)",
            "certBasePath": "ca文件的根路径",
            "sslPassword": "ca的验证密码"
        }, null, 2)
    }else if(value == "server"){
        properties.value = JSON.stringify({
            "port": "服务端，端口",
            "sslPassword": "ca的验证密码",
            "certBasePath": "ca文件的根路径"
        }, null, 2)
    }
}

watch(properties, (val) => {
	try {
		__props.info.properties = JSON.parse(val)
	} catch (e) {
		__props.info.properties = {}
	}
})
</script>