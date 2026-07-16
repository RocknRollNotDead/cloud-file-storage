package ru.codeportfolio.dao;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.codeportfolio.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    <S extends User> boolean exists(Example<S> example);

    Optional<User> findUsersByLogin(String login);

}
