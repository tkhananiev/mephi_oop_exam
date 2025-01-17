package financeapp.model;

/**
 * Перечисление всех доступных команд.
 * Здесь каждая команда привязана к своей "цифре-строке" в меню.
 */
public enum Command {
    ADD_INCOME("1"),
    ADD_EXPENSE("2"),
    SET_CATEGORY_BUDGET("3"),
    LIST_CATEGORIES("4"),
    SHOW_OVERALL_STATS("5"),
    SHOW_CATEGORY_STATS("6"),
    TRANSFER_FUNDS("7"),
    EXIT("8"),
    UNKNOWN("");

    private final String code;

    Command(String code) {
        this.code = code;
    }

    /**
     * Получаем Command по введённой строке.
     * Если введённое значение не совпадает ни с одной "цифрой", вернём UNKNOWN.
     */
    public static Command fromCode(String code) {
        for (Command cmd : Command.values()) {
            if (cmd.code.equals(code)) {
                return cmd;
            }
        }
        return UNKNOWN;
    }
}