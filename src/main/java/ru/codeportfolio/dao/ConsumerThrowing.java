package ru.codeportfolio.dao;

@FunctionalInterface
public interface ConsumerThrowing<T> {
    void apply(T t) throws Exception;
}
