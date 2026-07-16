package ru.codeportfolio.dao;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.codeportfolio.models.Users;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    @Override
    <S extends Users> boolean exists(Example<S> example);

    List<Users> findUsersByLogin(String login);

}
