package com.codearp.springboot.reactor.controllers;

import com.codearp.springboot.reactor.dao.ProductDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductDao productDao;


    @GetMapping("/products")
    public String listProducts(Model model) {

        model.addAttribute("title", "Product List");
        model.addAttribute("products", productDao.findAll());
        return "products/list";
    }
}
