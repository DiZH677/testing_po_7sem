package app.backend;

public class AuthorizationResult {
    private boolean isValid;
    private UserUI userUI;

    public AuthorizationResult(boolean isValid, UserUI userUI) {
        this.isValid = isValid;
        this.userUI = userUI;
    }

    public AuthorizationResult() {
        this.isValid = false;
        this.userUI = null;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public void setUserUI(UserUI userUI) {
        this.userUI = userUI;
    }

    public boolean isValid() {
        return isValid;
    }

    public UserUI getUserUI() {
        return userUI;
    }
}

