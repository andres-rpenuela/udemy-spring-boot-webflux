package com.codearp.springboot.reactor.springbootsebfluxapirest.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "categories")
public class Category {

    @Id
    private String id;

    private String name;

    public Category() {
    }

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isEmpty() {
        return this.id == null && this.name == null;
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
        private String name;

        public CategoryBuilder() {
        }

        public CategoryBuilder id(String id) {
            this.id = id;
            return this;
        }

        public CategoryBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Category build() {
            return new Category(id, name);
        }
    }
}
