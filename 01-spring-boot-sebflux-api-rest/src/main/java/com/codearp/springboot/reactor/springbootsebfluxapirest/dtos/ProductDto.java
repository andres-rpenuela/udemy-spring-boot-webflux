package com.codearp.springboot.reactor.springbootsebfluxapirest.dtos;

import com.codearp.springboot.reactor.springbootsebfluxapirest.shared.CATEGORY;
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

    private String name;
    private Double price;
    private Date createAt;
    private CATEGORY category;
    private String picture;
}
