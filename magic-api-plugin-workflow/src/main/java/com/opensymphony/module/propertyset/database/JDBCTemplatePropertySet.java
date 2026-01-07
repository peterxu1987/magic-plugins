package com.opensymphony.module.propertyset.database;

import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.util.ClassLoaderUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Map;

public class JDBCTemplatePropertySet extends AbstractPropertySet {
    protected static Log log = LogFactory.getLog(JDBCTemplatePropertySet.class);

    private JDBCTemplateConfigurationProvider configProvider;
    private JDBCTemplatePropertySetDAO dao;


    @SuppressWarnings("rawtypes")
	public void init(Map config, Map args) {
        log.debug("(JDBCTemplatePropertySet) top of init.");

        super.init(config, args);

        // first let's see if we got given a configuration provider to use
        // already
        this.configProvider = (JDBCTemplateConfigurationProvider) args.get("configurationProvider"); //$NON-NLS-1$

        /* if we did not get given one in the args, we need to set a config provider up */
        if (this.configProvider == null) {
            // lets see if we need to use a configurationProvider from a class
            String configProviderClass = (String) config.get("configuration.provider.class");//$NON-NLS-1$
            if (configProviderClass != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting up property set provider of class: " + configProviderClass);//$NON-NLS-1$
                }

                try {
                    this.configProvider = (JDBCTemplateConfigurationProvider) ClassLoaderUtil
                            .loadClass(configProviderClass, this.getClass()).newInstance();
                }
                catch (Exception e) {
                    log.error("Unable to load configuration provider class: " + configProviderClass, e);//$NON-NLS-1$
                    return;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Setting up property set with DefaultJDBCTemplateConfigurationProvider");//$NON-NLS-1$
                }
                this.configProvider = new DefaultJDBCTemplateConfigurationProvider();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Setting up property set with jdbcTemplate provider passed in args."); //$NON-NLS-1$
            }
        }

        dao = configProvider.getPropertySetDAO();

        dao.setGlobalKey((String) args.get("globalKey"));

        dao.setTableName((String) config.get("table.name"));
        dao.setColGlobalKey((String) config.get("col.globalKey"));
        dao.setColItemKey((String) config.get("col.itemKey"));
        dao.setColItemType((String) config.get("col.itemType"));
        dao.setColString((String) config.get("col.string"));
        dao.setColDate((String) config.get("col.date"));
        dao.setColData((String) config.get("col.data"));
        dao.setColFloat((String) config.get("col.float"));
        dao.setColNumber((String) config.get("col.number"));

    }

    public Collection<?> getKeys(String prefix, int type) throws PropertyException {
        return dao.getKeys(prefix, type);
    }

    public int getType(String key) throws PropertyException {
        return dao.getType(key);
    }

    public boolean exists(String key) throws PropertyException {
        return dao.exists(key);
    }

    public void remove(String key) throws PropertyException {
        dao.remove(key);
    }

    protected void setImpl(int type, String key, Object value) throws PropertyException {
        dao.setImpl(type, key, value);
    }

    protected Object get(int type, String key) throws PropertyException {
        return dao.get(type, key);
    }

    public void remove() throws PropertyException {

    }
}
