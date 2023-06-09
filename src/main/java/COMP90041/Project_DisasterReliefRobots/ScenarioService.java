package COMP90041.Project_DisasterReliefRobots;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class ScenarioService {

    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final String[] LINE_START = {"scenario", "location", "animal", "human"};
    private static final String PREGNANT = "pregnant";
    private static final String PET = "pet";
    private static final String[] BODY_TYPE = {"OVERWEIGHT", "AVERAGE", "ATHLETIC", "UNSPECIFIED"};
    private static final String[] GENDER = {"MALE", "FEMALE", "UNKNOWN"};
    private static final String[] STATUS = {"TRESPASSING", "LEGAL"};
    private static final String[] PROFESSION = {"DOCTOR", "CEO", "CRIMINAL", "HOMELESS", "UNEMPLOYED", "ATHLETIC", "STUDENT", "PROFESSOR", "NONE"};
    private static final String[] DISASTER = {"CYCLONE", "EARTHQUAKE", "BUSHFIRE", "METEORITE", "FLOOD"};
    private static final Character[] LATITUDE_DIRECTION = {'N', 'S'};
    private static final Character[] LONGITUDE_DIRECTION = {'E', 'W'};

    private static final Integer DEFAULT_LATITUDE = 45;
    private static final Integer DEFAULT_LONGITUDE = 90;
    private ArrayList<Scenario> scenarios;
    private HashMap<String, int[]> attributes;
    private boolean consent;
    private ArrayList<Integer> savedHumanAge;
    private String userLogPath;

    public ScenarioService(String userLogPath) {
        scenarios = new ArrayList<>();
        consent = false;
        attributes = new HashMap<>();
        savedHumanAge = new ArrayList<>();
        this.userLogPath = Objects.requireNonNullElse(userLogPath, "userRescueBot.csv");
        df.setRoundingMode(RoundingMode.UP);
    }

    public void loadScenariosFromFile(String scenariosFilePath) {
        loadScenarios(scenariosFilePath, scenarios);
    }

    /**
     * Load scenario from file path, and store them into scenario list.
     * Since the occurrence sequence is always scenario -> location -> resident(human or animal).
     * When we first meet a new scenario, we set this to the current scenario and add location into it.
     * Same for the location and resident.
     */
    private void loadScenarios(String scenariosFilePath, ArrayList<Scenario> scenarios) {
        Scenario currentScenario = null;
        Location currentLocation = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(scenariosFilePath))) {
            String line;
            int lineNumber = 0;
            // outerLoop while loop
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) {
                    continue; // Skip headers
                }
                //Only if the line start with these can be processed, otherwise just skip.
                if (!line.startsWith("s") && !line.startsWith("l") && !line.startsWith("a") && !line.startsWith("h")) {
                    continue;
                }
                //execute the file to obtain the information from it
                Object object = parseLine(line, lineNumber);
                // Append object to relevant scenario or location
                if (object instanceof Scenario) {
                    currentScenario = (Scenario) object;
                    scenarios.add(currentScenario);
                } else currentLocation = getLocationOrResident(currentScenario, currentLocation, object);
            }
        } catch (FileNotFoundException e) {
            System.out.println("java.io.FileNotFoundException: could not find scenarios file.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get location or resident, if is location, we return it,
     * if is resident then add resident into the current location.
     * @return location
     */
    public Location getLocationOrResident(Scenario currentScenario, Location currentLocation, Object object) {
        if (object instanceof Location) {
            currentLocation = (Location) object;
            if (currentScenario != null) {
                currentScenario.getLocations().add(currentLocation);
            }
        } else {
            if (currentScenario != null && currentLocation != null) {
                // set trespassing attribute of animals
                if (currentLocation.isTrespassing()) {
                    ((Resident) object).setTrespassing(true);
                }
                // add current resident to this location
                currentLocation.getResidents().add((Resident) object);
            }
        }
        return currentLocation;
    }

    /**
     * Decide what this line's about
     * @return an object, can be Scenario, Location or Resident
     */

    public Object parseLine(String line, int lineNumber) {
        String[] data = line.split(",", -1);
        // Check for invalid data format
        try {
            if (data.length != 8) {
                throw new InvalidDataFormatException(lineNumber);
            }
        } catch (InvalidDataFormatException ignored) {
        }
        // Parse and validate data
        if (data[0].startsWith("scenario:")) {
            return parseScenario(data, lineNumber); // Parse scenario
        } else if (data[0].startsWith("location:")) {
            return parseLocation(data, lineNumber); // Parse location
        } else {
            return parseCharacter(data, lineNumber);// Parse Resident
        }
    }

    /**
     * Executing this line to get Scenario data abd do the error handing
     * @return Scenario object
     */
    private Scenario parseScenario(String[] data, int lineNumber) {
        // Parse scenario
        //start from the 9th character e.g. scenario:flood,,,,,,,
        String disaster = data[0].substring(9);
        Scenario scenario = new Scenario(disaster);
        try {
            if (!Arrays.stream(DISASTER).toList().contains(disaster.toUpperCase())) {
                throw new InvalidCharacteristicException(lineNumber);
            }
        } catch (InvalidCharacteristicException e) {
            disaster = DISASTER[DISASTER.length - 1];//Default disaster
            scenario.setDisaster(disaster);
        }
        // For auditing
        if (data[1].equals("Simulation")) {
            scenario.setSimulation(true);
        }
        return scenario;
    }

    /**
     * Executing this line to get Location data abd do the error handing
     * @return Location object
     */
    private Location parseLocation(String[] data, int lineNumber) {
        // Parse location
        String[] locationInfo = data[0].split(";");//split location information e.g. location:13.7154 N;150.9094 W;trespassing,,,,,,,
        try {
            if (locationInfo.length != 3) {
                throw new InvalidDataFormatException(lineNumber);
            }
        } catch (InvalidDataFormatException ignored) {
        }
        //latitude[0,90] and longitude[0,180]
        String[] latitudeInfo = locationInfo[0].split(":");//e.g. location:13.7154 N
        String[] latitude = latitudeInfo[1].split(" ");//13.7154 N
        String[] longitude = locationInfo[1].split(" ");//150.9094 W
        double locationLatitude = 0;
        double locationLongitude = 0;

        try {
            locationLatitude = Double.parseDouble(latitude[0]);
        } catch (NumberFormatException e) {
            System.out.println("WARNING: invalid number format in line " + lineNumber);
            locationLatitude = DEFAULT_LATITUDE;
        }

        try {
            locationLongitude = Double.parseDouble(longitude[0]);
        } catch (NumberFormatException e) {
            System.out.println("WARNING: invalid number format in line " + lineNumber);
            locationLatitude = DEFAULT_LONGITUDE;
        }

        char locationLatitudeDirection = latitude[1].charAt(0);
        char locationLongitudeDirection = longitude[1].charAt(0);

        try { //Latitude direction validation
            if (!Arrays.stream(LATITUDE_DIRECTION).toList().contains(locationLatitudeDirection)) {
                throw new InvalidCharacteristicException(lineNumber);
            }
        } catch (InvalidCharacteristicException e) {
            locationLatitudeDirection = LATITUDE_DIRECTION[0];
        }
        try { //Longitude direction validation
            if (!Arrays.stream(LONGITUDE_DIRECTION).toList().contains(locationLongitudeDirection)) {
                throw new InvalidCharacteristicException(lineNumber);
            }
        } catch (InvalidCharacteristicException e) {
            locationLongitudeDirection = LONGITUDE_DIRECTION[0];
        }

        //status - trespassing or legal
        String status = locationInfo[2].split(",")[0];
        try { //Longitude direction validation
            if (status == null) {
                throw new InvalidCharacteristicException(lineNumber);
            } else if (!Arrays.stream(STATUS).toList().contains(status.toUpperCase())) {
                throw new InvalidCharacteristicException(lineNumber);
            }
        } catch (InvalidCharacteristicException e) {
            status = STATUS[0].toLowerCase();//trespassing
        }
        boolean isTrespassing = status.equals(STATUS[0].toLowerCase());//trespassing
        Location location = new Location(locationLatitude, locationLongitude, locationLatitudeDirection, locationLongitudeDirection, isTrespassing);
        //For auditing
        if (data[1].equals("true")) {
            location.setSaved(true);
        }
        return location;
    }

    /**
     * Executing this line to get Resident data abd do the error handing
     * @return Resident object
     */
    private Resident parseCharacter(String[] data, int lineNumber) {
        //Parse Resident
        String resident = data[0];

        String gender = data[1];
        try { //Gender validation
            if (!Arrays.stream(GENDER).toList().contains(gender.toUpperCase())) { //Gender validation
                throw new InvalidCharacteristicException(lineNumber);
            }
        } catch (InvalidCharacteristicException e) {
            gender = GENDER[GENDER.length - 1]; // default value
        }

        int age = 18;
        try {
            age = Integer.parseInt(data[2]);
            if (age < 0) {
                throw new InvalidCharacteristicException(lineNumber);
            }
        } catch (NumberFormatException e) {
            System.out.println("WARNING: invalid number format in line " + lineNumber);
            age = 18;
        } catch (InvalidCharacteristicException e) {
            age = 18; // default value
        }

        String bodyType = data[3];

        try { // Validate bodyType
            if (!Arrays.stream(BODY_TYPE).toList().contains(bodyType.toUpperCase())) {
                throw new InvalidCharacteristicException(lineNumber);
            }
        } catch (InvalidCharacteristicException e) {
            bodyType = BODY_TYPE[BODY_TYPE.length - 1]; // default value
        }

        // species validation
        String species = data[6];
        boolean isPet = false;
        if (resident.equalsIgnoreCase("animal")) {
            isPet = Boolean.parseBoolean(data[7]);
        }
        String profession = PROFESSION[PROFESSION.length - 1];
        boolean isPregnant = false;

        // Validate profession for human
        if (resident.equalsIgnoreCase("human")) {
            // profession validation
            try {
                profession = data[4];
                if (!Arrays.stream(PROFESSION).toList().contains(profession.toUpperCase())) {
                    throw new InvalidCharacteristicException(lineNumber);
                }
            } catch (InvalidCharacteristicException e) {
                profession = PROFESSION[PROFESSION.length - 1].toLowerCase(); //NONE
            }

            isPregnant = Boolean.parseBoolean(data[5]);
            try { // Validate profession
                if ((age < 17 || age > 68) && !profession.equalsIgnoreCase(PROFESSION[PROFESSION.length - 1])) {
                    throw new InvalidCharacteristicException(lineNumber);
                }
            } catch (InvalidCharacteristicException e) {
                profession = PROFESSION[PROFESSION.length - 1]; // default value
            }

            try { // Validate pregnant
                if (gender.equalsIgnoreCase(GENDER[0]) && isPregnant) {
                    throw new InvalidCharacteristicException(lineNumber);
                }
            } catch (InvalidCharacteristicException e) {
                isPregnant = false;// default value
            }
        }

        // Create Human or Animal Resident based on 'resident' field
        if (resident.equalsIgnoreCase("human")) {
            return new Human(gender, age, bodyType, profession, isPregnant);
        } else {
            return new Animal(gender, age, bodyType, species, isPet);
        }
    }

    public void randomScenarioGeneration() {
        RandomScenario randomScenario = new RandomScenario();
        randomScenario.randomScenarioGeneration(scenarios);
    }

    public void randomScenarioGeneration(int randomScenarioNumber) {
        RandomScenario randomScenario = new RandomScenario();
        randomScenario.randomScenarioGeneration(scenarios, randomScenarioNumber);
    }

    public void collectUserConsent(Scanner scanner) {
        System.out.println("Do you consent to have your decisions saved to a file? (yes/no)");
        boolean notConsent = true;
        while (notConsent) {
            try {
                System.out.print("> ");
                String response = scanner.nextLine().toLowerCase();
                switch (response) {
                    case "yes" -> {
                        consent = true;
                        notConsent = false;
                    }
                    case "no" -> {
                        consent = false;
                        notConsent = false;
                    }
                    default -> throw new InvalidInputException();
                }
            } catch (InvalidInputException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Let user judge which location to save. Every 3 times ask user to continue or not
     */
    public void presentScenarios(Scanner scanner) {
        int scenarioNum = 1;
        RescueLog rescueLog = new RescueLog();
        for (Scenario scenario : scenarios) {
            scenario.presentScenario();
            deployRescueBot(scanner, scenario);
            if (consent) {
                rescueLog.writeToCSV(userLogPath, scenario, false);
            }
            if (scenarioNum % 3 == 0) {
                printStatistics(scenarioNum);
                if (scenarioNum != scenarios.size()) {
                    System.out.println("Would you like to continue? (yes/no)");
                    String response = scanner.nextLine().toLowerCase();
                    while (!response.equals("no") && !response.equals("yes")) {
                        System.out.println("Invalid input. Please type 'yes' or 'no'.");
                        response = scanner.nextLine().toLowerCase();
                    }
                    if (response.equals("no")) {
                        System.out.println("> That's all. Press Enter to return to main menu.");
                        System.out.print("> ");
                        scanner.nextLine();  // Wait for the user to press Enter
                        return; // Return if user selects 'no'
                    }
                }
            }
            scenarioNum++;
        }
        printStatistics(scenarioNum);
        System.out.println("That's all. Press Enter to return to main menu.");
        System.out.print("> ");
        scanner.nextLine();  // Wait for the user to press Enter
    }

    /**
     * Deploy rescue bot to this scenario, and save one location's resident.
     * @param scenario
     */

    private void deployRescueBot(Scanner scanner, Scenario scenario) {
        // Load current scenario's residents' all attributes
        for (Location location : scenario.getLocations()) {
            for (Resident resident : location.getResidents()) {
                addResidentAttributes(resident);
            }
        }
        System.out.println("To which location should RescueBot be deployed?");
        while (true) {
            System.out.print("> ");
            int choice = -1;
            try {
                choice = scanner.nextInt() - 1;
                if (choice >= 0 && choice < scenario.getLocations().size()) {
                    scenario.getLocations().get(choice).setSaved(true); // set this location to saved
                    for (Resident resident : scenario.getLocations().get(choice).getResidents()) {
                        addSavedResidentAttributes(resident); //Load this saved location's residents' attributes
                        if (resident instanceof Human) {
                            savedHumanAge.add(resident.age);
                        }
                    }
                    break;
                } else {
                    System.out.println("Invalid response! To which location should RescueBot be deployed?");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid response! To which location should RescueBot be deployed?");
                scanner.next();
            }
        }
        scanner.nextLine();
    }

    public void runSimulation(Scenario scenario, Location savedLocation) {
        // Load current scenario's residents' all attributes
        for (Location location : scenario.getLocations()) {
            for (Resident resident : location.getResidents()) {
                addResidentAttributes(resident);
            }
        }
        for (Resident resident : savedLocation.getResidents()) {
            addSavedResidentAttributes(resident); //Load this location's residents' attributes
            if (resident instanceof Human) {
                savedHumanAge.add(resident.age);
            }
        }
    }

    /**
     * Print saving statistics into the console.
     * @param runNumber the run number
     */

    public void printStatistics(int runNumber) {
        // Create a list to hold the survival ratio of each attribute
        List<AttributeSurvivalRatio> survivalRatios = new ArrayList<>();

        // Iterate over the HashMap
        for (Map.Entry<String, int[]> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            int[] values = entry.getValue();

            // Calculate the survival ratio
            double survivalRatio = (double) values[1] / values[0];

            // Add the survival ratio to the list
            survivalRatios.add(new AttributeSurvivalRatio(attributeName, survivalRatio));
        }

        // Sort the list
        Collections.sort(survivalRatios);

        // Print the statistic
        System.out.println("======================================");
        System.out.println("# Statistic");
        System.out.println("======================================");
        System.out.println("- % SAVED AFTER " + runNumber + " RUNS");

        // Print each survival ratio
        for (AttributeSurvivalRatio ratio : survivalRatios) {
            String survivalRatio = df.format(ratio.getSurvivalRatio());
            System.out.printf("%s: %s\n", ratio.getAttributeName(), survivalRatio);
        }

        // Print the average age
        System.out.println("--");
        System.out.printf("average age: %.2f", getAvgAge(savedHumanAge));
        System.out.println();
    }

    /**
     * Print saving statistics into the console. This overload is for audit using
     * @param runNumber total number of run
     * @param isSimulation method to define the header of the statistic
     * @param attributes the attribute hashtable to store all the attributes that occurs in the scenario
     * @param savedHumanAge the list contains all saved human age
     */
    public void printStatistics(int runNumber, boolean isSimulation, HashMap<String, int[]> attributes, ArrayList<Integer> savedHumanAge) {
        // Create a list to hold the survival ratio of each attribute
        List<AttributeSurvivalRatio> survivalRatios = new ArrayList<>();

        // Iterate over the HashMap
        for (Map.Entry<String, int[]> entry : attributes.entrySet()) {
            String attributeName = entry.getKey();
            int[] values = entry.getValue();

            // Calculate the survival ratio
            double survivalRatio = (double) values[1] / values[0];

            // Add the survival ratio to the list
            survivalRatios.add(new AttributeSurvivalRatio(attributeName, survivalRatio));
        }

        // Sort the list
        Collections.sort(survivalRatios);

        // Print the statistic
        System.out.println("======================================");
        if (isSimulation) {
            System.out.println("# Algorithm Audit");
        } else {
            System.out.println("# User Audit");
        }
        System.out.println("======================================");
        System.out.println("- % SAVED AFTER " + runNumber + " RUNS");

        // Print each survival ratio
        for (AttributeSurvivalRatio ratio : survivalRatios) {
            String survivalRatio = df.format(ratio.getSurvivalRatio());
            System.out.printf("%s: %s\n", ratio.getAttributeName(), survivalRatio);
        }

        // Print the average age
        System.out.println("--");
        System.out.printf("average age: %.2f", getAvgAge(savedHumanAge));
    }

    public void audit(Scanner scanner, String userLogPath, String simulationLogFile) {
        Audit audit = new Audit(userLogPath);
        audit.audit(scanner, userLogPath, simulationLogFile);
    }

    /**
     * Add attribute into hashmap.
     * @param resident the resident's all attribute to be added
     */
    private void addResidentAttributes(Resident resident) {
        if (resident.getTrespassing()) {
            addAttribute(STATUS[0].toLowerCase());//trespassing
        } else {
            addAttribute(STATUS[1].toLowerCase());//legal
        }
        if (resident instanceof Human) {
            addAttribute(resident.getClass().getSimpleName().toLowerCase());//human class type (human or animal)
            addAttribute(((Human) resident).getAgeCategory()); //age category
            addAttribute(resident.getGender()); //gender
            addAttribute(resident.getBodyType()); //body type
            if (!((Human) resident).getProfession().equalsIgnoreCase(PROFESSION[PROFESSION.length - 1])) {
                addAttribute(((Human) resident).getProfession().toLowerCase()); //profession
            }
            if (((Human) resident).getPregnant()) {
                addAttribute(PREGNANT); //pregnancy
            }
        } else {
            addAttribute(resident.getClass().getSimpleName().toLowerCase()); //animal class type (human or animal)
            addAttribute(((Animal) resident).getSpecies()); //species
            if (((Animal) resident).getPet()) {
                addAttribute(PET); //pets
            }
        }
    }

    /**
     * Add saved attribute into hashmap.
     * @param resident the resident's all attribute to be added
     */
    private void addSavedResidentAttributes(Resident resident) {
        if (resident.getTrespassing()) { //trespassing
            addSavedAttribute(STATUS[0].toLowerCase());//trespassing
        } else {
            addSavedAttribute(STATUS[1].toLowerCase());//legal
        }
        addSavedAttribute(resident.getClass().getSimpleName().toLowerCase());//animal class type (human or animal)
        if (resident instanceof Human) {
             //human class type (human or animal)
            addSavedAttribute(((Human) resident).getAgeCategory()); //age category
            addSavedAttribute(resident.getGender()); //gender
            addSavedAttribute(resident.getBodyType()); //body type
            if (!((Human) resident).getProfession().equalsIgnoreCase(PROFESSION[PROFESSION.length - 1])) {
                addSavedAttribute(((Human) resident).getProfession().toLowerCase()); //profession
            }
            if (((Human) resident).getPregnant()) {
                addSavedAttribute(PREGNANT); //pregnancy
            }
        } else {
            addSavedAttribute(((Animal) resident).getSpecies()); //species
            if (((Animal) resident).getPet()) {
                addSavedAttribute(PET); //pets
            }
        }
    }

    private void addAttribute(String attribute) {
        if (attributes.containsKey(attribute)) {
            attributes.get(attribute)[0]++; //total number
        } else {
            int[] value = new int[]{1, 0}; //total number
            attributes.put(attribute, value);
        }
    }

    private void addSavedAttribute(String attribute) {
        attributes.get(attribute)[1]++; //saved number
    }

    private double getAvgAge(ArrayList<Integer> savedHumanAge) {
        double totalAge = 0;
        for (Integer age : savedHumanAge) {
            totalAge += age;
        }
        double age = (totalAge / savedHumanAge.size());
        return Double.parseDouble(df.format(age));
    }


    public ArrayList<Scenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(ArrayList<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public boolean isConsent() {
        return consent;
    }

    public void setConsent(boolean consent) {
        this.consent = consent;
    }

    public HashMap<String, int[]> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, int[]> attributes) {
        this.attributes = attributes;
    }

    public String getUserLogPath() {
        return userLogPath;
    }

    public void setUserLogPath(String userLogPath) {
        this.userLogPath = userLogPath;
    }

}
