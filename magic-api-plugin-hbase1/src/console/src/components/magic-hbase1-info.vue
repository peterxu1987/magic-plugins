<template>
	<div class="magic-hbase-info">
		<form>
			 <div class="magic-form-row">
			       <label>{{$i('message.name')}}</label>
			       <magic-input v-model:value="info.name" :placeholder="$i('hbase1.form.placeholder.name')"/>
			   </div>
			   <div class="magic-form-row">
			      <label>key</label>
			       <magic-input v-model:value="info.key" :placeholder="$i('hbase1.form.placeholder.name')"/>
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
      zkUrls: "zookeeper单机或者集群的连接地址，如：bigdata001:2181,bigdata002:2181,bigdata003:2181",
      hbaseRootdir: "hbase 存储在 hadoop 上的路径，如：hdfs://bigdata001:9000/hbase； 存在本地磁盘：file:///tmp/hbase-data"
}, null, 2))

watch(properties, (val) => {
	try {
		__props.info.properties = JSON.parse(val)
	} catch (e) {
		__props.info.properties = {}
	}
})
</script>