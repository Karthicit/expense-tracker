package com.karthi.expense_tracker.controller;

import com.karthi.expense_tracker.dto.TransactionRequest;
import com.karthi.expense_tracker.dto.TransactionResponse;
import com.karthi.expense_tracker.entity.Category;
import com.karthi.expense_tracker.entity.Transaction;
import com.karthi.expense_tracker.entity.User;
import com.karthi.expense_tracker.exception.ResourceNotFoundException;
import com.karthi.expense_tracker.repository.CategoryRepository;
import com.karthi.expense_tracker.repository.TransactionRepository;
import com.karthi.expense_tracker.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUser currentUser;

    public TransactionController(TransactionRepository transactionRepository,
                                 CategoryRepository categoryRepository,
                                 CurrentUser currentUser) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<TransactionResponse> list() {
        return transactionRepository
                .findByUserIdOrderByTransactionDateDescIdDesc(currentUser.get().getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@PathVariable Long id) {
        return toResponse(findOwned(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request) {
        User user = currentUser.get();
        Transaction t = new Transaction();
        t.setUser(user);
        apply(t, request, user);
        return toResponse(transactionRepository.save(t));
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        User user = currentUser.get();
        Transaction t = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        apply(t, request, user);
        return toResponse(transactionRepository.save(t));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        transactionRepository.delete(findOwned(id));
    }

    private Transaction findOwned(Long id) {
        return transactionRepository.findByIdAndUserId(id, currentUser.get().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private void apply(Transaction t, TransactionRequest r, User user) {
        Category category = categoryRepository.findByIdAndUserId(r.categoryId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        t.setCategory(category);
        t.setAmount(r.amount());
        t.setType(r.type());
        t.setDescription(r.description());
        t.setTransactionDate(r.date());
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAmount(), t.getType(), t.getDescription(),
                t.getTransactionDate(), t.getCategory().getId(), t.getCategory().getName());
    }
}