package com.projekt.projekt.Repositories;

import com.projekt.projekt.Models.Role;
import com.projekt.projekt.Models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByTelNumber(String telNumber);
    List<User> findAllByRole(Role role);
    @Query(value="SELECT c FROM User c WHERE c.name like %:search% or cast( c.id AS string ) like %:search% or  c.email like %:search% or c.telNumber like %:search% or c.role.name like %:search%")
    Page<User> findAllByNameContainingOrEmailContaining(String search,Pageable pageable);
    List<User> findAllByEmailContaining(String email);
    @Query("SELECT b FROM User b WHERE EXTRACT (day FROM b.registrationDate) = :day AND EXTRACT (month FROM b.registrationDate) = :month  AND EXTRACT (year FROM b.registrationDate) = :year")
    List<User> findAllByRegistrationDay(Integer day,Integer month, Integer year);
}
