package com.karthi.expense_tracker.repository;

import com.karthi.expense_tracker.dto.CategoryTotal;
import com.karthi.expense_tracker.dto.MonthTypeTotal;
import com.karthi.expense_tracker.entity.Transaction;
import com.karthi.expense_tracker.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.karthi.expense_tracker.dto.MonthTypeTotal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByTransactionDateDescIdDesc(Long userId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByCategoryId(Long categoryId);

    // SELECT SUM(amount) ... returns null when there are no rows
    @Query("select sum(t.amount) from Transaction t "
            + "where t.user.id = :userId and t.type = :type "
            + "and t.transactionDate between :from and :to")
    BigDecimal sumAmount(@Param("userId") Long userId,
                         @Param("type") TransactionType type,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to);

    // SELECT category, SUM(amount) ... GROUP BY category, biggest first
    @Query("select new com.karthi.expense_tracker.dto.CategoryTotal(c.name, sum(t.amount)) "
            + "from Transaction t join t.category c "
            + "where t.user.id = :userId and t.type = :type "
            + "and t.transactionDate between :from and :to "
            + "group by c.name order by sum(t.amount) desc")
    List<CategoryTotal> sumByCategory(@Param("userId") Long userId,
                                      @Param("type") TransactionType type,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);
    // one row per (year, month, type): SUM(amount) ... GROUP BY year, month, type
    @Query("select new com.karthi.expense_tracker.dto.MonthTypeTotal("
            + "year(t.transactionDate), month(t.transactionDate), t.type, sum(t.amount)) "
            + "from Transaction t "
            + "where t.user.id = :userId and t.transactionDate between :from and :to "
            + "group by year(t.transactionDate), month(t.transactionDate), t.type")
    List<MonthTypeTotal> monthlyTotals(@Param("userId") Long userId,
                                       @Param("from") LocalDate from,
                                       @Param("to") LocalDate to);
}