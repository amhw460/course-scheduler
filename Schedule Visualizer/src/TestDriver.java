import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TestDriver {
    public static void main(String[] args) throws Exception {
        System.out.println("HI");
        StringBuffer total = new StringBuffer();
        List<String> strList = Files.readAllLines(Paths.get(FileHunter.fileHunt("output.json").getAbsolutePath()));
        
        for (String str : strList) {
            total.append(str + "\n");
        }

        JSONReader ready = new JSONReader(total.toString());
        
        Object obj1 = ready.parse();

        // System.out.println(obj1);
        Object firstClass = ((ArrayList)obj1).get(0);

        System.out.println(firstClass);
        for (Object o : ((HashMap)firstClass).values()) {
            // System.out.println(o);
            // System.out.println(o.getClass());
            // System.out.println();
            // System.out.println(((ArrayList)o).get(0));
            // System.out.println(((ArrayList)o).get(0).getClass());
        }
    }

    // Custom Class made by Farhan for all DSA Labs involving user input
    public static class InputTools {
        /**
         * Inputs a boolean from the provided scanner, doing safe checks until the user
         * answers properly
         * 
         * @param s      The scanner to be used
         * @param prompt The prompt to be given to the user
         * @return the boolean (true/false) value provided by the user
         */
        public static boolean inputBoolean(Scanner s, String prompt) {
            String answer;
            System.out.print(prompt);
            answer = s.nextLine().toLowerCase();
            while (!answer.equals("yes") && !answer.equals("no")) {
                System.out.println("You must respond yes or no. No other values accepted.");
                System.out.print(prompt);
                answer = s.nextLine().toLowerCase();
            }
            return (answer == "yes");
        }

        /**
         * Inputs an String from the provided scanner
         * 
         * @param s      The scanner to be used
         * @param prompt The prompt to be given to the user
         * @return the String provided by the user
         */
        public static String inputString(Scanner s, String prompt) {
            String answer;
            System.out.print(prompt);
            answer = s.nextLine();
            return answer;
        }

        /**
         * Inputs an integer from the provided scanner, doing safe checks until the user
         * answers properly
         * 
         * @param s      The scanner to be used
         * @param prompt The prompt to be given to the user
         * @return the integer value provided by the user
         */
        public static int inputInt(Scanner s, String prompt) {
            String answer;
            System.out.print(prompt);
            answer = s.nextLine();
            while (!isInteger(answer)) {
                System.out.println("You must respond with an integer. No other values accepted.");
                System.out.print(prompt);
                answer = s.nextLine();
            }
            return Integer.valueOf(answer);
        }

        /**
         * Inputs an integer from the provided scanner, doing safe checks until the user
         * answers properly
         * 
         * @param s      The scanner to be used
         * @param prompt The prompt to be given to the user
         * @param min    Minimum value to return (inclusive)
         * @param max    Maximum value to return (incluisve)
         * @return the integer value provided by the user
         * 
         */
        public static int inputIntInRange(Scanner s, String prompt, int min, int max) {
            String answer;
            System.out.print(prompt);
            answer = s.nextLine();
            while (!isInteger(answer) || Integer.valueOf(answer) < min || Integer.valueOf(answer) > max) {
                if (!isInteger(answer)) {
                    System.out.println("You must respond with an integer. No other values accepted.");
                } else if (Integer.valueOf(answer) < min) {
                    System.out.println("Your answer is too low. At minimum, input an integer greater than " + min);
                } else if (Integer.valueOf(answer) > max) {
                    System.out.println("Your answer is too high. At maximum, input an integer lesser than " + max);
                }

                System.out.print(prompt);
                answer = s.nextLine();
            }
            return Integer.valueOf(answer);
        }

        /**
         * Inputs a double from the provided scanner, doing safe checks until the user
         * answers properly
         * 
         * @param s      The scanner to be used
         * @param prompt The prompt to be given to the user
         * @return the double value provided by the user
         */
        public static double inputDouble(Scanner s, String prompt) {
            String answer;
            System.out.print(prompt);
            answer = s.nextLine().toLowerCase();

            while (!isDouble(answer)) {
                System.out.println(
                        "You must respond with a double (number with decimal points). No other values accepted.");
                System.out.print(prompt);
                answer = s.nextLine().toLowerCase();
            }
            return Double.valueOf(answer);
        }

        /**
         * Inputs a string from the provided scanner, ensuring it matches one of the
         * options provided
         * 
         * @param s      The scanner to be used
         * @param prompt The prompt to be given to the user
         * @return the option selected by the user (set to lowercase)
         */
        public static String inputChoice(Scanner s, String prompt, String[] options) {
            String answer;
            System.out.print(prompt);
            answer = s.nextLine();
            while (getOption(answer, options) == -1) {
                System.out.println("Your response was not one of the options.");
                System.out.println(
                        "Please make sure your answer is one of the following: " + Arrays.toString(options) + "\n");

                System.out.print(prompt);
                answer = s.nextLine();
            }
            return answer.toLowerCase();
        }

        public static int getOption(String o, String[] options) {
            int i = 0;
            o = o.toLowerCase();
            for (String s : options) {
                if (s.toLowerCase().equals(o))
                    return i;
                i++;
            }
            return -1;
        }

        /**
         * Safely checks if the string provided is a double
         * 
         * @param s The string to be checked
         * @return True if it is a double, false if it is not
         */
        public static boolean isDouble(String s) {
            try {
                Double.valueOf(s);
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        /**
         * Safely checks if the string provided is an integer
         * 
         * @param s The string to be checked
         * @return True if it is an integer, false if it is not
         */
        public static boolean isInteger(String s) {
            try {
                Integer.valueOf(s);
            } catch (Exception e) {
                return false;
            }
            return true;

        }
    }

    public static class FileHunter {
        /**
         * Inputs a file from the user via the name provided in the scanner safely
         * 
         * @param s      The scanner to be used to give the filename
         * @param prompt The prompt to be given to the user
         * @return the File object corresponding to the user's desired file
         */
        public static File inputFile(Scanner s, String prompt) {
            String answer;
            System.out.print(prompt);
            answer = s.nextLine();
            File result = null;
            while (true) {
                result = fileHunt(answer);
                if (result != null)
                    break;
                System.out.println("You must respond with a real file name.");
                System.out.print(prompt);
                answer = s.nextLine();
            }
            return result;
        }

        // Start of recursive file hunter
        // Made to find files no matter where they are in project directory
        public static File fileHunt(String filename) {
            // System.out.println();
            // System.out.println("UP");
            // System.out.println(dir.getAbsolutePath());

            File dir;
            if (filename.charAt(0) == '"')
                filename = filename.substring(1);
            if (filename.charAt(filename.length() - 1) == '"')
                filename = filename.substring(0, filename.length() - 1);

            // System.out.println(filename);
            if (!filename.contains("C:\\"))
                dir = new File(new File("").getAbsolutePath() + "\\");
            else {
                dir = new File(filename);
                if (dir.exists())
                    return dir;
                else
                    return null;
            }
            return recurFileHunt(dir.getAbsolutePath(), filename);
        }

        // Recursively goes through folders to find the file
        public static File recurFileHunt(String path, String filename) {
            // Get the list of files in the directory
            File[] files = new File(path).listFiles();

            // Check if it's there
            for (File f : files) {
                // Return if found
                if (!f.isDirectory() && f.getName().equals(filename))
                    return f;
            }

            // Go through the directories, recursively search them
            for (File f : files) {
                // Only go through a directory if actually a directory
                if (f.isDirectory()) {
                    // Hunt recursively
                    File result = recurFileHunt(f.getAbsolutePath() + "\\", filename);
                    if (result != null)
                        return result;// The only way it's not null is if we found it!
                }
            }
            return null;
        }
    }

}
