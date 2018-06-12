package com.odw.ridesharing.controllers;

import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.odw.ridesharing.factories.CarFactory;
import com.odw.ridesharing.model.CarType;
import com.odw.ridesharing.model.RuntimeConstants;
import com.odw.ridesharing.model.abstractmodel.AbstractCar;

import com.odw.ridesharing.model.exceptions.CarNotFoundException;
import com.odw.ridesharing.model.exceptions.InvalidCarArgumentsException;

import java.util.ArrayList;
import java.util.Map;

/**
 * CarController is called by CommandController to handle the commands done on
 * Car. Car Controller calls CarFactory to create a Car and handles modifying
 * and deleting of Car object.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CarController {

    private ConcurrentHashMap<Integer, AbstractCar> carDatabase = new ConcurrentHashMap<Integer, AbstractCar>();
    
    @XmlTransient
    private CarFactory carFactory = new CarFactory();

    /**
     * 
     * Call CarFactory to create a car
     * 
     * @param typeValues_
     *            Expected input values specified under CREATE_CAR_FORMAT in
     *            RuntimeConstants.
     * @return _car To be used for logger
     * @throws InvalidCarArgumentsException
     */    

    public AbstractCar createCar(ArrayList<String> typeValues_) throws InvalidCarArgumentsException {
        if (typeValues_.size() == RuntimeConstants.CREATE_CAR_FORMAT.length) {
            try {
            	CarType _carType = CarType.valueOf((typeValues_.get(0).toUpperCase()));
                String _make = typeValues_.get(1);
                String _model = typeValues_.get(2);
                String _color = typeValues_.get(3);
                int _year = Integer.parseInt(typeValues_.get(4));
            
                AbstractCar _car = carFactory.buildCar(_carType, _make, _model, _color, _year);
                carDatabase.put(_car.getCarID(), _car);
                return _car;
            } catch (Exception e_) {
                throw new InvalidCarArgumentsException();
            }
        }
        // Something went wrong..
        throw new InvalidCarArgumentsException();
    }

    /**
     * Modifies the car specified by the ID by creating a new car and deleting the
     * old car.
     * 
     * @param typeValues_
     *            Expected input values specified under MODIFY_CAR_FORMAT in
     *            RuntimeConstants.
     */
    public AbstractCar modifyCar(ArrayList<String> typeValues_) throws CarNotFoundException, InvalidCarArgumentsException {
        if (typeValues_.size() == RuntimeConstants.MODIFY_CAR_FORMAT.length) {
            try {
                // Removing the carID from typeValues_ so that it is properly formatted
                // to CREATE_CAR_FORMAT (see RuntimeConstants) buildCar (in carFactory).
                int _carID = Integer.parseInt(typeValues_.remove(0));

                // Removing the old car from the system.
                AbstractCar _oldCar = carDatabase.remove(_carID);

                CarType _carType = CarType.valueOf((typeValues_.get(0).toUpperCase()));
                String _make = typeValues_.get(1);
                String _model = typeValues_.get(2);
                String _color = typeValues_.get(3);
                int _year = Integer.parseInt(typeValues_.get(4));
                            
                // Modifying the existing car by replacing it with a new car.
                AbstractCar _newlyModifiedCar = carFactory.buildCar(_carType, _make, _model, _color, _year);

                // Setting this car ID to _oldCar's ID. NullPointerException if _oldCar is null.
                _newlyModifiedCar.setCarID(_oldCar.getCarID());

                // Replacing the old car with the new car.
                carDatabase.put(_newlyModifiedCar.getCarID(), _newlyModifiedCar);

                return _newlyModifiedCar;
            } catch (NullPointerException e_) {
                throw new CarNotFoundException();
            } catch (NumberFormatException e_) {
                throw new InvalidCarArgumentsException();
            }
        }

        // Something went wrong..
        throw new CarNotFoundException();
    }

    /**
     * Delete the car's info from the database
     * 
     * @param typeValues_
     *            Expected input values specified under DELETE_CAR_FORMAT in
     *            RuntimeConstants.
     * @return carDatabase.remove(_carID) The object to be removed
     * @throws CarNotFoundException
     */
    public AbstractCar deleteCar(ArrayList<String> typeValues_) throws CarNotFoundException {
        if (typeValues_.size() == RuntimeConstants.DELETE_CAR_FORMAT.length) {
            int _carID = Integer.parseInt(typeValues_.get(0));

            if (carDatabase.get(_carID) != null) {
                return carDatabase.remove(_carID);
            } else {
                throw new CarNotFoundException();
            }
        }

        // Something went wrong..
        throw new CarNotFoundException();
    }

    /**
     * Check to see if a car is in database.
     * 
     * @param carID_
     *            The ID used to lookup the car.
     * @return Returns true if the car is in database. False otherwise.
     */
    public boolean isCarInDatabase(int carID_) {
        return carDatabase.get(carID_) != null; // If null then car doesn't exist in database.
    }
    
    /**
     * Returns a string of all the cars in database.
     * 
     * @return A list string of all the cars currently in database.
     */
    public String getCarDatabaseAsString() {
        if (!carDatabase.isEmpty()) {
            StringBuilder _result = new StringBuilder();

            for (Map.Entry<Integer, AbstractCar> _entry : carDatabase.entrySet()) {
                AbstractCar _currentCar = _entry.getValue();

                _result.append(System.lineSeparator() + _currentCar.toString());
            }

            return _result.toString();
        }

        return "";
    }

}
