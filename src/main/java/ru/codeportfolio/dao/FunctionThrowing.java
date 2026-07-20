package ru.codeportfolio.dao;

@FunctionalInterface
public interface FunctionThrowing<T, R> {
    R apply(T t) throws Exception;
}
