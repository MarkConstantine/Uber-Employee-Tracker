package com.odw.ridesharing.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.odw.ridesharing.model.*;
import com.odw.ridesharing.model.exceptions.*;

public class CommandController {

    private CarController carController = new CarController();
    private UserController userController = new UserController();
    private PickupController pickupController = new PickupController();
    private Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * Processes a file line-by-line by parsing each line into an event and
     * performing each event. Can be considered as the application's "starting
     * point".
     * 
     * @param fileName_
     *            The file name and path to process.
     * @param delimiter_
     *            The delimiter used in the file to separate values.
     */
    public void processFile(String fileName_, String delimiter_) {
        EventParser _eventParser = new EventParser();

        /* @formatter:off */
        
        try (BufferedReader _inputReader = new BufferedReader(
                                            new InputStreamReader(
                                             new FileInputStream(fileName_)))) {    
            // Process each event line-by-line.
            String _nextLine = null;
            while ((_nextLine = _inputReader.readLine()) != null) {
                try {
                    Event _nextEvent = _eventParser.parseEvent(_nextLine, delimiter_);
                    processEvent(_nextEvent);
                } catch (InvalidEventException e_) {
                    logger.error("Could not parse the given event: \"" + _nextLine + "\"");
                }
            }

            // SUCCESS!
            logger.debug("FINAL CAR INVENTORY:" + carController.getCarInventoryAsString());
            logger.debug("FINAL USER DATABASE:" + userController.getUserDatabaseAsString());
            logger.debug("PICKUP HISTORY:" + pickupController.getPickupHistoryAsString());
        } catch (FileNotFoundException e_) {
            logger.error("Could not find the specified file.");
        } catch (IOException e_) {
            logger.error("Something went wrong while reading the file.");
        }
        /* @formatter:on */
    }

    /**
     * Helper function for processFile. Decodes the current event's command.
     * 
     * @param newEvent_
     *            The current event to be decoded
     */
    private void processEvent(Event newEvent_) {
        switch (newEvent_.getCommand()) {
            case RuntimeConstants.CREATE:
                create(newEvent_);
                break;
            case RuntimeConstants.MODIFY:
                modify(newEvent_);
                break;
            case RuntimeConstants.DELETE:
                delete(newEvent_);
                break;
            default:
                logger.error("Error: Invalid command.");
                break;
        }
    }

    /**
     * Helper function for processEvent. Decodes the input type to create.
     * 
     * @param event_
     *            The current event to be decoded.
     */
    private void create(Event event_) {
        switch (event_.getInputType()) {
            case RuntimeConstants.CAR: {
                try {
                    Car _addedCar = carController.createCar(event_.getTypeValues());
                    logger.debug("CREATED CAR: " + _addedCar.toString());
                } catch (InvalidCarArgumentsException e_) {
                    logger.error("There was a problem with adding car: " + event_.typeValuesToString());
                }
                break;
            }
            case RuntimeConstants.USER: {
                try {
                    User _addedUser = userController.createUser(event_.getTypeValues());
                    logger.debug("CREATED USER: " + _addedUser.toString());
                } catch (InvalidUserArgumentsException e_) {
                    logger.error(
                            "The argument passed are not valid; unable to add user: " + event_.typeValuesToString());
                }
                break;
            }
            case RuntimeConstants.PICKUP: {
                try {
                    int _customerID = Integer.parseInt(event_.getTypeValues().get(0));
                    Customer _pickupCustomer = (Customer) userController.getCustomerByID(_customerID); // TODO error
                                                                                                       // handle
                    // this downcast.
                    Driver _scheduledDriver = (Driver) userController.getNextAvailableDriver(); // TODO error handle
                                                                                                // this downcast.
                    Pickup _addedPickup = pickupController.createPickup(event_.getTypeValues(), _pickupCustomer,
                            _scheduledDriver);
                    logger.debug("CREATED PICKUP: " + _addedPickup.toString());
                } catch (InvalidPickupArgumentsException e_) {
                    logger.error("There was a problem with creating pickup: " + event_.typeValuesToString());
                } catch (BadCustomerException e_) {
                    // TODO
                    // BadUserException
                }
                break;
            }
            default:
                logger.error("Error: Invalid input type.");
                break;
        }
    }

    /**
     * Helper function for processEvent. Decodes the input type to modify.
     * 
     * @param event_
     *            The current event to be decoded.
     */
    private void modify(Event event_) {
        switch (event_.getInputType()) {
            case RuntimeConstants.CAR: {
                try {
                    Car modifiedCar = carController.modifyCar(event_.getTypeValues());
                    logger.debug("MODIFIED CAR: " + modifiedCar.toString());
                } catch (BadCarException e_) {
                    logger.error("There was a problem with modifying car: " + event_.typeValuesToString());
                } catch (InvalidCarArgumentsException e_) {
                    logger.error("There was a problem with modifying car: " + event_.typeValuesToString());
                }
                break;
            }
            case RuntimeConstants.USER: {
                try {
                    User modifiedUser = userController.modifyUser(event_.getTypeValues());
                    logger.debug("MODIFIED USER: " + modifiedUser.toString());
                } catch (BadCustomerException e_) {
                    logger.error("There was a problem with modifying customer; customer does not exist: "
                            + event_.typeValuesToString("|"));
                } catch (BadDriverException e_) {
                    logger.error("There was a problem with modifying driver; driver does not exist: "
                            + event_.typeValuesToString("|"));
                } catch (InvalidUserArgumentsException e_) {
                    logger.error(
                            "The argument passed are not valid; unable to add user: " + event_.typeValuesToString());
                }
                break;
            }

            // ----- DEPRECATED! -----
            /*
             * case RuntimeConstants.PICKUP: { try { Pickup modifiedPickup =
             * pickupController.modifyPickup(event_.getTypeValues());
             * logger.debug("MODIFIED PICKUP: " + modifiedPickup.toString()); } catch
             * (BadPickupException e_) {
             * logger.error("There was a problem with modifying pickup: " +
             * event_.typeValuesToString("|")); } break; }
             */
            // -----------------------

            default:
                logger.error("Error: Invalid input type.");
                break;
        }
    }

    /**
     * Helper function for processEvent. Decodes the input type to delete.
     * 
     * @param event_
     *            The current event to be decoded.
     */
    private void delete(Event event_) {
        switch (event_.getInputType()) {
            case RuntimeConstants.CAR: {
                try {
                    Car deletedCar = carController.deleteCar(event_.getTypeValues());
                    logger.debug("DELETED CAR: " + deletedCar.toString());
                } catch (BadCarException e_) {
                    logger.error("There was a problem deleting car: " + event_.typeValuesToString());
                }
                break;
            }
            case RuntimeConstants.USER: {
                try {
                    User _deletedUser = userController.deleteUser(event_.getTypeValues());
                    logger.debug("DELETED USER: " + _deletedUser.toString());
                } catch (BadUserException e_) {
                    logger.error("There was a problem deleting user: " + event_.typeValuesToString());
                }
                break;
            }

            // ----- DEPRECATED! -----
            /*
             * case RuntimeConstants.PICKUP: { try { Pickup deletedPickup =
             * pickupController.deletePickup(event_.getTypeValues());
             * logger.debug("DELETED PICKUP: " + deletedPickup.toString()); } catch
             * (BadPickupException e_) {
             * logger.error("There was a problem with deleting pickup: " +
             * event_.typeValuesToString("|")); } break; }
             */
            // -----------------------

            default:
                logger.error("Error: Invalid input type.");
                break;
        }
    }
}
