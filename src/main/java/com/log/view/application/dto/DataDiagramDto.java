package com.log.view.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataDiagramDto {
    private String component;
    private String fill;
    private String stroke;
    private String shape;
    private String icon;
    private String refs;
    private String text;
}
