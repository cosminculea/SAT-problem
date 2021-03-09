// Copyright 2020
// Author: Matei Simtinică

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bonus Task
 * You have to implement 4 methods:
 * readProblemData         - read the problem input and store it however you see fit
 * formulateOracleQuestion - transform the current problem instance into a SAT instance and write the oracle input
 * decipherOracleAnswer    - transform the SAT answer back to the current problem's answer
 * writeAnswer             - write the current problem's answer
 */
public class BonusTask extends Task {
    private static final int FAMILIES_INDEX = 0;
    private static final int RELATIONS_INDEX = 1;
    private static final int INFO_NUMBER = 2;
    private static final int TWO_FAMILIES = 2;
    private static final int FAMILY_ONE_INDEX = 0;
    private static final int FAMILY_TWO_INDEX = 1;

    private static class Family {
        private final List<Integer> relations;
        private int factor;

        public Family(List<Integer> relations) {
            this.relations = relations;
        }

        public void setFactor(int factor) {
            this.factor = factor;
        }

        public int getFactor() {
            return factor;
        }

        public List<Integer> getRelations() {
            return relations;
        }
    }


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
     * numarul maxim de familii dintr-o familie extinsa (clica)
     */
    private int numberOfExtendedFamily;

    /**
     * lista minimal cu familiile ce trebuiesc arestate
     */
    private List<Integer> minimumFamiliesArrested;

    /**
     * lista de adiacenta a grafului complementar in care nodurile sunt familiile, iar muchiile
     * reprezinta relatiile dintre ele
     */

    private List<List<Integer>> fullComplementaryRelations;

    /**
     * numarul de variabile date oracolului
     */

    private int numberOfVariables;

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
        fullComplementaryRelations = new ArrayList<>();

        for (int i = 0; i <= numberOfFamilies; i++) {
            fullComplementaryRelations.add(new ArrayList<>());
        }

