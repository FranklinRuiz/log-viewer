package com.log.view.persistence.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataModel {
    private String origin;
    private String logLevel;
    private String serviceType;
    private String service;
    private String destination;
}
