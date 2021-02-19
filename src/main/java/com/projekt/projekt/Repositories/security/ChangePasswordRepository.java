package com.projekt.projekt.Repositories.security;

import com.projekt.projekt.Models.security.ChangePassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangePasswordRepository extends JpaRepository<ChangePassword, Integer> {
    ChangePassword getChangePasswordByConfirmationToken(String confiramtionToken);
    Boolean existsByConfirmationToken(String confiramtionToken);
    ChangePassword findByEmail(String email);
    Boolean existsByEmail(String email);
}
