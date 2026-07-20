package ru.codeportfolio.dao.func_interfaces;

@FunctionalInterface
public interface FunctionThrowing<T, R> {
    R apply(T t) throws Exception;
}
