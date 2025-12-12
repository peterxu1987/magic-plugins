<template>
	<div class="magic-redis-info">
		<form>
			<div class="magic-form-row">
			       <label>{{$i('message.name')}}</label>
			       <magic-input v-model:value="info.name" :placeholder="$i('redis.form.placeholder.name')"/>
			   </div>
			   <div class="magic-form-row">
			      <label>key</label>
			       <magic-input v-model:value="info.key" :placeholder="$i('redis.form.placeholder.name')"/>
			   </div>
			   <div class="magic-form-row">
			       <label>{{$i('redis.form.type')}}</label>
			       <magic-select inputable  @update:value="handleChange" v-model:value="info.type" width="100%" :options="constants.map(it => { return {text: it, value: it} })" :placeholder="$i('redis.form.placeholder.type')"/>
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
const constants = ref([
	'single',
	'sentinel',
	'cluster'
])
const { info } = defineProps({
    info: Object
})

const properties = ref(JSON.stringify(__props.info.properties || {}, null, 2))

const handleChange = (value) => {
    if(value == "single"){
        properties.value = JSON.stringify({
         /*   host: "IP地址",
            port: "端口",
            password: "密码",
            database: "指定数据库，默认为0",
            pool: {
                "max-active": "最大活跃连接数，默认8个,建议100",
                "max-idle": "最大空闲连接数，默认8个,建议100",
                "min-idle": "最小空闲连接数，默认0,建议100",
                "max-wait": "获取连接的最大等待时间，默认-1，表示无限制，单位毫秒。建议1000"
            }*/
      
            "host": "172.16.250.42",
            "port": "6060",
            "password": "1HNMDG4s5fz8",
            "database": "0",
            "pool": {
                "max-active": "100",
                "max-idle": "100",
                "min-idle": "100",
                "max-wait": "1000"
            }
        }, null, 2)
    }else if(value == "sentinel"){
        properties.value = JSON.stringify({
            master: "",
            nodes: ["ip:port","ip:port"],
            password: "密码",
            pool:{
                "max-active": "最大活跃连接数，默认8个,建议100",
                "max-idle": "最大空闲连接数，默认8个,建议100",
                "min-idle": "最小空闲连接数，默认0,建议100",
                "max-wait": "获取连接的最大等待时间，默认-1，表示无限制，单位毫秒。建议1000"
            }
        }, null, 2)
    }else if(value == "cluster"){
        properties.value = JSON.stringify({
            nodes: ["ip:port","ip:port"],
            password: "密码",
            pool:{
                "max-active": "最大活跃连接数，默认8个,建议100",
                "max-idle": "最大空闲连接数，默认8个,建议100",
                "min-idle": "最小空闲连接数，默认0,建议100",
                "max-wait": "获取连接的最大等待时间，默认-1，表示无限制，单位毫秒。建议1000"
            }
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