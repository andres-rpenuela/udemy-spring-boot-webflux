package com.codearp.springboot.reactor.springbootsebfluxapirest.shared;

public enum CATEGORY {
    ELECTRONICS,
    FASHION,
    HOME,
    BEAUTY,
    SPORTS,
    TOYS,
    AUTOMOTIVE,
    BOOKS,
    MUSIC,
    GROCERY,
    OTHER;

    public static CATEGORY fromString(String category) throws IllegalArgumentException {
        for (CATEGORY cat : CATEGORY.values()) {
            if (cat.name().equalsIgnoreCase(category)) {
                return cat;
            }
        }
        throw new IllegalArgumentException("No enum constant for category: " + category);
    }

    public static CATEGORY fromCategory(com.codearp.springboot.reactor.springbootsebfluxapirest.documents.Category category) throws IllegalArgumentException {
        return fromString( category.getName().toString() );
    }

}
