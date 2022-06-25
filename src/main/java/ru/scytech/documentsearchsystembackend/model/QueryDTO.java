package ru.scytech.documentsearchsystembackend.model;

import lombok.Data;

import java.util.List;

@Data
public class QueryDTO {
    private String query;
    private List<QueryFlags> queryFlags;
}