        for (int i = 1; i <= numberOfFamilies; i++) {
            List<Integer> familyRelations = relationsFamilies.get(i);

            for (int family = 1; family <= numberOfFamilies; family++) {

                if (!familyRelations.contains(family) && (family != i)){
                    fullComplementaryRelations.get(i).add(family);
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

        updateComplementary();
    }

    /**
     * Se calculeaza numarul maxim de familii dintr-o familie extinsa posibila cu probabilitate mare
     * ,se calculeaza numarul de variabile si de clauze si se afiseaza toate datele necesare,
     * pe acelasi principiu ca la task2. Spre deosebire de taskul 2, primul set de clause afisat
     * are cate un weight specific, si anume pozitia pentru care se construieste clauza, pe cand
     * restul au maximum weight, intrucat sunt complet necesare si nu influenteaza decizia
     * oracolului daca vor exista familii extinse cu mai putin de numberOfExtendedFamily familii.
     *  Weight-ul maxim este suma tuturor celorlalte weights adica 1+1+2+3+..+numberOfExtendedFamily
     * adica 1+numberOfExtendedFamily(numberOfExtendedFamily+1)/2.
     */

    @Override
    public void formulateOracleQuestion() throws IOException {
        FileWriter out = new FileWriter(oracleInFilename);

        numberOfExtendedFamily = calculateMaximumExtendedFamily();

        int numberOfClauses = numberOfExtendedFamily
                + numberOfFamilies * numberOfExtendedFamily * (numberOfExtendedFamily - 1) / 2
                + numberOfExtendedFamily * numberOfFamilies * (numberOfFamilies - 1) / 2
                + numberOfRelations * numberOfExtendedFamily * numberOfExtendedFamily;

        numberOfVariables = numberOfFamilies * numberOfExtendedFamily;

        int maximumWeight = numberOfExtendedFamily * ( numberOfExtendedFamily + 1) / 2 + 1;

        out.write("p wcnf" + " " + numberOfVariables +  " " + numberOfClauses +  " " +
                maximumWeight + "\n");

        /*
         * pentru fiecare pozitie din familia extinsa(clica) se genereaza clauzele ce garanteaza
         * existenta macar unei familii pe acea pozitie
         */

        for (int position = 1; position <= numberOfExtendedFamily; position ++) {
            out.write((position + " "));

            for (int family = 1; family <= numberOfFamilies; family++) {
                int variable = (family - 1) * numberOfExtendedFamily + position;
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

            for (int position1 = familyVariablesStart; position1 < familyVariablesStart
                    + numberOfExtendedFamily - 1; position1++) {
                for (int position2 = position1 + 1; position2 < familyVariablesStart
                        + numberOfExtendedFamily; position2++) {

                    out.write(maximumWeight + " -" + position1 + " -" + position2 + " 0\n");
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
                    out.write(maximumWeight + " -" + familyVariableI
                            + " -" + familyVariableJ
                            + " 0\n");
                }
            }
        }

        /*
         * - genereaza clauzele care garanteaza ca doua familie care nu au o relatie nu vor exista
         * in familia extinsa(clica) pe nicio pozitie. Se foloseste lista de adiacenta
         * relationsFamilies, deoarece este lista de adiacenta complementara.
         */

        for (int i = 1; i <= numberOfFamilies; i++) {
            List<Integer> relationsOfFamily = relationsFamilies.get(i);

            for (Integer relation : relationsOfFamily) {

                for (int position1 = 1; position1 <= numberOfExtendedFamily; position1++) {
                    int family1 = (i - 1) * numberOfExtendedFamily + position1;

                    for (int position2 = 1; position2 <= numberOfExtendedFamily; position2++) {
                        int family2 = (relation - 1) * numberOfExtendedFamily + position2;

                        out.write(maximumWeight + " -" + family1
                                + " -" + family2
                                + " 0\n");
                    }
                }

                relationsFamilies.get(relation).remove((Integer) i);
            }
        }

        out.flush();
    }

    /**
     * - se citeste raspunsul oracolului, iar familiile care fac parte din familia extinsa sunt
     * stocate in complementaryArrestedFamily. Pentru a determina insa, familiile ce
     * trebuiesc arestate, se determina multimea complementara a lui complementaryArrestedFamily si
     * se stocheaza in minimumFamiliesArrested
     */

    @Override
    public void decipherOracleAnswer() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(oracleOutFilename));

        in.readLine(); // se citeste prima linie care nu este necesara

        String[] variables = in.readLine().split(" ", numberOfVariables + 1);

        List<Integer> complementaryArrestedFamilies = new ArrayList<>();
        minimumFamiliesArrested = new ArrayList<>();

        for (String variable : variables) {
            if (!variable.contains("-") && !variable.equals("")) {
                int family = (Integer.parseInt(variable) - 1) / numberOfExtendedFamily + 1;
                complementaryArrestedFamilies.add(family);
            }
        }

        for (int i = 1; i <= numberOfFamilies; i++) {
            if (!complementaryArrestedFamilies.contains(i)) {
                minimumFamiliesArrested.add(i);
            }
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
     * Metoda calculeaza un numar maxim de familii pentru o familie extinsa care are probabilitatea
     * cea mai mare sa existe, analizand relatiile si numarul de relatii dintre acestea.
     *  In primul rand, creeaza o lista de familii sortata descrescator dupa numarul de relatii pe
     * care le are fiecare si calculeaza factorul fiecareia.
     *  Dupa care se itereaza aceasta lista, si se verifica pentru fiecare familie daca factorul
     * sau este mai mare sau egal ca numarul de relatii pe care il are. Daca se satisface conditia
     * inseamna ca exista suficiente familii cu suficiente relatii, astfel incat familia respectiva
     * si toate relatiile ei sa faca parte dintr-o posibila familie extinsa(clica).
     *  Mai departe, pentru o precizie si mai buna, o alta conditie reprezinta ca familiile in
     * relatie cu familia curenta, sa aiba la randul lor cel putin acelasi numar de relatii.
     * (intr-o k-clica toate nodurile trebuie sa aiba un grad mai mare sau egal cu k-1).
     * @return Cand toate conditiile sunt respectate de catre o familie, se returneaza numarul de
     * relatii pe care le are + 1, care reprezinta o clica maxima cu probilitate cea mai mare de
     * existenta.
     */

    private int calculateMaximumExtendedFamily() {
        List<Family> families = new ArrayList<>(numberOfFamilies);

        for (int i = 1; i <= numberOfFamilies; i++) {
            families.add(new Family(fullComplementaryRelations.get(i)));
        }

        List<Family> numbersRelationsSorted = families.stream()
                .sorted((family1, family2) ->
                        family2.getRelations().size() - family1.getRelations().size())
                .collect(Collectors.toList());

        calculateFactors(numbersRelationsSorted);

        for (int i = 1; i < numbersRelationsSorted.size() - 1; i++) {
           int factor = numbersRelationsSorted.get(i).getFactor();

           if (factor  >= numbersRelationsSorted.get(i).getRelations().size()) {
               List<Integer> familyRelations = numbersRelationsSorted.get(i).getRelations();
               int matches = 0;

               for (Integer relation : familyRelations) {
                   List<Integer> counterpartRelations = fullComplementaryRelations.get(relation);

                   if (counterpartRelations.size() >= familyRelations.size()) {
                       matches++;
                   }
               }

               if (matches >= familyRelations.size()) {
                   return numbersRelationsSorted.get(i).getRelations().size() + 1;
               }
           }
       }

      return numbersRelationsSorted.get(numbersRelationsSorted.size() - 1).getRelations().size()
              + 1;
    }

    /**
     * - pentru fiecare familie se calculeaza un factor care reprezinta cate familii mai exista cu
     * un numar mai mare sau egal de relatii.
     */

    private void calculateFactors(final List<Family> numbersRelationsSorted) {
        int contor = 0;

        while (contor < numbersRelationsSorted.size() - 1) {

            int factor = contor;
            int size = numbersRelationsSorted.get(factor).getRelations().size();

            while (factor < numbersRelationsSorted.size() - 1
                    && numbersRelationsSorted.get(factor + 1).getRelations().size() == size) {
                factor++;
            }


            while (contor <= factor) {
                numbersRelationsSorted.get(contor).setFactor(factor);
                contor++;
            }
        }

        numbersRelationsSorted.get(numbersRelationsSorted.size() - 1)
                .setFactor(numbersRelationsSorted.size() - 1);
    }
}
