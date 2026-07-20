package ru.codeportfolio.dao.func_interfaces;

@FunctionalInterface
public interface ConsumerThrowing<T> {
    void apply(T t) throws Exception;
}
