package financeapp;

import financeapp.data.DataManager;
import financeapp.model.Command;
import financeapp.model.User;
import financeapp.service.FinanceManager;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static FinanceManager financeManager;

    public static void main(String[] args) {
        // Перед началом работы читаем список пользователей (если он уже есть)
        DataManager.loadUsers();

        System.out.println("Добро пожаловать в систему управления личными финансами!");

        // Авторизуем (или регистрируем) пользователя
        User currentUser = authenticateUser();
        if (currentUser == null) {
            // Если не авторизовались — завершаем
            System.out.println("Авторизация не удалась. Завершение работы приложения.");
            return;
        }

        // Создаём менеджер для управлением кошельком
        financeManager = new FinanceManager(currentUser);

        // Основной цикл — пока running = true, будем показывать меню и обрабатывать команды
        boolean running = true;
        while (running) {
            printMenu();

            String userInput = scanner.nextLine().trim();
            Command command = Command.fromCode(userInput);

            switch (command) {
                case ADD_INCOME:
                    addIncome();
                    break;
                case ADD_EXPENSE:
                    addExpense();
                    break;
                case SET_CATEGORY_BUDGET:
                    setCategoryBudget();
                    break;
                case LIST_CATEGORIES:
                    listCategories();
                    break;
                case SHOW_OVERALL_STATS:
                    showOverallStats();
                    break;
                case SHOW_CATEGORY_STATS:
                    showCategoryStats();
                    break;
                case TRANSFER_FUNDS:
                    transferFunds();
                    break;
                case EXIT:
                    exitAndSaveData();
                    running = false;
                    break;
                default:
                    System.out.println("Неизвестная команда. Попробуйте ещё раз.");
            }
        }

    }

    /**
     * Метод для авторизации или регистрации пользователя.
     * @return Объект User или null (если не удалось залогиниться/зарегистрироваться)
     */
    private static User authenticateUser() {
        System.out.print("Введите логин: ");
        String login = scanner.nextLine().trim();
        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        // Проверяем, есть ли уже такой логин в системе
        User user = DataManager.getUserByLogin(login);
        if (user == null) {
            // Если пользователя с таким логином нет, можем предложить зарегистрироваться
            System.out.println("Пользователь не найден. Желаете зарегистрироваться (y/n)?");
            String answer = scanner.nextLine().trim();

            if (answer.equalsIgnoreCase("y")) {
                // Регистрируем нового пользователя
                user = DataManager.registerNewUser(login, password);
                if (user != null) {
                    System.out.println("Поздравляем! Регистрация прошла успешно.");
                } else {
                    // Если вернулся null, значит логин уже был занят и т.д.
                    return null;
                }
            } else {
                // Отказались регистрироваться — возвращаем null
                return null;
            }
        } else {
            // Если логин есть, проверяем пароль
            boolean isValid = DataManager.validateUserPassword(login, password);
            if (!isValid) {
                System.out.println("Неверный пароль!");
                return null;
            }
        }

        // Если мы дошли сюда — пользователь либо успешно зарегистрирован, либо авторизовался
        return user;
    }

    /**
     * Выводит в консоль список доступных команд (меню).
     */
    private static void printMenu() {
        System.out.println("\nМеню команд:");
        System.out.println("1. Добавить доход");
        System.out.println("2. Добавить расход");
        System.out.println("3. Установить (или изменить) лимит по категории");
        System.out.println("4. Показать все категории");
        System.out.println("5. Показать общую статистику (доход/расход/баланс)");
        System.out.println("6. Показать подробную статистику по категориям");
        System.out.println("7. Перевести средства другому пользователю (доп.)");
        System.out.println("8. Выйти (с сохранением данных)");
        System.out.print("Введите номер команды и нажмите Enter: ");
    }

    /**
     * Обёртка вокруг метода добавления дохода, с безопасным считыванием числового значения.
     */
    private static void addIncome() {
        System.out.print("Введите описание дохода (например, Зарплата): ");
        String description = scanner.nextLine().trim();

        // Просим ввести сумму дохода и безопасно пытаемся считать double
        double amount = readDoubleFromConsole("Введите сумму дохода: ");

        try {
            // Вызываем метод менеджера для добавления дохода
            financeManager.addIncome(description, amount);
            System.out.println("Доход успешно добавлен!");
        } catch (IllegalArgumentException e) {
            // Если в методе addIncome была брошена ошибка, выводим сообщение
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Аналогично, добавляем расход. Запрашиваем описание, сумму и категорию.
     */
    private static void addExpense() {
        System.out.print("Введите описание расхода (например, Покупка продуктов): ");
        String description = scanner.nextLine().trim();

        double amount = readDoubleFromConsole("Введите сумму расхода: ");

        System.out.print("Введите категорию расхода (например, Еда, Такси): ");
        String categoryName = scanner.nextLine().trim();

        try {
            financeManager.addExpense(description, amount, categoryName);
            System.out.println("Расход успешно добавлен!");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Запрашиваем у пользователя название категории и новый лимит.
     */
    private static void setCategoryBudget() {
        System.out.print("Введите название категории: ");
        String categoryName = scanner.nextLine().trim();

        double limit = readDoubleFromConsole("Введите новый лимит (неотрицательное число): ");

        try {
            financeManager.setBudgetForCategory(categoryName, limit);
            System.out.println("Бюджет для категории '" + categoryName + "' установлен/обновлён.");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Вывести список категорий.
     */
    private static void listCategories() {
        financeManager.listCategories();
    }

    /**
     * Отобразить общую сводную статистику: суммарный доход, расход и текущий баланс.
     */
    private static void showOverallStats() {
        double totalIncome = financeManager.calculateTotalIncome();
        double totalExpense = financeManager.calculateTotalExpense();
        double currentBalance = financeManager.getWallet().getCurrentBalance();

        System.out.println("Общий доход: " + totalIncome);
        System.out.println("Общие расходы: " + totalExpense);
        System.out.println("Текущий баланс: " + currentBalance);
    }

    /**
     * Отобразить подробную статистику по категориям (лимит, потрачено, остаток).
     */
    private static void showCategoryStats() {
        financeManager.showCategoryStats();
    }

    /**
     * Доп. функция: перевод средств другому пользователю.
     * Запрашиваем логин получателя, описание и сумму.
     */
    private static void transferFunds() {
        System.out.print("Введите логин получателя: ");
        String recipientLogin = scanner.nextLine().trim();

        System.out.print("Введите описание перевода (например, 'Подарок'): ");
        String description = scanner.nextLine().trim();

        double amount = readDoubleFromConsole("Введите сумму перевода: ");

        try {
            financeManager.transferFunds(recipientLogin, description, amount);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Завершение работы: сохраняем кошелёк и общий список пользователей.
     * После этого выводим сообщение и выходим из приложения.
     */
    private static void exitAndSaveData() {
        financeManager.saveWalletData();
        DataManager.saveUsers();
        System.out.println("Данные успешно сохранены. Выходим из программы...");
    }

    /**
     * Вспомогательный метод для безопасного считывания double.
     * Если пользователь вводит некорректные данные, просим повторить ввод.
     * @param prompt приглашение в консоли
     * @return число типа double, введённое пользователем
     */
    private static double readDoubleFromConsole(String prompt) {
        while (true) {
            System.out.print(prompt);

            // Переводим строку в double, если ошибка — ловим исключение
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException | InputMismatchException e) {
                System.out.println("Некорректный ввод! Пожалуйста, введите число (формат: 123.45).");
            }
        }
    }
}
