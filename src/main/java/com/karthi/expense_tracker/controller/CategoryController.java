package com.karthi.expense_tracker.controller;

import com.karthi.expense_tracker.dto.CategoryRequest;
import com.karthi.expense_tracker.dto.CategoryResponse;
import com.karthi.expense_tracker.entity.Category;
import com.karthi.expense_tracker.entity.User;
import com.karthi.expense_tracker.exception.ResourceNotFoundException;
import com.karthi.expense_tracker.repository.CategoryRepository;
import com.karthi.expense_tracker.repository.TransactionRepository;
import com.karthi.expense_tracker.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUser currentUser;

    public CategoryController(CategoryRepository categoryRepository,
                              TransactionRepository transactionRepository,
                              CurrentUser currentUser) {
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryRepository.findByUserIdOrderByNameAsc(currentUser.get().getId()).stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        User user = currentUser.get();
        Category category = new Category();
        category.setName(request.name().trim());
        category.setUser(user);
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category category = findOwned(id);
        category.setName(request.name().trim());
        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Category category = findOwned(id);
        if (transactionRepository.existsByCategoryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Category is used by transactions; delete or move them first");
        }
        categoryRepository.delete(category);
    }

    private Category findOwned(Long id) {
        return categoryRepository.findByIdAndUserId(id, currentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}