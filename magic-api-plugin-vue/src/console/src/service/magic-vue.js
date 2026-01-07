export default function (bus, constants, $i, Message, request) {
    return {
        // svg text
        getIcon: item => ['VUE', '#9012FE'],
        // 任务名称
        name: $i('vue.name'),
        // 脚本语言
        language: 'vue',
        // 默认脚本
        defaultScript: `<template>

</template>

<script setup>
import { ref, reactive, nextTick } from 'vue'
const hasPermission = (permission) =>{
   return $common.hasPermission(permission)
}
const userInfo = $common.getUserInfo();
const token = $common.getToken();
$common.getDictData() // 重新加载字典数据
const dictTypes = $common.getDictType('dict_type');// 获取字典类型的数组值
const dictTypeMap = $common.getDictTypeMapData('dict_type');// 获取字典类型的Map对象值
</script>

<style scoped>

</style>`,
        automaticLayout: true,
        formatOnPaste: true,
        formatOnType: true,
        // 执行测试的逻辑
        // 是否允许执行测试
        runnable: false,
        // 是否需要填写路径
        requirePath: false,
        // 合并
        merge: item => item
    }
}