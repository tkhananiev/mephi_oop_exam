package financeapp.model;

import java.io.Serializable;

/**
 * Класс пользователя:
 * - хранит логин;
 * - хранит хэш пароля (passwordHash);
 * - имеет ссылку на Wallet (кошелёк), чтобы работать с ним напрямую.
 *   Кошелёк сериализуется отдельно, поэтому здесь помечен как transient.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String login;
    private final String passwordHash;
    private transient Wallet wallet; // помечено transient, так как хранится в другом файле

    public User(String login, String passwordHash) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.wallet = null; // по умолчанию кошелёк не загружен
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
