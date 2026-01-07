package com.opensymphony.module.propertyset.database;


public class DefaultJDBCTemplateConfigurationProvider implements JDBCTemplateConfigurationProvider {


    private JDBCTemplatePropertySetDAO propertySetDAO;

    public JDBCTemplatePropertySetDAO getPropertySetDAO() {
        if (this.propertySetDAO == null) {
            JDBCTemplatePropertySetDAOImpl dao = new JDBCTemplatePropertySetDAOImpl();
            this.propertySetDAO = dao;
        }

        return this.propertySetDAO;
    }
}
