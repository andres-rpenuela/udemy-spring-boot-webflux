package com.codearp.springboot.reactor.springbootsebfluxapirest.dtos;

import com.codearp.springboot.reactor.springbootsebfluxapirest.shared.CATEGORY;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDto {

    private String id;

    @NotNull
    private String name;
    @Positive
    private Double price;
    private Date createAt;
    @NotNull
    private CATEGORY category;

    private String picture;
}
