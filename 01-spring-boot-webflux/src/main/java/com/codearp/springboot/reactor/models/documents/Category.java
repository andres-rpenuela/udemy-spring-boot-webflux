package com.codearp.springboot.reactor.models.documents;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "categories")
public class Category {

    // Enum definido dentro de la clase (puede extraerse si se prefiere)
    public enum CategoryName {
        ELECTRONICS,
        FASHION,
        HOME,
        BEAUTY,
        SPORTS,
        TOYS,
        AUTOMOTIVE,
        BOOKS,
        MUSIC,
        GROCERY;
    }

    @Id
    private String id;

    @NotNull
    private CategoryName name;

    public Category() {
    }

    public Category(String id, CategoryName name) {
        this.id = id;
        this.name = name;
    }

    public boolean isEmpty() {
        return this.id == null || ( this.name == null || this.name.name().isEmpty() ) ;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public CategoryName getName() {
        return name;
    }

    public void setName(CategoryName name) {
        this.name = name;
    }
    public String toString() {
        return "Category{id='" + id + "', name=" + name + "}";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id) && name == category.name;
    }

    public int hashCode() {
        return Objects.hash(id, name);
    }

    public static CategoryBuilder builder() {
        return new CategoryBuilder();
    }

    public static class CategoryBuilder {
        private String id;
        private CategoryName name;

        public CategoryBuilder() {
        }

        public CategoryBuilder id(String id) {
            this.id = id;
            return this;
        }

        public CategoryBuilder name(CategoryName name) {
            this.name = name;
            return this;
        }

        public Category build() {
            return new Category(id, name);
        }
    }
}
