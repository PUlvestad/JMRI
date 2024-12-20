// ImportCars.java
package jmri.jmrit.operations.rollingstock.cars;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import javax.swing.JOptionPane;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.ImportRollingStock;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This routine will import cars into the operation database.
 *
 * Each field is space or comma delimited. Field order: Number Road Type Length
 * Weight Color Owner Built Location - Track Load Kernel Moves Value Comment
 * Miscellaneous Extensions 
 *
 * @author Dan Boudreau Copyright (C) 2008 2010 2011, 2013, 2016
 * @version $Revision$
 */
public class ImportCars extends ImportRollingStock {

    CarManager manager = CarManager.instance();

    private int weightResults = JOptionPane.NO_OPTION; // Automatically calculate weight for car if weight entry is not
    // found
    private boolean autoCalculate = true;
    private boolean askAutoCreateTypes = true;
    private boolean askAutoCreateLocations = true;
    private boolean askAutoCreateTracks = true;
    private boolean askAutoLocationType = true;
    private boolean askAutoIncreaseTrackLength = true;
    private boolean askAutoForceCar = true;

    private boolean autoCreateTypes = false;
    private boolean autoCreateLocations = false;
    private boolean autoCreateTracks = false;
    private boolean autoAdjustLocationType = false;
    private boolean autoAdjustTrackLength = false;
    private boolean autoForceCar = false;

    private boolean autoCreateRoads = true;
    private boolean autoCreateLengths = true;
    private boolean autoCreateColors = true;
    private boolean autoCreateOwners = true;

