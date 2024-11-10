package IRepositories;

import entities.Car;
import exceptions.RepositoryException;
import params.CarParams;

import java.util.List;

public interface ICarRepository {
    Car getCar(int id) throws RepositoryException;
    boolean saveCar(Car cr) throws RepositoryException;
    boolean delCar(int del_id) throws RepositoryException;
    boolean editCar(Car cr) throws RepositoryException;
    List<Car> getCarsByParams(CarParams params) throws RepositoryException;
}
