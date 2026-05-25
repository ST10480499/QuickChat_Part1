/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quickchat;


/**
 * Login class for the QuickChat registration and authentication system.
 *
 * Cell phone regex reference:


/**
 *
 * @author khoza
 */
class Login {
      private String username;
    private String password;
    private String cellPhoneNumber;
    private String firstName;
    private String lastName;

    public Login(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Login() {
        this.firstName = "";
        this.lastName = "";
    }

    public void setUsername(String username)             { this.username = username; }
    public void setPassword(String password)             { this.password = password; }
    public void setCellPhoneNumber(String cellPhoneNumber) { this.cellPhoneNumber = cellPhoneNumber; }
    public String getUsername()                          { return username; }
    public String getFirstName()                         { return firstName; }
    public String getLastName()                          { return lastName; }

    public boolean checkUserName(String username) {
        if (username == null) return false;
        return username.contains("_") && username.length() <= 5;
    }

    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))           hasUpper = true;
            else if (Character.isDigit(c))          hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        return hasUpper && hasDigit && hasSpecial;
    }

    public boolean checkCellPhoneNumber(String cellPhone) {
        if (cellPhone == null) return false;
        return cellPhone.matches("^\\+[0-9]{10,11}$");
    }

    public String registerUser(String username, String password, String cellPhone) {
        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your username "
                 + "contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password "
                 + "contains at least eight characters, a capital letter, a number, and a special character.";
        }
        if (!checkCellPhoneNumber(cellPhone)) {
            return "Cell number is incorrectly formatted or does not contain an international code; "
                 + "please correct the number and try again.";
        }
        this.username = username;
        this.password = password;
        this.cellPhoneNumber = cellPhone;
        return "Username successfully captured.\nPassword successfully captured.\nCell number successfully captured.\nUser registered successfully.";
    }

    public boolean loginUser(String username, String password) {
        if (this.username == null || this.password == null) return false;
        return this.username.equals(username) && this.password.equals(password);
    }

    public String returnLoginStatus(String username, String password) {
        if (loginUser(username, password)) {
            return "Welcome " + firstName + ", " + lastName + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    public String validateUsername(String username) {
        return checkUserName(username)
            ? "Username successfully captured."
            : "Username is not correctly formatted; please ensure that your username "
            + "contains an underscore and is no more than five characters in length.";
    }

    public String validatePassword(String password) {
        return checkPasswordComplexity(password)
            ? "Password successfully captured."
            : "Password is not correctly formatted; please ensure that the password "
            + "contains at least eight characters, a capital letter, a number, and a special character.";
    }

    public String validateCellPhone(String cellPhone) {
        return checkCellPhoneNumber(cellPhone)
            ? "Cell number successfully captured."
            : "Cell number is incorrectly formatted or does not contain an international code; "
            + "please correct the number and try again.";
    }
}

