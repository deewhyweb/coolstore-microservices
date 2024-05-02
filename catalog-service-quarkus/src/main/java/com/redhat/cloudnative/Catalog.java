package com.redhat.cloudnative;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Cacheable
public class Catalog extends PanacheEntity {

    public String itemId;
    public String name;
    public String description;
    public Long price;

    public Catalog() {

    }

}