    // we use a thread so the status frame will work!
    public void run() {
        File file = getFile();
        if (file == null) {
            return;
        }
        BufferedReader in = getBufferedReader(file);
        if (in == null) {
            return;
        }

        createStatusFrame(Bundle.getMessage("ImportCars"));

        // Now read the input file
        boolean importOkay = false;
        boolean comma = false;
        boolean importKernel = false;
        int lineNum = 0;
        int carsAdded = 0;
        String line = " ";
        String carNumber;
        String carRoad;
        String carType;
        String carLength;
        String carWeight;
        String carColor = "";
        String carOwner = "";
        String carBuilt = "";
        String carLocation = "";
        String carTrack = "";
        String carLoad = "";
        String carKernel = "";
        String[] inputLine;

        // does the file name end with .csv?
        if (file.getAbsolutePath().endsWith(".csv")) { // NOI18N
            log.info("Using comma as delimiter for import cars");
            comma = true;
        }

        while (true) {
            lineNumber.setText(Bundle.getMessage("LineNumber") + " " + Integer.toString(++lineNum));
            try {
                line = in.readLine();
            } catch (IOException e) {
                break;
            }

            if (line == null) {
                importOkay = true;
                break;
            }

            // has user canceled import?
            if (!fstatus.isShowing()) {
                break;
            }

            line = line.trim();
            if (log.isDebugEnabled()) {
                log.debug("Import: {}", line);
            }
            importLine.setText(line);

            if (line.startsWith(Bundle.getMessage("Number"))) {
                continue; // skip header
            }
            if (line.equalsIgnoreCase("kernel")) { // NOI18N
                log.info("Importing kernel names");
                importKernel = true;
                continue;
            }
            if (line.equalsIgnoreCase("comma")) { // NOI18N
                log.info("Using comma as delimiter for import cars");
                comma = true;
                continue;
            }
            // use comma as delimiter if found otherwise use spaces
            if (comma) {
                inputLine = parseCommaLine(line, 11);
            } else {
                inputLine = line.split("\\s+"); // NOI18N
            }
            if (inputLine.length < 1 || line.equals("")) {
                log.debug("Skipping blank line");
                continue;
            }
            int base = 1;
            if (comma || !inputLine[0].equals("")) {
                base--; // skip over any spaces at start of line
            }

            if (inputLine.length > base + 3) {

                carNumber = inputLine[base + 0];
                carRoad = inputLine[base + 1];
                carType = inputLine[base + 2];
                carLength = inputLine[base + 3];
                carWeight = "0";
                carColor = "";
                carOwner = "";
                carBuilt = "";
                carLocation = "";
                carTrack = "";
                carLoad = CarLoads.instance().getDefaultEmptyName();
                carKernel = "";

                if (inputLine.length > base + 4) {
                    carWeight = inputLine[base + 4];
                }
                if (inputLine.length > base + 5) {
                    carColor = inputLine[base + 5];
                }

                log.debug("Checking car number ({}) road ({}) type ({}) length ({}) weight ({}) color ({})", carNumber,
                        carRoad, carType, carLength, carWeight, carColor); // NOI18N
                if (carNumber.length() > Control.max_len_string_road_number) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarRoadNumberTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carNumber}), MessageFormat.format(Bundle
                                    .getMessage("carRoadNum"), new Object[]{Control.max_len_string_road_number + 1}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carRoad.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarRoadNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carRoad}), MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carType.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarTypeNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carType}), MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (!CarTypes.instance().containsName(carType)) {
                    if (autoCreateTypes) {
                        log.debug("Adding car type ({})", carType);
                        CarTypes.instance().addName(carType);
                    } else {
                        int results = JOptionPane.showConfirmDialog(null,
                                Bundle.getMessage("Car")
                                + " ("
                                + carRoad
                                + " "
                                + carNumber
                                + ")"
                                + NEW_LINE
                                + MessageFormat.format(Bundle.getMessage("typeNameNotExist"),
                                        new Object[]{carType}), Bundle.getMessage("carAddType"),
                                JOptionPane.YES_NO_CANCEL_OPTION);
                        if (results == JOptionPane.YES_OPTION) {
                            CarTypes.instance().addName(carType);
                            if (askAutoCreateTypes) {
                                results = JOptionPane.showConfirmDialog(null, Bundle
                                        .getMessage("DoYouWantToAutoAddCarTypes"), Bundle.getMessage("OnlyAskedOnce"),
                                        JOptionPane.YES_NO_OPTION);
                                if (results == JOptionPane.YES_OPTION) {
                                    autoCreateTypes = true;
                                }
                            }
                            askAutoCreateTypes = false;
                        } else if (results == JOptionPane.CANCEL_OPTION) {
                            break;
                        }
                    }
                }
                if (carLength.length() > Control.max_len_string_length_name) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarLengthNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carLength}), MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_length_name}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carLength.equals("")) {
                    log.debug("Car ({} {}) length not specified", carRoad, carNumber);
                    JOptionPane.showMessageDialog(null, MessageFormat.format(
                            Bundle.getMessage("CarLengthNotSpecified"), new Object[]{(carRoad + " " + carNumber)}),
                            Bundle.getMessage("CarLengthMissing"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                try {
                    Integer.parseInt(carLength);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("CarLengthNameNotNumber"), new Object[]{(carRoad + " " + carNumber),
                                carLength}), Bundle.getMessage("CarLengthMissing"), JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carWeight.length() > Control.max_len_string_weight_name) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarWeightNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carWeight}), MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_weight_name}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                if (carColor.length() > Control.max_len_string_attibute) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("CarColorNameTooLong"),
                            new Object[]{(carRoad + " " + carNumber), carColor}), MessageFormat.format(Bundle
                                    .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                }
                // calculate car weight if "0"
                if (carWeight.equals("0")) {
                    try {
                        double doubleCarLength = Double.parseDouble(carLength) * 12 / Setup.getScaleRatio();
                        double doubleCarWeight = (Setup.getInitalWeight() + doubleCarLength * Setup.getAddWeight()) / 1000;
                        NumberFormat nf = NumberFormat.getNumberInstance();
                        nf.setMaximumFractionDigits(1);
                        carWeight = nf.format(doubleCarWeight); // car weight in ounces.
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("carLengthMustBe"), Bundle
                                .getMessage("carWeigthCanNot"), JOptionPane.ERROR_MESSAGE);
                    }
                }
                Car c = manager.getByRoadAndNumber(carRoad, carNumber);
                if (c != null) {
                    log.info("Can not add, car number (" + carNumber + ") road (" + carRoad + ") already exists!"); // NOI18N
                } else {
                    if (inputLine.length > base + 6) {
                        carOwner = inputLine[base + 6];
                        if (carOwner.length() > Control.max_len_string_attibute) {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CarOwnerNameTooLong"), new Object[]{(carRoad + " " + carNumber),
                                        carOwner}), MessageFormat.format(Bundle.getMessage("carAttribute"),
                                            new Object[]{Control.max_len_string_attibute}), JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }
                    if (inputLine.length > base + 7) {
                        carBuilt = inputLine[base + 7];
                        if (carBuilt.length() > Control.max_len_string_built_name) {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CarBuiltNameTooLong"), new Object[]{(carRoad + " " + carNumber),
                                        carBuilt}), MessageFormat.format(Bundle.getMessage("carAttribute"),
                                            new Object[]{Control.max_len_string_built_name}), JOptionPane.ERROR_MESSAGE);
                            break;
                        }
                    }
                    if (inputLine.length > base + 8) {
                        carLocation = inputLine[base + 8];

                    }
                    // Location name can be one to three words
                    if (inputLine.length > base + 9) {
                        if (!inputLine[base + 9].equals("-")) {
                            carLocation = carLocation + " " + inputLine[base + 9];
                            if (inputLine.length > base + 10) {
                                if (!inputLine[base + 10].equals("-")) {
                                    carLocation = carLocation + " " + inputLine[base + 10];
                                }
                            }
                        }
                        log.debug("Car ({} {}) has location ({})", carRoad, carNumber, carLocation);
                        // now get the track name
                        boolean foundDash = false;
                        for (int i = base + 9; i < inputLine.length; i++) {
                            if (inputLine[i].equals("-")) {
                                foundDash = true;
                                if (inputLine.length > i + 1) {
                                    carTrack = inputLine[++i];
                                }
                            } else if (foundDash && !comma) {
                                carTrack = carTrack + " " + inputLine[i];
                            }
                        }
                        if (carTrack == null) {
                            carTrack = "";
                        }
                        log.debug("Car ({} {}) has track ({})", carRoad, carNumber, carTrack);
                    }
                    
                    // is there a load name?
                    if (comma && inputLine.length > base + 11) {
                        carLoad = inputLine[11];
                        log.debug("Car ({} {}) has load ({})", carRoad, carNumber, carLoad);
                    }
                    // is there a kernel name?
                    if (comma && inputLine.length > base + 12) {
                        carKernel = inputLine[12];
                        log.debug("Car ({} {}) has kernel name ({})", carRoad, carNumber, carKernel);
                    }
                    
                    if (carLocation.length() > Control.max_len_string_location_name) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("CarLocationNameTooLong"), new Object[]{(carRoad + " " + carNumber),
                                    carLocation}), MessageFormat.format(Bundle.getMessage("carAttribute"),
                                        new Object[]{Control.max_len_string_location_name}), JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    if (carTrack.length() > Control.max_len_string_track_name) {
                        JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                .getMessage("CarTrackNameTooLong"), new Object[]{(carRoad + " " + carNumber),
                                    carTrack}), MessageFormat.format(Bundle.getMessage("carAttribute"),
                                        new Object[]{Control.max_len_string_track_name}), JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                    Location location = LocationManager.instance().getLocationByName(carLocation);
                    Track track = null;
                    if (location == null && !carLocation.equals("")) {
                        if (autoCreateLocations) {
                            log.debug("Create location ({})", carLocation);
                            location = LocationManager.instance().newLocation(carLocation);
                        } else {
                            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CarLocationDoesNotExist"), new Object[]{(carRoad + " " + carNumber),
                                        carLocation}), Bundle.getMessage("carLocation"), JOptionPane.ERROR_MESSAGE);
                            int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                    .getMessage("DoYouWantToCreateLoc"), new Object[]{carLocation}), Bundle
                                    .getMessage("carLocation"), JOptionPane.YES_NO_OPTION);
                            if (results == JOptionPane.YES_OPTION) {
                                log.debug("Create location ({})", carLocation);
                                location = LocationManager.instance().newLocation(carLocation);
                                if (askAutoCreateLocations) {
                                    results = JOptionPane.showConfirmDialog(null, Bundle
                                            .getMessage("DoYouWantToAutoCreateLoc"),
                                            Bundle.getMessage("OnlyAskedOnce"), JOptionPane.YES_NO_OPTION);
                                    if (results == JOptionPane.YES_OPTION) {
                                        autoCreateLocations = true;
                                    }
                                }
                                askAutoCreateLocations = false;
                            } else {
                                break;
                            }
                        }
                    }
                    if (location != null && !carTrack.equals("")) {
                        track = location.getTrackByName(carTrack, null);
                        if (track == null) {
                            if (autoCreateTracks) {
                                if (location.getLocationOps() == Location.NORMAL) {
                                    log.debug("Create 1000 foot yard track ({})", carTrack);
                                    track = location.addTrack(carTrack, Track.YARD);
                                } else {
                                    log.debug("Create 1000 foot staging track ({})", carTrack);
                                    track = location.addTrack(carTrack, Track.STAGING);
                                }
                                track.setLength(1000);
                            } else {
                                JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                        .getMessage("CarTrackDoesNotExist"), new Object[]{
                                            (carRoad + " " + carNumber), carTrack, carLocation}), Bundle
                                        .getMessage("carTrack"), JOptionPane.ERROR_MESSAGE);
                                int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                        .getMessage("DoYouWantToCreateTrack"), new Object[]{carTrack, carLocation}),
                                        Bundle.getMessage("carTrack"), JOptionPane.YES_NO_OPTION);
                                if (results == JOptionPane.YES_OPTION) {
                                    if (location.getLocationOps() == Location.NORMAL) {
                                        log.debug("Create 1000 foot yard track ({})", carTrack);
                                        track = location.addTrack(carTrack, Track.YARD);
                                    } else {
                                        log.debug("Create 1000 foot staging track ({})", carTrack);
                                        track = location.addTrack(carTrack, Track.STAGING);
                                    }
                                    track.setLength(1000);
                                    if (askAutoCreateTracks) {
                                        results = JOptionPane.showConfirmDialog(null, Bundle
                                                .getMessage("DoYouWantToAutoCreateTrack"), Bundle
                                                .getMessage("OnlyAskedOnce"), JOptionPane.YES_NO_OPTION);
                                        if (results == JOptionPane.YES_OPTION) {
                                            autoCreateTracks = true;
                                        }
                                        askAutoCreateTracks = false;
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    log.debug("Add car ({} {}) owner ({}) built ({}) location ({}, {})", carRoad, carNumber, carOwner,
                            carBuilt, carLocation, carTrack);
                    Car car = manager.newCar(carRoad, carNumber);
                    car.setTypeName(carType);
                    car.setLength(carLength);
                    car.setWeight(carWeight);
                    car.setColor(carColor);
                    car.setOwner(carOwner);
                    car.setBuilt(carBuilt);
                    car.setLoadName(carLoad);
                    car.setKernel(manager.newKernel(carKernel));
                    carsAdded++;
                    
                    car.setCaboose(carType.equals("Caboose"));
                    // determine if there are any car extensions
                    if (comma && inputLine.length > base + 17) {
                        String extensions = inputLine[17];
                        log.debug("Car ({} {}) has extension ({})", carRoad, carNumber, extensions);
                        String[] ext = extensions.split(Car.EXTENSION_REGEX);
                        for (int i = 0; i < ext.length; i++) {
                            if (ext[i].equals(Car.CABOOSE_EXTENSION)) {
                                car.setCaboose(true);
                            }
                            if (ext[i].equals(Car.FRED_EXTENSION)) {
                                car.setFred(true);
                            }
                            if (ext[i].equals(Car.PASSENGER_EXTENSION)) {
                                car.setPassenger(true);
                                car.setBlocking(Integer.parseInt(ext[i + 1]));
                            }
                            if (ext[i].equals(Car.UTILITY_EXTENSION)) {
                                car.setUtility(true);
                            }
                            if (ext[i].equals(Car.HAZARDOUS_EXTENSION)) {
                                car.setHazardous(true);
                            }
                        }
                    }

                    // add new roads
                    if (!CarRoads.instance().containsName(carRoad)) {
                        if (autoCreateRoads) {
                            log.debug("add car road {}", carRoad);
                            CarRoads.instance().addName(carRoad);
                        }
                    }

                    // add new lengths
                    if (!CarLengths.instance().containsName(carLength)) {
                        if (autoCreateLengths) {
                            log.debug("add car length {}", carLength);
                            CarLengths.instance().addName(carLength);
                        }
                    }

                    // add new colors
                    if (!CarColors.instance().containsName(carColor)) {
                        if (autoCreateColors) {
                            log.debug("add car color {}", carColor);
                            CarColors.instance().addName(carColor);
                        }
                    }

                    // add new owners
                    if (!CarOwners.instance().containsName(carOwner)) {
                        if (autoCreateOwners) {
                            log.debug("add car owner {}", carOwner);
                            CarOwners.instance().addName(carOwner);
                        }
                    }

                    if (car.getWeight().equals("")) {
                        log.debug("Car ({} {}) weight not specified", carRoad, carNumber);
                        if (weightResults != JOptionPane.CANCEL_OPTION) {
                            weightResults = JOptionPane.showOptionDialog(null, MessageFormat.format(Bundle
                                    .getMessage("CarWeightNotFound"), new Object[]{(carRoad + " " + carNumber)}),
                                    Bundle.getMessage("CarWeightMissing"), JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE, null, new Object[]{
                                        Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                                        Bundle.getMessage("ButtonDontShow")}, autoCalculate ? Bundle
                                            .getMessage("ButtonYes") : Bundle.getMessage("ButtonNo"));
                        }
                        if (weightResults == JOptionPane.NO_OPTION) {
                            autoCalculate = false;
                        }
                        if (weightResults == JOptionPane.YES_OPTION || autoCalculate == true
                                && weightResults == JOptionPane.CANCEL_OPTION) {
                            autoCalculate = true;
                            try {
                                double carLen = Double.parseDouble(car.getLength()) * 12 / Setup.getScaleRatio();
                                double carWght = (Setup.getInitalWeight() + carLen * Setup.getAddWeight()) / 1000;
                                NumberFormat nf = NumberFormat.getNumberInstance();
                                nf.setMaximumFractionDigits(1);
                                car.setWeight(nf.format(carWght)); // car weight in ounces.
                                int tons = (int) (carWght * Setup.getScaleTonRatio());
                                // adjust weight for caboose
                                if (car.isCaboose()) {
                                    tons = (int) (Double.parseDouble(car.getLength()) * .9); // .9 tons/foot
                                }
                                car.setWeightTons(Integer.toString(tons));
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, Bundle.getMessage("carLengthMustBe"), Bundle
                                        .getMessage("carWeigthCanNot"), JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                    if (location != null && track != null) {
                        String status = car.setLocation(location, track);
                        if (!status.equals(Track.OKAY)) {
                            log.debug("Can't set car's location because of {}", status);
                            if (status.startsWith(Track.TYPE)) {
                                if (autoAdjustLocationType) {
                                    location.addTypeName(carType);
                                    track.addTypeName(carType);
                                    status = car.setLocation(location, track);
                                } else {
                                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                            .getMessage("CanNotSetCarAtLocation"), new Object[]{
                                                (carRoad + " " + carNumber), carType, carLocation, carTrack, status}),
                                            Bundle.getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
                                    int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                            .getMessage("DoYouWantToAllowService"), new Object[]{carLocation,
                                                carTrack, (carRoad + " " + carNumber), carType}), Bundle
                                            .getMessage("ServiceCarType"), JOptionPane.YES_NO_OPTION);
                                    if (results == JOptionPane.YES_OPTION) {
                                        location.addTypeName(carType);
                                        track.addTypeName(carType);
                                        status = car.setLocation(location, track);
                                        log.debug("Set car's location status: {}", status);
                                        if (askAutoLocationType) {
                                            results = JOptionPane.showConfirmDialog(null, Bundle
                                                    .getMessage("DoYouWantToAutoAdjustLocations"), Bundle
                                                    .getMessage("OnlyAskedOnce"), JOptionPane.YES_NO_OPTION);
                                            if (results == JOptionPane.YES_OPTION) {
                                                autoAdjustLocationType = true;
                                            }
                                            askAutoLocationType = false;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            if (status.startsWith(Track.LENGTH)) {
                                if (autoAdjustTrackLength) {
                                    track.setLength(track.getLength() + 1000);
                                    status = car.setLocation(location, track);
                                    log.debug("Set track length status: {}", status);
                                } else {
                                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                            .getMessage("CanNotSetCarAtLocation"), new Object[]{
                                                (carRoad + " " + carNumber), carType, carLocation, carTrack, status}),
                                            Bundle.getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
                                    int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                            .getMessage("DoYouWantIncreaseLength"), new Object[]{carTrack}), Bundle
                                            .getMessage("TrackLength"), JOptionPane.YES_NO_OPTION);
                                    if (results == JOptionPane.YES_OPTION) {
                                        track.setLength(track.getLength() + 1000);
                                        status = car.setLocation(location, track);
                                        log.debug("Set track length status: {}", status);
                                        if (askAutoIncreaseTrackLength) {
                                            results = JOptionPane.showConfirmDialog(null, Bundle
                                                    .getMessage("DoYouWantToAutoAdjustTrackLength"), Bundle
                                                    .getMessage("OnlyAskedOnce"), JOptionPane.YES_NO_OPTION);
                                            if (results == JOptionPane.YES_OPTION) {
                                                autoAdjustTrackLength = true;
                                            }
                                            askAutoIncreaseTrackLength = false;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            if (!status.equals(Track.OKAY)) {
                                if (autoForceCar) {
                                    car.setLocation(location, track, true); // force car
                                } else {
                                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                                            .getMessage("CanNotSetCarAtLocation"), new Object[]{
                                                (carRoad + " " + carNumber), carType, carLocation, carTrack, status}),
                                            Bundle.getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
                                    int results = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                                            .getMessage("DoYouWantToForceCar"), new Object[]{
                                                (carRoad + " " + carNumber), carLocation, carTrack}), Bundle
                                            .getMessage("OverRide"), JOptionPane.YES_NO_OPTION);
                                    if (results == JOptionPane.YES_OPTION) {
                                        car.setLocation(location, track, true); // force car
                                        if (askAutoForceCar) {
                                            results = JOptionPane.showConfirmDialog(null, Bundle
                                                    .getMessage("DoYouWantToAutoForceCar"), Bundle
                                                    .getMessage("OnlyAskedOnce"), JOptionPane.YES_NO_OPTION);
                                            if (results == JOptionPane.YES_OPTION) {
                                                autoForceCar = true;
                                            }
                                            askAutoForceCar = false;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // log.debug("No location for car ("+carRoad+" "+carNumber+")");
                    }
                }
            } else if (importKernel && inputLine.length == base + 3) {
                carNumber = inputLine[base + 0];
                carRoad = inputLine[base + 1];
                String kernelName = inputLine[base + 2];
                Car car = manager.getByRoadAndNumber(carRoad, carNumber);
                if (car != null) {
                    Kernel kernel = manager.newKernel(kernelName);
                    car.setKernel(kernel);
                    carsAdded++;
                } else {
                    log.info("Car number (" + carNumber + ") road (" + carRoad + ") does not exist!"); // NOI18N
                    break;
                }
            } else if (!line.equals("")) {
                log.info("Car import line " + lineNum + " missing attributes: " + line);
                JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportMissingAttributes"),
                        new Object[]{lineNum})
                        + NEW_LINE + line + NEW_LINE + Bundle.getMessage("ImportMissingAttributes2"), Bundle
                        .getMessage("CarAttributeMissing"), JOptionPane.ERROR_MESSAGE);
                break;
            }
        }
        try {
            in.close();
        } catch (IOException e) {
        }

        // kill status panel
        fstatus.dispose();

        if (importOkay) {
            JOptionPane
                    .showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportCarsAdded"),
                                    new Object[]{carsAdded}), Bundle.getMessage("SuccessfulImport"),
                            JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("ImportCarsAdded"),
                    new Object[]{carsAdded}), Bundle.getMessage("ImportFailed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ImportCars.class.getName());
}
