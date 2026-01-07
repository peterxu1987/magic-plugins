package org.ssssssss.magicapi.vue.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.event.GroupEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.vue.model.VueInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VueMagicDynamicRegistry extends AbstractMagicDynamicRegistry<VueInfo> {
	
	private final Map<String, MappingNode<VueInfo>> mappings = new ConcurrentHashMap<>();

    public VueMagicDynamicRegistry(
            MagicResourceStorage<VueInfo> magicResourceStorage) {
        super(magicResourceStorage);
    }

    @EventListener(condition = "#event.type == 'vue'")
    public void onFileEvent(FileEvent event) {
        processEvent(event);
    }

    @EventListener(condition = "#event.type == 'vue'")
    public void onGroupEvent(GroupEvent event) {
        processEvent(event);
    }

    @Override
    public boolean register(VueInfo entity) {
        unregister(entity);
        return super.register(entity);
    }

    @Override
    protected boolean register(MappingNode<VueInfo> mappingNode) {
    	VueInfo info = mappingNode.getEntity();
        if (info.isEnabled()) {
        	mappings.put(info.getName(), mappingNode);
        }else{
            unregister(mappingNode);
        }
        return true;
    }

    @Override
    protected void unregister(MappingNode<VueInfo> mappingNode) {
    	VueInfo info = mappingNode.getEntity();
        log.debug("取消注册VUE文件:{}", info.getName());
        mappings.remove(info.getName());
    }
    
	public VueInfo getMapping(String mappingKey) {
		MappingNode<VueInfo> node = mappings.get(mappingKey);
		return node == null ? null : node.getEntity();
	}
 
}
