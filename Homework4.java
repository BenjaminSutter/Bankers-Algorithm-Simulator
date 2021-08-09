/*
 * File: Homework4.java
 * Author: Ben Sutter
 * Date: April 9th, 2021
 * Purpose: Implements the Banker's Algorithm for deadlock avoidance
 */
package homework4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

//FOR ALL SOLUTIONS
//https://www.geeksforgeeks.org/bankers-algorithm-in-operating-system/
public class Homework4 {

    int numberOfProcesses;
    int numberOfResources;
    int totalSafeSequences = 0;//Keeps track of how many sequences were created

    int[][] max; //2D array of max resources per process
    int[][] allocated; //2D array of resources allocated to each process
    int[] availability; //Contains initial avaailability and is updated with new availability
    int need[][]; //2D array of the need for each process (max - allocated)
    boolean marked[];//Used to keep track of what processes have already been visited
    //Used to determine safe sequences
    static Vector<Integer> safeSequences = new Vector<Integer>();

    //Method found from https://stackoverflow.com/a/35765098
    //When given a string with integers seperated by commas it will prase them into an array.
    public int[] stringToIntegerArray(String line) {
        return Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
    }

    //Retrives the file based on user input, and then creates the arrays based on the text file retrieved.
    public boolean requestFileAndInitializeArrays() {

        //Method from https://www.java67.com/2016/07/how-to-read-text-file-into-arraylist-in-java.html
        //Converts a file into an array where each string is an element
        List<String> lines = Collections.emptyList();
        try {
            System.out.println("Please type the name of the file to parse:");
            Scanner scan = new Scanner(System.in);
            String userInput = scan.nextLine();
            lines = Files.readAllLines(Paths.get(userInput), StandardCharsets.UTF_8);

            /*Intialize these variables based off of file
            First line is for proccesses, second is for resources*/
            numberOfProcesses = Integer.parseInt(lines.get(0));
            numberOfResources = Integer.parseInt(lines.get(1));
            //Initialize arrays based on number of processes and resources
            max = new int[numberOfProcesses][numberOfResources];
            allocated = new int[numberOfProcesses][numberOfResources];
            need = new int[numberOfProcesses][numberOfResources];
            marked = new boolean[numberOfProcesses];

            /*Initialize max array from file input.
            The lines after the first 2 are for max and allocated*/
            for (int i = 0; i < numberOfProcesses; i++) {
                max[i] = stringToIntegerArray(lines.get(i + 2));//Starting after line 2, parse for max
                //Starting after line 2 + processes, parse for allocated
                allocated[i] = stringToIntegerArray(lines.get(numberOfProcesses + 2 + i));
            }

            //The last line contains the availabilty array
            availability = stringToIntegerArray(lines.get(lines.size() - 1));
            return true;//Parse was sucessful so return true
        } catch (NoSuchFileException e) {
            // TODO Auto-generated catch block
            System.out.println("Invalid file, please try again.\n");
        } catch (Exception e) {
            System.out.println("File is in a format that is not parseable, please choose a valid file.\n");
        }
        return false;//Parse failed
    }
    //Create the need array bnased on the max and allocated

    public void createNeedArray() {
        for (int i = 0; i < allocated.length; i++) {
            for (int j = 0; j < allocated[i].length; j++) {
                need[i][j] = max[i][j] - allocated[i][j];
            }
        }
    }

    //Check the current process (passed as parameter) to see if the need is greater than current available
    public boolean checkAvailability(int currentProcess) {
        //Iterate through all resources to make sure they aren't greater
        for (int i = 0; i < allocated[i].length; i++) {
            if (need[currentProcess][i] > availability[i]) {
                return false;//If even one resource is greater, than false
            }
        }
        return true;//Otherwise, true
    }

    //Most of the code for this method was inspired by: https://www.geeksforgeeks.org/bankers-algorithm-in-operating-system/
    public void findSafeSequences() {
        //Loops as many times as there are processes.
        for (int i = 0; i < allocated.length; i++) {
            //If the current process is not marked, mark it and see if it will lead to a safe sequences
            if (!marked[i] && checkAvailability(i)) {
                //System.out.println("WE IN");
                marked[i] = true;
                //Iterate through the length of availabilty and update it
                for (int j = 0; j < availability.length; j++) {
                    availability[j] += allocated[i][j];
                }

                safeSequences.add(i);//Add sequence to safe list.
                findSafeSequences();//Recursive call
                safeSequences.removeElementAt(safeSequences.size() - 1);//Resize vector
                marked[i] = false;//Unmark it for the next recursive call

                // decrease the available
                for (int j = 0; j < numberOfResources; j++) {
                    availability[j] -= allocated[i][j];
                }

            }
        }

        //If the added sequences makes the vector as big as the process list, print the safe sequence
        if (safeSequences.size() == allocated.length) {
            String results = "Found safe sequence: ";
            //Build the string of safe sequence
            for (int j = 0; j < safeSequences.size(); j++) {
                results += "P" + (safeSequences.get(j) + 1);
                if (j < safeSequences.size() - 1) {
                    results += " > ";
                }
            }
            System.out.println(results);
            totalSafeSequences++;//Increment to keep track of total safe sequences
        }
    }

    public void displayNumberOfSequences() {
        System.out.println("Attempting to locate safe sequences: \n");
        findSafeSequences();//Call recursive method to print safe sequences
        if (totalSafeSequences == 1) {
            //If there are safe sequences, display how many
            System.out.println("\nThere is only 1 safe sequence\n");
        } else if (totalSafeSequences > 1) {
            //If there are safe sequences, display how many
            System.out.println("\nThere are " + totalSafeSequences + " safe sequences\n");
        } else {
            //If there are no safe sequences, notify the user
            System.out.println("\nDeadlock occured, no safe sequence");
        }
    }

    public static void main(String[] args) {
        Homework4 test = new Homework4();
        //Loop method until it becomes true (a valid parseable file was entered
        while (!test.requestFileAndInitializeArrays()) {}
        //After a valid file has been parsed, perform actions
        test.createNeedArray();
        test.displayNumberOfSequences();
    }

}
