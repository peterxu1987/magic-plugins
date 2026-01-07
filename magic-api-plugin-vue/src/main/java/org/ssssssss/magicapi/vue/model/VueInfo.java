package org.ssssssss.magicapi.vue.model;

import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.model.PathMagicEntity;

public class VueInfo extends PathMagicEntity {

    /**
     * 是否启用
     */
    private boolean enabled;
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    @Override
    public MagicEntity simple() {
        VueInfo info = new VueInfo();
        super.simple(info);
        return info;
    }
    @Override
    public VueInfo copy() {
        VueInfo info = new VueInfo();
        super.copyTo(info);
        info.setEnabled(this.enabled);
        return info;
    }

}
