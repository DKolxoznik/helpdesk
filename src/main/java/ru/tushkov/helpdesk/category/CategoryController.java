package ru.tushkov.helpdesk.category;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categories;

    public CategoryController(CategoryRepository categories) {
        this.categories = categories;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return categories.findAllByOrderByNameAsc().stream()
                .map(category -> new CategoryResponse(category.getId(), category.getCode(), category.getName()))
                .toList();
    }
}
