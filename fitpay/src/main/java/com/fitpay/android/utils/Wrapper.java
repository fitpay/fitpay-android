package com.fitpay.android.utils;

public class Wrapper<T> {

    private String filter;
    private Class<?> clazz;
    private T object;

    public Wrapper(String filter, T object) {
        this.filter = filter;
        this.object = object;
        clazz = object.getClass();
    }

    public String getFilter() {
        return filter;
    }

    public T getObject() {
        return object;
    }

    public Class getClazz() {
        return clazz;
    }
}
