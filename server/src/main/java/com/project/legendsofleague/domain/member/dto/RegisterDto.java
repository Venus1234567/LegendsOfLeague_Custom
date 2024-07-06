package com.project.legendsofleague.domain.member.dto;

import com.project.legendsofleague.domain.member.domain.ROLE;
import com.project.legendsofleague.domain.member.exception.InvalidEmailException;
import com.project.legendsofleague.domain.member.exception.InvalidIdInputException;
import com.project.legendsofleague.domain.member.exception.InvalidPasswordException;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class RegisterDto {
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{5,20}$";

    private static final String EMAIL_PATTERN = "^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,}$";

    private static final String PASSWORD_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$";

    private static final Pattern usernamePattern = Pattern.compile(USERNAME_PATTERN);

    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    private String username;

    private String password;

    private String nickname;

    private String email;

    private ROLE role;

    public static void validate(RegisterDto dto) {

        if (!dto.isUsernameValid()) {
            throw new InvalidIdInputException(dto.getUsername());
        }

        if (!dto.isPasswordValid()) {
            throw new InvalidPasswordException(dto.getPassword());
        }

        if (!dto.isEmailValid()) {
            throw new InvalidEmailException(dto.getEmail());
        }
    }

    public boolean isUsernameValid() {
        Matcher matcher = usernamePattern.matcher(username);
        return matcher.matches();
    }

    public boolean isEmailValid() {
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    public boolean isPasswordValid() {
        Matcher matcher = passwordPattern.matcher(password);
        return matcher.matches();
    }
}
