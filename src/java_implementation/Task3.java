// Copyright 2020
// Author: Matei Simtinică

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Task3
 * This being an optimization problem, the solve method's logic has to work differently.
 * You have to search for the minimum number of arrests by successively querying the oracle.
 * Hint: it might be easier to reduce the current task to a previously solved task
 */
public class Task3 extends Task {
    private static final int FAMILIES_INDEX = 0;
    private static final int RELATIONS_INDEX = 1;
    private static final int INFO_NUMBER = 2;
    private static final int TWO_FAMILIES = 2;
    private static final int FAMILY_ONE_INDEX = 0;
    private static final int FAMILY_TWO_INDEX = 1;
    private static final String TRUE = "True";

    String task2InFilename;
    String task2OutFilename;

    /**
     * numarul de relatii
     */

    private int numberOfRelations;

    /**
     * numarul de familii
     */

    private int numberOfFamilies;

    /**
     * lista de adiacenta pentru relatiile dintre familii
     */

    private List<List<Integer>> relationsFamilies;

    /**
     * lista de adiacenta a grafului complementar in care nodurile sunt familiile, iar muchiile
     * reprezinta relatiile dintre ele
     */

    private List<List<Integer>> relationsFamiliesComplementary;

    /**
     * numarul de relatii(muchii) complementare
     */

    private int numberOfComplementaryRelations;

    /**
     * numarul maxim de familii dintr-o familie extinsa (clica)
     */
    private int maximumExtendedFamily;

    /**
     * lista minimal cu familiile ce trebuiesc arestate
     */
    private List<Integer> minimumFamiliesArrested;


    @Override
    public void solve() throws IOException, InterruptedException {
        task2InFilename = inFilename + "_t2";
        task2OutFilename = outFilename + "_t2";
        Task2 task2Solver = new Task2();
        task2Solver.addFiles(task2InFilename, oracleInFilename, oracleOutFilename, task2OutFilename);
        readProblemData();

        updateComplementary();
        numberOfComplementaryRelations = numberOfFamilies * (numberOfFamilies - 1) / 2
                - numberOfRelations;
        maximumExtendedFamily = calculateMaximumExtendedFamily();

        boolean answer = false;

        while (!answer) {
            reduceToTask2();
            task2Solver.solve();
            answer = extractAnswerFromTask2();
        }

        writeAnswer();
    }

    /**
     * se creeazza o lista de adiacenta pentru un graf complementar in care nodurile reprezinta
     * familiile, iar relatiile reprezinta muchiile
     */

    private void updateComplementary() {
        relationsFamiliesComplementary = new ArrayList<>();

        for (int i = 0; i <= numberOfFamilies; i++) {
            relationsFamiliesComplementary.add(new ArrayList<>());
        }

        for (int i = 1; i <= numberOfFamilies; i++) {
            List<Integer> familyRelations = relationsFamilies.get(i);
            List<Integer> familyRelationsComplementary = relationsFamiliesComplementary.get(i);
            for (int family = i + 1; family <= numberOfFamilies; family++) {

                if (!familyRelations.contains(family)) {
                    familyRelationsComplementary.add(family);
                }
            }
        }
    }

    /**
     * - se citesc datele problemei adica numarul de familii, numarul relatiilor dintre ei si
     * relatiile dintre ele sub forma unor liste de adiacenta si se stocheaza in variabilele
     * specifice
     */

