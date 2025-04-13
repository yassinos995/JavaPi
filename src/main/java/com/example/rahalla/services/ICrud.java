package com.example.rahalla.services;

import java.util.List;

public interface ICrud<T> {
    List<T> getAll();

    T getById(int id);

    boolean add(T item);

    boolean update(T item);

    boolean remove(int id);

    List<T> search(String keyword);
}
