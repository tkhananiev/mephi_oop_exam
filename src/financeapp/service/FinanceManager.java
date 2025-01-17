package financeapp.service;

import financeapp.data.DataManager;
import financeapp.model.Category;
import financeapp.model.Operation;
import financeapp.model.User;
import financeapp.model.Wallet;

/**
 * В этом классе реализуется "бизнес-логика":
 * - добавление доходов/расходов,
 * - проверка лимитов,
 * - переводы между пользователями,
 * - сохранение данных.
 */
public class FinanceManager {
    private final User currentUser;
    private final Wallet wallet;

    public FinanceManager(User currentUser) {
        this.currentUser = currentUser;
        // Подгружаем кошелёк пользователя из файла (или создаём новый, если файла нет)
        this.wallet = DataManager.loadWalletForUser(currentUser.getLogin());
        // Привязываем кошелёк к пользователю, чтобы он "знал" о нём
        this.currentUser.setWallet(this.wallet);
    }

    /**
     * Добавить доход (income) в кошелёк.
     * @param description описание дохода (например, "Зарплата")
     * @param amount сумма
     */
    public void addIncome(String description, double amount) {
        if (amount <= 0) {
            System.out.println("Ошибка: Сумма дохода должна быть положительной!");
            return;
        }

        wallet.addToBalance(amount);
        Operation op = new Operation(true, description, amount, null);
        wallet.addOperation(op);

        // Проверим общий баланс
        checkOverallBalance();
    }

    /**
     * Добавить расход (expense) из кошелька.
     * @param description описание (например, "Продукты в супермаркете")
     * @param amount сумма расходов
     * @param categoryName название категории (например, "Еда")
     */
    public void addExpense(String description, double amount, String categoryName) {
        if (amount <= 0) {
            System.out.println("Ошибка: Сумма расхода должна быть положительной!");
            return;
        }
        if (categoryName == null || categoryName.isEmpty()) {
            System.out.println("Ошибка: Название категории не может быть пустым!");
            return;
        }

        wallet.subtractFromBalance(amount);

        // Создаём операцию
        Operation op = new Operation(false, description, amount, categoryName);
        wallet.addOperation(op);

        // Обновляем статистику по категории
        Category cat = wallet.getOrCreateCategory(categoryName);
        cat.addSpent(amount);

        // Проверяем, не превышен ли лимит для этой категории
        if (cat.getBudgetLimit() > 0 && cat.getTotalSpent() > cat.getBudgetLimit()) {
            System.out.println("Внимание! Превышен лимит по категории: " + categoryName);
        }

        // Проверяем общий баланс
        checkOverallBalance();
    }

    /**
     * Установить или изменить бюджетный лимит для категории.
     * @param categoryName название категории
     * @param limit лимит
     */
    public void setBudgetForCategory(String categoryName, double limit) {
        if (limit < 0) {
            System.out.println("Ошибка: Лимит не может быть отрицательным!");
            return;
        }
        if (categoryName == null || categoryName.isEmpty()) {
            System.out.println("Ошибка: Название категории не может быть пустым!");
            return;
        }

        Category cat = wallet.getOrCreateCategory(categoryName);
        cat.setBudgetLimit(limit);
        System.out.println("Бюджет для категории '" + categoryName + "' установлен: " + limit);
    }

    /**
     * Вывести список всех категорий, их лимиты, потраченные суммы и сколько ещё осталось.
     */
    public void listCategories() {
        if (wallet.getCategories().isEmpty()) {
            System.out.println("Категорий ещё нет.");
            return;
        }

        System.out.println("Список категорий:");
        for (Category cat : wallet.getCategories().values()) {
            double left = cat.getBudgetLimit() - cat.getTotalSpent();
            System.out.printf("- %s | Лимит: %.2f | Потрачено: %.2f | Осталось: %.2f%n",
                    cat.getName(), cat.getBudgetLimit(), cat.getTotalSpent(), left);
        }
    }

    /**
     * Подсчитать общий доход (сумма всех операций, где isIncome = true).
     */
    public double calculateTotalIncome() {
        double total = 0.0;
        for (Operation op : wallet.getOperations()) {
            if (op.isIncome()) {
                total += op.getAmount();
            }
        }
        return total;
    }

    /**
     * Подсчитать общий расход (сумма всех операций, где isIncome = false).
     */
    public double calculateTotalExpense() {
        double total = 0.0;
        for (Operation op : wallet.getOperations()) {
            if (!op.isIncome()) {
                total += op.getAmount();
            }
        }
        return total;
    }

    /**
     * Показать статистику по всем категориям: лимиты, потрачено, остаток.
     */
    public void showCategoryStats() {
        if (wallet.getCategories().isEmpty()) {
            System.out.println("Категорий нет.");
            return;
        }

        System.out.println("Статистика по категориям:");
        for (Category cat : wallet.getCategories().values()) {
            double limit = cat.getBudgetLimit();
            double spent = cat.getTotalSpent();
            double remaining = limit - spent;
            System.out.printf("Категория '%s': Лимит=%.2f, Потрачено=%.2f, Остаток=%.2f%n",
                    cat.getName(), limit, spent, remaining);
        }
    }

    /**
     * Сохранить кошелёк текущего пользователя в файл, чтобы при следующем запуске
     * все данные были на месте.
     */
    public void saveWalletData() {
        DataManager.saveWalletForUser(currentUser.getLogin(), wallet);
    }

    /**
     * Проверяем, не ушли ли мы в минус по балансу. Если да, то выводим предупреждение.
     */
    private void checkOverallBalance() {
        if (wallet.getCurrentBalance() < 0) {
            System.out.println("Внимание! Ваш баланс стал отрицательным. Расходы превысили доходы.");
        }
    }

    /**
     * Для получения объекта кошелька (если где-то нужно использовать в других методах).
     */
    public Wallet getWallet() {
        return wallet;
    }

    /**
     * Дополнительная функция: перевод между кошельками пользователей.
     * При этом:
     * - у отправителя создаём операцию расхода;
     * - у получателя создаём операцию дохода.
     *
     * @param recipientLogin логин получателя
     * @param description описание перевода
     * @param amount сумма
     */
    public void transferFunds(String recipientLogin, String description, double amount) {
        if (amount <= 0) {
            System.out.println("Ошибка: Сумма перевода должна быть положительной!");
            return;
        }

        // Ищем пользователя-получателя
        User recipient = DataManager.getUserByLogin(recipientLogin);
        if (recipient == null) {
            System.out.println("Ошибка: пользователь с логином '" + recipientLogin + "' не найден!");
            return;
        }

        // Списываем средства у текущего пользователя (это будет расход)
        addExpense(description, amount, "Перевод пользователю " + recipientLogin);

        // Добавляем доход получателю
        // Для этого загружаем кошелёк получателя
        Wallet recipientWallet = DataManager.loadWalletForUser(recipientLogin);
        recipientWallet.addToBalance(amount);
        Operation op = new Operation(true, "Перевод от " + currentUser.getLogin(), amount, null);
        recipientWallet.addOperation(op);

        // Сохраняем кошелёк получателя
        DataManager.saveWalletForUser(recipientLogin, recipientWallet);

        System.out.println("Перевод " + amount + " пользователю '" + recipientLogin + "' успешно выполнен!");
    }
}
