package financeapp.data;

import financeapp.model.User;
import financeapp.model.Wallet;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Этот класс отвечает за работу с данными пользователей:
 * - хранит всех зарегистрированных пользователей в памяти (HashMap);
 * - при необходимости загружает пользователей из файла и сохраняет в файл;
 * - также умеет загружать/сохранять кошелёк для каждого пользователя по отдельному файлу.
 *
 * Смысл этого разделения такой: общее хранилище (usersMap) знает, какие пользователи есть,
 * а каждый пользователь имеет свой файл "wallet_<login>.dat", где находится его кошелёк.
 */
public class DataManager {
    // Здесь храним путь к файлу со списком всех пользователей
    private static final String USERS_FILE = "users.dat";

    // Мапа "логин -> пользователь". В памяти на время работы программы.
    private static HashMap<String, User> usersMap = new HashMap<>();

    /**
     * Загрузить всех пользователей из файла (users.dat), если он существует.
     */
    @SuppressWarnings("unchecked")
    public static void loadUsers() {
        File f = new File(USERS_FILE);

        // Если файла нет, значит пока что ни одного пользователя не зарегистрировано.
        if (!f.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            usersMap = (HashMap<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Если файл вдруг испорчен или возникли проблемы чтения, мы оповестим, но не упадём.
            System.out.println("Не удалось загрузить пользователей: " + e.getMessage());
            usersMap = new HashMap<>();
        }
    }

    /**
     * Сохранить всех пользователей в файл (users.dat), чтобы при следующем запуске
     * они были под рукой.
     */
    public static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(usersMap);
        } catch (IOException e) {
            // Здесь также не вылетаем, а просто предупредим
            System.out.println("Ошибка при сохранении пользователей: " + e.getMessage());
        }
    }

    /**
     * Поиск пользователя по логину.
     * @param login логин
     * @return объект User или null, если такой логин не найден
     */
    public static User getUserByLogin(String login) {
        return usersMap.get(login);
    }

    /**
     * Регистрация нового пользователя.
     * @param login логин
     * @param password пароль (в обычном виде, не хэш!)
     * @return новый объект User или null, если пользователь с таким логином уже существует
     */
    public static User registerNewUser(String login, String password) {
        // Сначала проверяем, нет ли уже такого логина
        if (usersMap.containsKey(login)) {
            System.out.println("Пользователь с логином '" + login + "' уже существует!");
            return null;
        }

        // Хэшируем пароль, чтобы не хранить его в открытом виде
        String hashed = hashPassword(password);

        // Создаём объект пользователя и кладём в нашу мапу
        User newUser = new User(login, hashed);
        usersMap.put(login, newUser);

        saveUsers();
        return newUser;
    }

    /**
     * Проверка корректности пароля:
     * Мы хэшируем введённый пароль и сверяем с хранимым хэшем.
     */
    public static boolean validateUserPassword(String login, String password) {
        User user = usersMap.get(login);
        if (user == null) {
            return false;
        }
        String hashed = hashPassword(password);
        return user.getPasswordHash().equals(hashed);
    }

    /**
     * Загрузка кошелька конкретного пользователя из файла "wallet_<login>.dat".
     */
    public static Wallet loadWalletForUser(String login) {
        String walletFileName = "wallet_" + login + ".dat";
        File f = new File(walletFileName);

        // Если файла нет, это значит, что у пользователя ещё не создавался кошелёк
        if (!f.exists()) {
            return new Wallet(); // возвращаем новый "пустой" кошелёк
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            return (Wallet) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Если файл испорчен или не удалось прочитать — возвращаем новый кошелёк
            System.out.println("Не удалось загрузить кошелёк пользователя '" + login + "': " + e.getMessage());
            return new Wallet();
        }
    }

    /**
     * Сохранить кошелёк конкретного пользователя в файл "wallet_<login>.dat",
     * чтобы его данные были доступны при следующем входе.
     */
    public static void saveWalletForUser(String login, Wallet wallet) {
        String walletFileName = "wallet_" + login + ".dat";

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(walletFileName))) {
            oos.writeObject(wallet);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении кошелька пользователя '" + login + "': " + e.getMessage());
        }
    }

    /**
     * Вспомогательный метод для хэширования пароля алгоритмом SHA-256.
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());

            // Преобразуем байты в hex-строку
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            // Маловероятно, что это случится, но перехватим и выбросим, чтобы не падать
            throw new RuntimeException("Алгоритм SHA-256 не поддерживается!", e);
        }
    }
}