    @Override
    public void readProblemData() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(inFilename));

        String firstLine = in.readLine();
        String []infoInput = firstLine.split(" ", INFO_NUMBER);

        numberOfFamilies = Integer.parseInt(infoInput[FAMILIES_INDEX]);
        numberOfRelations = Integer.parseInt(infoInput[RELATIONS_INDEX]);

        relationsFamilies = new ArrayList<>(numberOfFamilies + 1);

        for (int i = 0; i <= numberOfFamilies; i++) {
            relationsFamilies.add(new ArrayList<>());
        }

        for (int i = 0; i < numberOfRelations; i++) {
            String line = in.readLine();
            String[] infoRelations = line.split(" ", TWO_FAMILIES);

            int family1 = Integer.parseInt(infoRelations[FAMILY_ONE_INDEX]);
            int family2 = Integer.parseInt(infoRelations[FAMILY_TWO_INDEX]);

            relationsFamilies.get(family1).add(family2);
            relationsFamilies.get(family2).add(family1);
        }
    }

    /**
     * - se afiseaza in fisierul task2InFile in datele pentru gasirea unei familii extinse dupa
     * formatul inpututlui pentru task2 adica
     *      - pe prima linie N, M, K
     *          -N = numarul de familii
     *          -M = numarul de relatii
     *          -K = numarul familiei extinse cautate
     *       - pe urmatoarele M linii se afiseaza relatia dintre doua familii daca aceasta exista
     *  - de mentionat este ca relatiile date oracolului sunt reprezinte de muchiile unui
     *  graf complementar.
     */

    public void reduceToTask2() throws IOException {
        FileWriter out = new FileWriter(task2InFilename);

        out.write(numberOfFamilies + " " + numberOfComplementaryRelations
                + " " + maximumExtendedFamily + "\n");

        for (int i = 1; i <= numberOfFamilies; i++) {
            List<Integer> familyRelations = relationsFamiliesComplementary.get(i);
            for (Integer relation : familyRelations) {
                out.write(i + " " + relation + "\n");
            }
        }

        out.flush();
    }

    /**
     * - se citeste raspunsul oracolului din fisierul task2OutFilename
     * - daca prima linie este reprezentata de stringul "True", atunci s-a gasit o familie extinsa
     * de marime maximumExtendedFamily si se stocheaza in complementaryArrestedFamilies care
     * reprezinta multimea complementara de familii ce trebuiesc arestate. In
     * minimumFamiliesArrested se stocheaza familiile ce trebuiesc arestate prin raportare la
     * complementaryArrestedFamilies.
     * - daca raspunsul oracolului este "False" atunci nu s-a gasit o familie extinsa de marime
     * maximumExtendedFamily si se decrementeaza aceasta marime
     * @return true daca raspunul oracolului e "True" sau false daca raspunsul oracolului este
     * "False"
     */

    public boolean extractAnswerFromTask2() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(task2OutFilename));

        String firstLine = in.readLine();

        if (firstLine.equals(TRUE)) {
            String secondLine = in.readLine();
            String[] families = secondLine.split(" ", maximumExtendedFamily + 1);

            List<Integer> complementaryArrestedFamilies = new ArrayList<>();
            minimumFamiliesArrested = new ArrayList<>(numberOfFamilies
                   - maximumExtendedFamily);

            for (int i = 0; i < maximumExtendedFamily; i++) {
                complementaryArrestedFamilies.add(Integer.parseInt(families[i]));
            }

            for (int i = 1; i <= numberOfFamilies; i++) {
                if (!complementaryArrestedFamilies.contains(i)) {
                    minimumFamiliesArrested.add(i);
                }
            }

            return true;
        } else {
            maximumExtendedFamily--;
            return false;
        }
    }

    /**
     * - se afiseaza in fisier toate familiile care trebuie sa fie arestate stocate in
     * minimumFamiliesArrested
     */

    @Override
    public void writeAnswer() throws IOException {
        FileWriter out = new FileWriter(outFilename);

        for (Integer family : minimumFamiliesArrested) {
            out.write(family + " ");
        }

        out.flush();
    }

    /**
     * - calculeaza numarul maxim al unei familii extinse (clica) ce trebuie trimis oracolului
     * (adica cel in functie de relatiile complementare)
     * - o intr-o familie extinsa (clica) exista N(N-1)/2 relatii (muchii), unde N = nr de familii
     * din clica
     * - pentru a obtine dimensiunea maxima a unei familii extinse (clica) dintr-un graf calculam
     * N din relatia N(N-1)/2 = M, unde M = numarul de relatii existente (nr de muchii din graf)
     * - astfel obtinem N^2 - N - 2 * M = 0
     * - in aceasta metoda se afla solutia mai mare a acestei ecuatii de gradul 2 care reprezinta
     * de fapt numarul maxim al unei familii extinse(al clicii) care poate exista in graf
     */

    private int calculateMaximumExtendedFamily() {
        double a = 1.0;
        double b = -1.0;
        double c = -2.0 * numberOfComplementaryRelations;

        double delta = b * b - 4 * a * c;

        double maxSol = (-b + Math.sqrt(delta)) / (2 * a);

        return ((int) Math.round(maxSol));
    }
}
