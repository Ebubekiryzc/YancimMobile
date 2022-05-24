package tr.edu.duzce.mf.bm.yancimmobile.model;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String email;
    private String role;

    public User() {
    }

    public User(String email, String username, String role) {
        this.email = email;
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
