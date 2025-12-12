<template>
	<div class="magic-es-info">
		<form>
			<div class="magic-form-row">
			       <label>{{$i('message.name')}}</label>
			       <magic-input v-model:value="info.name" :placeholder="$i('es.form.placeholder.name')"/>
			   </div>
			   <div class="magic-form-row">
			      <label>key</label>
			       <magic-input v-model:value="info.key" :placeholder="$i('es.form.placeholder.name')"/>
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

const properties = ref(JSON.stringify(__props.info.properties || {
      address: "es 集群的 地址列表（ip:port，。。。）",
      username: "认证用户名称",
      password: "认证密码",
      connectTimeout: "建立连接的超时时间（毫秒），建议：5000",
      socketTimeout: "数据传输超时（毫秒）,建议：30000",
      connectionRequestTimeout: "从连接池获取连接的超时（毫秒）,建议：5000",
      maxConnTotal: "最大总连接数（默认20，高并发需调大）,建议：30",
      maxConnPerRoute:"每个路由（节点）的最大连接数（默认10，高并发需调大）,建议：上面这个参数的一半 15"
}, null, 2))

watch(properties, (val) => {
	try {
		__props.info.properties = JSON.parse(val)
	} catch (e) {
		__props.info.properties = {}
	}
})
</script>