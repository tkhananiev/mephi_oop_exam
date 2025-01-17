package financeapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Кошелёк пользователя:
 * - currentBalance: текущий баланс (с учётом доходов и расходов),
 * - operations: список всех операций (доход и расход),
 * - categories: набор (HashMap) категорий (например, "Еда", "Коммуналка" и т.д.).
 */
public class Wallet implements Serializable {
    private static final long serialVersionUID = 1L;

    private double currentBalance;
    private List<Operation> operations;
    private HashMap<String, Category> categories;

    public Wallet() {
        this.currentBalance = 0.0;
        this.operations = new ArrayList<>();
        this.categories = new HashMap<>();
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    /**
     * Увеличить баланс
     */
    public void addToBalance(double amount) {
        currentBalance += amount;
    }

    /**
     * Уменьшить баланс
     */
    public void subtractFromBalance(double amount) {
        currentBalance -= amount;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    /**
     * Сохранить операцию в общем списке, чтобы можно было посмотреть историю.
     */
    public void addOperation(Operation op) {
        operations.add(op);
    }

    public HashMap<String, Category> getCategories() {
        return categories;
    }

    /**
     * Если нужной категории нет, создадим её с нулевым лимитом.
     */
    public Category getOrCreateCategory(String categoryName) {
        if (!categories.containsKey(categoryName)) {
            categories.put(categoryName, new Category(categoryName, 0.0));
        }
        return categories.get(categoryName);
    }

    /**
     * Если категория не существует, вернём null.
     */
    public Category getCategory(String categoryName) {
        return categories.get(categoryName);
    }
}
