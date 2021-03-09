// Copyright 2020
// Author: Matei Simtinică

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Task2
 * You have to implement 4 methods:
 * readProblemData         - read the problem input and store it however you see fit
 * formulateOracleQuestion - transform the current problem instance into a SAT instance and write the oracle input
 * decipherOracleAnswer    - transform the SAT answer back to the current problem's answer
 * writeAnswer             - write the current problem's answer
 */
public class Task2 extends Task {
    private static final int INFO_NUMBER = 3;
    private static final int FAMILIES_INDEX = 0;
    private static final int RELATIONS_INDEX = 1;
    private static final int EXTENDED_FAMILY_INDEX = 2;
    private static final int TWO_FAMILIES = 2;
    private static final int FAMILY_ONE_INDEX = 0;
    private static final int FAMILY_TWO_INDEX = 1;
    private static final String TRUE = "True";

    /**
     * numarul de familii (noduri)
     */

    private int numberOfFamilies;

    /**
     * numarul de relatii intre familii (muchii)
     */

    private int numberOfRelations;

    /**
     * numarul de familii a unei familii extinse (numarul unei clici)
     */

    private int numberOfExtendedFamily;

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
     * lista de adiacenta a grafului, unde nodurile sunt familiile, iar muchiile sunt relatiile
     */

    private List<List<Integer>> relationsFamilies;

    /**
     * raspunsul Oracolului
     */

    private String oracleAnswer;

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
     * se creeazza o lista de adiacenta pentru un graf complementar in care nodurile reprezinta
     * familiile, iar relatiile reprezinta muchiile
     */

    private void updateComplementary() {
        relationsFamiliesComplementary = new ArrayList<>();

        numberOfComplementaryRelations = numberOfFamilies * (numberOfFamilies - 1) / 2 -
                numberOfRelations;

        for (int i = 0; i <= numberOfFamilies; i++) {
            relationsFamiliesComplementary.add(new ArrayList<>());
        }

        for (int i = 1; i <= numberOfFamilies - 1; i++) {
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
     * - se citesc datele problemei adica numarul de familii, numarul relatiilor dintre ele, numarul
     * de familii dintr-o familie extinsa si relatiile dintre ele sub forma unor liste de adiacenta
     * si se stocheaza in variabilele specifice
     */


    @Override
    public void readProblemData() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(inFilename));

        String firstLine = in.readLine();
        String []infoInput = firstLine.split(" ", INFO_NUMBER + 1);

        numberOfFamilies = Integer.parseInt(infoInput[FAMILIES_INDEX]);
        numberOfRelations = Integer.parseInt(infoInput[RELATIONS_INDEX]);
        numberOfExtendedFamily = Integer.parseInt(infoInput[EXTENDED_FAMILY_INDEX]);

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

        updateComplementary();
    }

    /**
     * - se calculeaza numarul de clauze si numarul de variabile dupa formulele prezentate in
     * README si se afiseaza pe prima linie
     * - se afiseaza totalitate clauzelor dupa formatul cerut
     * - de mentionat este insemnatatea variabilelor:
     *      - fie o variabila x din intervalul [1, N*M], unde N = numarul de familii si, M = numarul
     *      de relatii, si K = numarul de familii din familia extinsa
     *      - (x - 1) / K + 1 este familia pe care o reprezinta x
     *      - (x - 1) % K + 1 reprezinta pozitia din familia extinsa(clica) pe care o are familia
     */

    @Override
    public void formulateOracleQuestion() throws IOException {
        FileWriter out = new FileWriter(oracleInFilename);

        int numberOfClauses = numberOfExtendedFamily
                + numberOfFamilies * numberOfExtendedFamily * (numberOfExtendedFamily - 1) / 2
                + numberOfExtendedFamily * numberOfFamilies * (numberOfFamilies - 1) / 2 +
                + numberOfComplementaryRelations * numberOfExtendedFamily * numberOfExtendedFamily;
        int numberOfVariables = numberOfFamilies * numberOfExtendedFamily;

        out.write("p cnf" + " " + numberOfVariables +  " " + numberOfClauses +  "\n");

        /*
         * pentru fiecare pozitie din familia extinsa(clica) se genereaza clauzele ce garanteaza
         * existenta macar unei familii pe acea pozitie
         */

        for (int i = 1; i <= numberOfExtendedFamily; i ++) {
            for (int j = 1; j <= numberOfFamilies; j++) {
                int variable = (j - 1) * numberOfExtendedFamily + i;
                out.write(variable + " ");
            }

            out.write("0\n");
        }

        /*
         * se genereaza clauzele ce garanteaza existenta unei familii pe o singura pozitie, si nu
         * pe mai multe
         */

        for (int i = 1; i <= numberOfFamilies; i++) {
            int familyVariablesStart = (i - 1) * numberOfExtendedFamily + 1;

            for (int positionI = familyVariablesStart; positionI < familyVariablesStart
                    + numberOfExtendedFamily - 1; positionI++) {
                for (int positionJ = positionI + 1; positionJ < familyVariablesStart
                        + numberOfExtendedFamily; positionJ++) {

                    out.write("-" + positionJ + " -" + positionI + " 0\n");
                }
            }

        }

        /*
         * genereaza clauzele care garanteaza ca doua familii diferite nu ocupa aceeasi pozitie
         * in familia extinsa(clica)
         */

        for (int position = 1; position <= numberOfExtendedFamily; position++) {
            for (int i = 1; i < numberOfFamilies; i++) {
                for (int j = i + 1; j <= numberOfFamilies; j++) {
                    int familyVariableI = (i - 1) * numberOfExtendedFamily + position;
                    int familyVariableJ = (j - 1) * numberOfExtendedFamily + position;
                    out.write("-" + familyVariableI + " -" + familyVariableJ + " 0\n");
                }
            }
        }

        /*
         *  genereaza clauzele care garanteaza ca doua familii care nu au o relatie nu vor exista
         * in familia extinsa(clica) pe nicio pozitie
         */


        for (int i = 1; i <= numberOfFamilies; i++) {
            List<Integer> complementaryRelationsOfFamily = relationsFamiliesComplementary.get(i);

            for (Integer relation : complementaryRelationsOfFamily) {
                    for (int position1 = 1; position1 <= numberOfExtendedFamily; position1++) {
                        int family1 = (i - 1) * numberOfExtendedFamily + position1;

                        for (int position2 = 1; position2 <= numberOfExtendedFamily; position2++) {
                            int family2 = (relation - 1) * numberOfExtendedFamily + position2;

                            out.write("-" + family1 + " -" + family2 + " 0\n");
                        }
                    }

                    //relationsFamilies.get(j).add(i);
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

        oracleAnswer = in.readLine();

        if (oracleAnswer.equals(TRUE)) {
            int numberOfVariables = Integer.parseInt(in.readLine());
            variables = in.readLine().split(" ", numberOfVariables + 1);
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
        out.write(oracleAnswer + "\n");

        if (oracleAnswer.equals(TRUE)) {
            for (String variable : variables) {
                if (!variable.contains("-") && !variable.equals("")) {
                    int family = (Integer.parseInt(variable) - 1) / numberOfExtendedFamily + 1;
                    out.write(family + " ");
                }
            }
        }

        out.flush();
    }
}
