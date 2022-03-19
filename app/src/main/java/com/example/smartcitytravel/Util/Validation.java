package com.example.smartcitytravel.Util;

public class Validation {

    //check full name contain valid and allowed characters
    public String validateFullName(String fullName) {
        String nameRegex = "[a-zA-Z\\s]+";
        if (fullName.isEmpty()) {
            return "Error! Enter your name";
        } else if (!fullName.matches(nameRegex)) {
            return "Error! Only alphabets and spaces are acceptable";
        } else {
            return "";
        }
    }

    //check email contain valid and allowed characters
    public String validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9.]+@[A-Za-z.]+$";
        if (email.isEmpty()) {
            return "Error! Empty Email";
        } else if (!email.matches(emailRegex)) {
            return "Error! Invalid Email";
        } else {
            return "";
        }
    }

    //check password contain valid and allowed characters
    public String validatePassword(String password) {
        boolean containOneDigit = false;
        boolean containOneUpperCaseLetter = false;
        boolean containOneLowerCaseLetter = false;

        for (char oneChar : password.toCharArray()) {
            if (!containOneDigit && Character.isDigit(oneChar)) {
                containOneDigit = true;
            }
            if (!containOneUpperCaseLetter && Character.isUpperCase(oneChar)) {
                containOneUpperCaseLetter = true;
            }
            if (!containOneLowerCaseLetter && Character.isLowerCase(oneChar)) {
                containOneLowerCaseLetter = true;
            }
            if (containOneDigit && containOneUpperCaseLetter && containOneLowerCaseLetter) {
                break;
            }
        }
        if (password.isEmpty()) {
            return "Error! Enter password";
        } else if (password.contains(" ")) {
            return "Error! Password should not contain spaces";
        } else if (!containOneDigit) {
            return "Error! Password should contain at least one digit";
        } else if (!containOneUpperCaseLetter) {
            return "Error! Password should contain at least one uppercase letter";
        } else if (!containOneLowerCaseLetter) {
            return "Error! Password should contain at least one lowercase letter";
        } else if (password.length() < 8) {
            return "Error! Password should contain 8 or more characters";
        } else {
            return "";
        }
    }

    //check confirm password
    //and match confirm password with password
    public int matchPasswordAndConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            return 2;
        } else if (!password.equals(confirmPassword)) {

            return 1;
        } else if (password.equals(confirmPassword)) {
            return 0;
        } else {
            return 3;
        }
    }
}
