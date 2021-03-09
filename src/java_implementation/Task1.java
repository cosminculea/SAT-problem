// Copyright 2020
// Author: Matei Simtinică

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Task1
 * You have to implement 4 methods:
 * readProblemData         - read the problem input and store it however you see fit
 * formulateOracleQuestion - transform the current problem instance into a SAT instance and write the oracle input
 * decipherOracleAnswer    - transform the SAT answer back to the current problem's answer
 * writeAnswer             - write the current problem's answer
 */
public class Task1 extends Task {
    private static final int INFO_NUMBER = 3;
    private static final int FAMILIES_INDEX = 0;
    private static final int RELATIONS_INDEX = 1;
    private static final int SPIES_INDEX = 2;
    private static final int TWO_FAMILIES = 2;
    private static final int FAMILY_ONE_INDEX = 0;
    private static final int FAMILY_TWO_INDEX = 1;
    private static final String TRUE = "True";

    /**
     * numarul de familii
     */

    private int numberOfFamilies;

    /**
     * numarul de relatii dintre familii
     */

    private int numberOfRelations;

    /**
     * numarul de spioni disponibili
     */

    private int numberOfSpies;

    /**
     * lista de adiancenta a relatiilor dintre familie (dar sub forma de graf orientat in sensul
     * in care se va pastra o singura directie a relatiei, indiferent care este aceea)
     */

    private List<List<Integer>> relationsFamilies;

    /**
     * raspunsul oracolului
     */

    private String answer;

    /**
     * lista de variabile sub forma de stringuri
     */

    private String[] variables;

    @Override
    public void solve() throws IOException, InterruptedException {
        readProblemData();
        formulateOracleQuestion();
        askOracle();
        decipherOracleAnswer();
        writeAnswer();
    }

    /**
     * - se citesc datele problemei adica numarul de familii, numarul relatiilor dintre ele, numarul
     * de spioni si relatiile dintre ele sub forma unor liste de adiacenta si se stocheaza in
     * variabilele specifice
     */

    @Override
    public void readProblemData() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(inFilename));

        String firstLine = in.readLine();
        String []infoInput = firstLine.split(" ", INFO_NUMBER);

        numberOfFamilies = Integer.parseInt(infoInput[FAMILIES_INDEX]);
        numberOfRelations = Integer.parseInt(infoInput[RELATIONS_INDEX]);
        numberOfSpies = Integer.parseInt(infoInput[SPIES_INDEX]);

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
        }
    }

    /**
     * - se calculeaza numarul de clauze si numarul de variabile dupa formulele prezentate in
     * README si se afiseaza pe prima linie
     * - se afiseaza totalitate clauzelor dupa formatul cerut
     * - de mentionat este insemnatatea variabilelor:
     *      - fie o variabila x din intervalul [1, N * K], unde N = numarul de familii si,
     *      K = numarul de spioni
     *      - (x - 1) / K + 1 este familia pe care o reprezinta x
     *      - (x - 1) % K + 1 reprezinta spionul din familie
     */

    @Override
    public void formulateOracleQuestion() throws IOException {
        FileWriter out = new FileWriter(oracleInFilename);

        int numberOfClauses = numberOfFamilies
                + numberOfFamilies * numberOfSpies * (numberOfSpies - 1) / 2
                + numberOfRelations * numberOfSpies;
        int numberOfVariables = numberOfFamilies * numberOfSpies;

        out.write("p cnf" + " " + numberOfVariables +  " " + numberOfClauses +  "\n");


        for (int family = 1; family <= numberOfFamilies; family++) {
            int familySpiesVariable = (family - 1) * numberOfSpies + 1;

            /*
             * se afiseaza clauzele care garanteaza faptul ca fiecare familie va avea cel putin un
             * spion
             */

            for (int spy = familySpiesVariable; spy < familySpiesVariable + numberOfSpies; spy++) {
                out.write(spy + " ");
            }

            out.write("0\n");

            /*
             * se afiseaza clauzele care garanteaza ca o familie are un singur spion
             */

            for (int spy1 = familySpiesVariable; spy1 < familySpiesVariable + numberOfSpies - 1;
                 spy1++) {
                for (int spy2 = spy1 + 1; spy2 < familySpiesVariable + numberOfSpies; spy2++) {

                    out.write("-" + spy1 + " -" + spy2 + " 0\n");
                }
            }
        }

        /*
         *  genereaza clauzele care garanteaza ca doua familii care au o relatie nu vor avea acelasi
         * spion
         */

        for (int i = 1; i <= numberOfFamilies; i++) {
            List<Integer> relationsOfFamily = relationsFamilies.get(i);
            int familySpyVariable = (i - 1) * numberOfSpies + 1;

            for (Integer relation : relationsOfFamily) {
                int counterpartSpyVariable = (relation - 1) * numberOfSpies + 1;

                for (int spy = 0; spy < numberOfSpies; spy++) {
                    out.write("-" + (familySpyVariable + spy)
                            + " -" + (counterpartSpyVariable + spy)
                            + " 0\n");
                }
            }
        }
        out.flush();
    }

    /**
     * citeste raspunsul oracolului, si il stocheaza impreuna cu toate variabilele in lista de
     * Stringuri variables
     */

    @Override
    public void decipherOracleAnswer() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(oracleOutFilename));

        String oracleAnswer = in.readLine();
        answer = oracleAnswer;

        if (oracleAnswer.equals(TRUE)) {
            int numberOfVariables = Integer.parseInt(in.readLine());
            variables = in.readLine().split(" ", numberOfVariables);
        }
    }

    /**
     * - scrie in fisierul outFilename pe prima linie raspunsul oracolului adica "True" sau "False",
     * iar pe urmatoare linie familiile care face parte din familia extinsa(clica), dupa
     * semnificatia prezentata; familia este reprezentata de (x - 1) / K + 1, unde x = variabila,
     * K = numarul de familie dintr-o familie extinsa
     */

    @Override
    public void writeAnswer() throws IOException {
        FileWriter out = new FileWriter(outFilename);
        out.write(answer + "\n");

        if (answer.equals(TRUE)) {
            for (String variable : variables) {
                if (!variable.contains("-")) {
                    int spy = Integer.parseInt(variable) % numberOfSpies + 1;
                    out.write(spy + " ");
                }
            }
        }

        out.flush();
    }
}
