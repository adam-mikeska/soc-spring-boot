package com.projekt.projekt.Repositories.security;

import com.projekt.projekt.Models.security.ChangeEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeEmailRepository extends JpaRepository<ChangeEmail, Integer> {
    ChangeEmail getChangeEmailByConfirmationToken(String confiramtionToken);
    Boolean existsByConfirmationToken(String confiramtionToken);
    Boolean existsByCurrentEmail(String currentEmail);
    Boolean existsByNewEmail(String email);
    List<ChangeEmail> findAllByCurrentEmail(String email);
}
