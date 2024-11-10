package user;

public class User {
    public int id;
    public String login;
    public String password;
    public String role;

    public User (String login, String password, String role)
    {
        this.id = -1;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public User (int id, String login, String password, String role)
    {
        this.id = id;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public User() {
        this.id = -1;
        this.login = null;
        this.password = null;
        this.role = null;
    }

    public void changeRole(String role)
    {
        this.role = role;
    }

    public int getId() {
        return id;
    }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void print() {        System.out.printf("ID: %-5s | Логин: %-10s | Пароль: %-10s | Роль: %10s%n",
            id, login, password, role);
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setLogin(String login) { this.login = login; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}
