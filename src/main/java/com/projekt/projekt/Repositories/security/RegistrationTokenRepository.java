package com.projekt.projekt.Repositories.security;

import com.projekt.projekt.Models.security.RegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, Integer> {
    RegistrationToken getRegistrationTokenByConfirmationToken(String confiramtionToken);
    Boolean existsByConfirmationToken(String confiramtionToken);
}
