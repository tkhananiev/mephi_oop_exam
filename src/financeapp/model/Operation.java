package financeapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Операция (транзакция) внутри кошелька.
 * Может быть доходом или расходом (isIncome).
 * Храним:
 * - описание (description),
 * - сумму (amount),
 * - дату/время (dateTime),
 * - необязательное поле категории (categoryName), если это расход.
 */
public class Operation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean isIncome;
    private final String description;
    private final double amount;
    private final LocalDateTime dateTime;
    private final String categoryName; // null или пустая строка, если это доход

    public Operation(boolean isIncome, String description, double amount, String categoryName) {
        this.isIncome = isIncome;
        this.description = description;
        this.amount = amount;
        this.dateTime = LocalDateTime.now();
        this.categoryName = categoryName;
    }

    public boolean isIncome() {
        return isIncome;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateTime.format(formatter);

        if (isIncome) {
            return String.format("[Доход] %s: +%.2f (дата: %s)", description, amount, dateStr);
        } else {
            return String.format("[Расход] %s (категория: %s): -%.2f (дата: %s)",
                    description, categoryName, amount, dateStr);
        }
    }
}
