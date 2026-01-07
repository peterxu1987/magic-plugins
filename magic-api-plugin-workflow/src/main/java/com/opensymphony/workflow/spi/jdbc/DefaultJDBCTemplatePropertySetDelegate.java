package com.opensymphony.workflow.spi.jdbc;

import java.util.HashMap;
import java.util.Map;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.opensymphony.module.propertyset.database.DefaultJDBCTemplateConfigurationProvider;
import com.opensymphony.workflow.util.PropertySetDelegate;
@SuppressWarnings({"rawtypes","unchecked"})
public class DefaultJDBCTemplatePropertySetDelegate implements PropertySetDelegate {

    public DefaultJDBCTemplatePropertySetDelegate() {
        super();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public PropertySet getPropertySet(String entryId) {
        Map args = new HashMap(1);
        args.put("globalKey", "osff_temp_" + entryId);

        DefaultJDBCTemplateConfigurationProvider configurationProvider = new DefaultJDBCTemplateConfigurationProvider();
        args.put("configurationProvider", configurationProvider);

        return PropertySetManager.getInstance("jdbcTemplateSet", args);
    }
}
