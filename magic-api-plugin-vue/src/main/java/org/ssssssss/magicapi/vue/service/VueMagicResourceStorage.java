package org.ssssssss.magicapi.vue.service;

import org.ssssssss.magicapi.core.service.AbstractPathMagicResourceStorage;
import org.ssssssss.magicapi.vue.model.VueInfo;

public class VueMagicResourceStorage extends AbstractPathMagicResourceStorage<VueInfo> {
    @Override
    public String folder() {
        return "vue";
    }

    @Override
    public Class<VueInfo> magicClass() {
        return VueInfo.class;
    }

    @Override
    public void validate(VueInfo entity) {
    }

    @Override
    public String buildMappingKey(VueInfo info) {
        return buildMappingKey(info, info.getPath());
    }
}
