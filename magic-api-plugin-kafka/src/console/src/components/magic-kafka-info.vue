<template>
	<div class="magic-kafka-info">
		<form>
			<div class="magic-form-row">
			       <label>{{$i('message.name')}}</label>
			       <magic-input v-model:value="info.name" :placeholder="$i('kafka.form.placeholder.name')"/>
			   </div>
			   <div class="magic-form-row">
			      <label>key</label>
			       <magic-input v-model:value="info.key" :placeholder="$i('kafka.form.placeholder.name')"/>
			   </div>
			   <div class="magic-form-row">
			       <label>{{$i('kafka.form.type')}}</label>
			       <magic-select inputable  @update:value="handleChange" v-model:value="info.type" width="100%" :options="constants.map(it => { return {text: it, value: it} })" :placeholder="$i('kafka.form.placeholder.type')"/>
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
			'string',
			'byteArray'
		])
const { info } = defineProps({
    info: Object
})
const properties = ref(JSON.stringify(__props.info.properties || {}, null, 2))
const handleChange = (value) => {
    if(value == "string"){
        properties.value = JSON.stringify({
            serverConfig: "Kafka 集群的 broker 地址列表",
            batchSize: "生产者批量发送消息的字节数上限，整数（单位：字节）, 默认 16384（16KB）， 32768（32KB）较合理，高吞吐场景可增至 65536（64KB），但会增加延迟",
            bufferMemory: "生产者缓冲区的总内存大小，整数（单位：字节），推荐 67108864（64MB），高吞吐场景可设为 134217728（128MB）",
            autoOffsetReset: "latest（默认）：从最新消息开始消费；earliest：从最早消息开始消费；",
            linger: "等待xx ms以填充批次，建议值 20",
            valueDeserializer: "string"
        }, null, 2)
    }else {
         properties.value = JSON.stringify({
            serverConfig: "Kafka 集群的 broker 地址列表",
            batchSize: "生产者批量发送消息的字节数上限，整数（单位：字节）, 默认 16384（16KB）， 32768（32KB）较合理，高吞吐场景可增至 65536（64KB），但会增加延迟",
            bufferMemory: "生产者缓冲区的总内存大小，整数（单位：字节），推荐 67108864（64MB），高吞吐场景可设为 134217728（128MB）",
            autoOffsetReset: "latest（默认）：从最新消息开始消费；earliest：从最早消息开始消费；",
            linger: "等待xx ms以填充批次，建议值 20",
            valueDeserializer: "byteArray"
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