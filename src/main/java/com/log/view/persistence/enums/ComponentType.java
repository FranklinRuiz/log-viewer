package com.log.view.persistence.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ComponentType {
    MAINFRAME("#036897", "#ffffff", "mxgraph.cisco.computers_and_peripherals.ibm_mainframe", ""),
    WEBSITE("#7D7D7D", "", "mxgraph.mscae.enterprise.website_generic", ""),
    SQL_DATABASE("#00BEF2", "", "mxgraph.azure.sql_database", ""),
    POD("#2875E2", "#ffffff", "mxgraph.kubernetes.icon", "pod");

    private String fill;
    private String stroke;
    private String shape;
    private String icon;
}
