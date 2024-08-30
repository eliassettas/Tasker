package com.example.tasker.model.dto;

import com.example.tasker.model.persistence.BaseType;

public class TypeDTO {

    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static TypeDTO fromObject(BaseType baseType) {
        TypeDTO typeDTO = new TypeDTO();
        typeDTO.setName(baseType.getName());
        typeDTO.setDescription(baseType.getDescription());
        return typeDTO;
    }
}
