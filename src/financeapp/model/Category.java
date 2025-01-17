package financeapp.model;

import java.io.Serializable;

/**
 * Категория расходов.
 * Храним:
 * - название (name),
 * - бюджетный лимит (budgetLimit),
 * - общую потраченную сумму (totalSpent).
 *
 */
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double budgetLimit;
    private double totalSpent;

    public Category(String name, double budgetLimit) {
        this.name = name;
        this.budgetLimit = budgetLimit;
        this.totalSpent = 0.0;
    }

    public String getName() {
        return name;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(double newLimit) {
        this.budgetLimit = newLimit;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    /**
     * При добавлении расходов мы увеличиваем totalSpent на заданную сумму.
     */
    public void addSpent(double amount) {
        this.totalSpent += amount;
    }

}
