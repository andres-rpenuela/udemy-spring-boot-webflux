package com.codearp.springboot.reactor.models.documents;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Objects;

@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @NotNull
    @PositiveOrZero
    @DecimalMin("0.01")
    private Double price;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createAt;

    //@Valid
    private Category category;

    private String picture;

    public Product() {
    }

    public Product(String id, String name, Double price, Date createAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.createAt = Objects.requireNonNullElse( createAt, new Date());
    }

    public Product(String id, String name, Double price, Date createAt,Category category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.createAt = Objects.requireNonNullElse( createAt, new Date());
        this.category = category;
    }

    public Product(String id, String name, Double price, Date createAt,Category category,String picture) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.createAt = Objects.requireNonNullElse( createAt, new Date());
        this.category = category;
        this.picture = picture;
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

    public void setPicture(String picture){ this.picture = picture;}

    public String getPicture(){ return picture; }

    public Category getCategory() {        return category;    }

    public void setCategory(Category category) {        this.category = category;    }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder{
        private String id;
        private String name;
        private Double price;
        private Date createAt;
        private Category category;
        private String picture;

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

        public ProductBuilder withCategory(Category category) {
            this.category = category;
            return this;
        }

        public ProductBuilder withPicture(String picture) {
            this.picture = picture;
            return this;
        }

        public Product build() {
            return new Product(id, name, price, createAt, category, picture);
        }
    }

}
