package com.temperomineiro.erp.service;

import com.temperomineiro.erp.exception.BusinessException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class CredentialPolicyService {

    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern SYMBOL = Pattern.compile(".*[^A-Za-z0-9].*");

    public void validatePasswordStrength(String password) {
        if (password == null || password.isBlank()) {
            throw new BusinessException("A senha é obrigatória.");
        }

        if (password.length() < 8 || password.length() > 72
                || !UPPERCASE.matcher(password).matches()
                || !LOWERCASE.matcher(password).matches()
                || !DIGIT.matcher(password).matches()
                || !SYMBOL.matcher(password).matches()) {
            throw new BusinessException(
                    "A senha deve ter entre 8 e 72 caracteres e incluir letra maiúscula, letra minúscula, número e símbolo."
            );
        }
    }
}
