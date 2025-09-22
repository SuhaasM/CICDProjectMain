package com.example.lmsbackend.repository;

import com.example.lmsbackend.model.Profile;
import com.example.lmsbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);

    @Query("SELECT p FROM Profile p JOIN FETCH p.user WHERE p.user = :user")
    Optional<Profile> findByUserWithUserEagerly(@Param("user") User user);
}