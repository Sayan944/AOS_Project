import java.util.Scanner;

import node.UniversityNode;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println(
                "======================================");

        System.out.println(
                "National Supercomputing Consortium");

        System.out.println(
                "Raymond Mutual Exclusion");

        System.out.println(
                "Socket Programming Project");

        System.out.println(
                "======================================");

        System.out.print(
                "Enter University ID (1-5): ");

        int id = sc.nextInt();

        System.out.print(
                "Enter INITIAL TOKEN HOLDER ID (1-5): ");

        int initialTokenHolder =
                sc.nextInt();

        UniversityNode node =
                new UniversityNode(
                        id,
                        initialTokenHolder);

        System.out.println();

        if (id == initialTokenHolder) {

            System.out.println(
                    "University "
                            + id
                            + " initially HOLDS the TOKEN.");

        } else {

            System.out.println(
                    "University "
                            + id
                            + " initially does NOT hold the TOKEN.");

        }

        while (true) {

            System.out.println(
                    "\n========= MENU =========");

            System.out.println(
                    "1. Request AI Accelerator");

            System.out.println(
                    "2. Start Snapshot");

            System.out.println(
                    "3. Show Status");

            System.out.println(
                    "4. Exit");

            System.out.print(
                    "Enter Choice : ");

            int choice =
                    sc.nextInt();

            switch (choice) {

                case 1:

                    node.requestToken();

                    break;

                case 2:

                    node.startSnapshot();

                    break;

                case 3:

                    node.printStatus();

                    break;

                case 4:

                    System.out.println(
                            "University "
                                    + id
                                    + " shutting down.");

                    System.exit(0);

                    break;

                default:

                    System.out.println(
                            "Invalid Choice.");
            }
        }
    }
}