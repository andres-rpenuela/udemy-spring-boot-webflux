package com.codearp.springboot.reactor.models.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Objects;

@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;

    private Double price;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;

    public Product() {
    }

    public Product(String id, String name, Double price, Date createAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.createAt = Objects.requireNonNullElse( createAt, new Date());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder{
        private String id;
        private String name;
        private Double price;
        private Date createAt;

        public ProductBuilder() {

        }

        public ProductBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public ProductBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ProductBuilder withPrice(Double price) {
            this.price = price;
            return this;
        }

        public ProductBuilder withCreateAt(Date createAt) {
            this.createAt = createAt;
            return this;
        }

        public Product build() {
            return new Product(id, name, price, createAt);

        }
    }
}
