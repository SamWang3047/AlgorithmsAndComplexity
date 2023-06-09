package COMP90041.Project_DisasterReliefRobots;

import java.util.ArrayList;
import java.util.Random;

public class RandomScenario {
    /**
     * This class is for random generate different scenario
     *
     */
    private static final String[] AUTO_GENERATED_BODY_TYPE = {"OVERWEIGHT", "AVERAGE", "ATHLETIC"};
    private static final String[] AUTO_GENERATED_GENDER = {"MALE", "FEMALE"};
    private static final String[] AUTO_GENERATED_SPECIES = {"PUPPY", "DINGO", "CAT", "KOALA", "WALLABY", "SNAKE", "LION", "DOG", "PLATYPUS"};
    private static final String[] AUTO_GENERATED_PROFESSION = {"DOCTOR", "CEO", "CRIMINAL", "HOMELESS", "UNEMPLOYED", "ATHLETIC", "STUDENT", "PROFESSOR", "NONE"};
    private static final String[] AUTO_GENERATED_DISASTER = {"CYCLONE", "FLOOD", "EARTHQUAKE", "BUSHFIRE", "METEORITE"};
    private static final Character[] LATITUDE_DIRECTION = {'N', 'S'};
    private static final Character[] LONGITUDE_DIRECTION = {'E', 'W'};

    public void randomScenarioGeneration(ArrayList<Scenario> scenarios) {
        Random r = new Random();
        int scenarioNum = r.nextInt(7) + 3; // Number of scenarios [3, 10]
        randomScenario(scenarios, r, scenarioNum);
    }
    public void randomScenarioGeneration(ArrayList<Scenario> scenarios, int randomScenarioNumber) {
        Random r = new Random();
        randomScenario(scenarios, r, randomScenarioNumber);
    }

    private void randomScenario(ArrayList<Scenario> scenarios, Random r, int scenarioNum) {
        String disaster;
        for (int i = 0; i < scenarioNum; i++) {
            int locationNum = r.nextInt(4) + 2; // Number of locations [2, 6]
            ArrayList<Location> locations = new ArrayList<>();
            for (int j = 0; j < locationNum; j++) {
                locations.add(randomLocation());
            }
            disaster = AUTO_GENERATED_DISASTER[r.nextInt(AUTO_GENERATED_DISASTER.length)].toLowerCase();
            scenarios.add(new Scenario(disaster, locations));
        }
    }

    private Location randomLocation() {
        Random r = new Random();
        double lat = r.nextDouble() * 180 - 90;  // latitude [-90, 90]
        char latDirect = LATITUDE_DIRECTION[0];
        if (lat < 0) latDirect = LATITUDE_DIRECTION[1];
        double lon = r.nextDouble() * 360 - 180;  // longitude [-180, 180]
        char lonDirect = LONGITUDE_DIRECTION[0];
        if (lon < 0) lonDirect = LONGITUDE_DIRECTION[1];
        boolean isTrespassing = r.nextBoolean();

        int numCharacters = r.nextInt(7) + 1;  // Number of characters [1, 8]
        ArrayList<Resident> residents = new ArrayList<>();
        for (int i = 0; i < numCharacters; i++) {
            residents.add(randomCharacter());
        }

        return new Location(Math.abs(lat), Math.abs(lon), latDirect, lonDirect, isTrespassing, residents);
    }


    private Resident randomCharacter() {
        Random r = new Random();
        if (r.nextBoolean()) {
            // Generate a human
            int age = r.nextInt(100);

            String gender = AUTO_GENERATED_GENDER[r.nextInt(AUTO_GENERATED_GENDER.length)].toLowerCase();
            String bodyType = AUTO_GENERATED_BODY_TYPE[r.nextInt(AUTO_GENERATED_BODY_TYPE.length)].toLowerCase();
            String profession = AUTO_GENERATED_PROFESSION[AUTO_GENERATED_PROFESSION.length - 1];
            if (age >= 17 && age <= 68) {
                profession = AUTO_GENERATED_PROFESSION[r.nextInt(AUTO_GENERATED_PROFESSION.length)].toLowerCase();
            }
            boolean isPregnant = false;
            //Only female can get pregnant
            if (gender.equals(AUTO_GENERATED_GENDER[1])) {
                isPregnant = r.nextBoolean();
            }
            return new Human(gender, age, bodyType, profession, isPregnant);
        } else {
            // Generate an animal
            int age = r.nextInt(15);
            String gender = AUTO_GENERATED_GENDER[r.nextInt(AUTO_GENERATED_GENDER.length)].toLowerCase();
            String bodyType = AUTO_GENERATED_BODY_TYPE[r.nextInt(AUTO_GENERATED_BODY_TYPE.length)].toLowerCase();
            String species = AUTO_GENERATED_SPECIES[r.nextInt(AUTO_GENERATED_SPECIES.length)].toLowerCase();
            boolean isPet = r.nextBoolean();
            return new Animal(gender, age, bodyType, species, isPet);
        }
    }

}
