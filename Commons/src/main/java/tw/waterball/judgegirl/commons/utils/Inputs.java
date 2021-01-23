/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.commons.utils;

import java.util.Collection;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Inputs {
    private final static Scanner scanner = new Scanner(System.in);

    public static String inputLine(String message) {
        System.out.println(message);
        String line = scanner.nextLine();
        return line.isEmpty() ? inputLine(message) : line;
    }

    public static boolean inputForYesOrNo(String message) {
        System.out.println(message + " [y/n]: ");
        String next = scanner.next().trim().toLowerCase();
        if ("y".equals(next)) {
            return true;
        } else if ("n".equals(next)) {
            return false;
        }
        return inputForYesOrNo(message);
    }

    public static boolean inputForYesOrNo(String message, boolean defaultBoolean) {
        System.out.println(message + " [y/n]: ");
        String next = scanner.next().trim().toLowerCase();
        if (next.isEmpty()) {
            return defaultBoolean;
        } else if ("y".equals(next)) {
            return true;
        } else if ("n".equals(next)) {
            return false;
        }
        System.err.println("Please input 'y' or 'n'.");
        return inputForYesOrNo(message, defaultBoolean);
    }

    public static float inputRangedNumberOrDefault(String message, float defaultNum, float floor, float ceil) {
        do {
            try {
                System.out.println(message + floor + "~" + ceil + " (default: " + defaultNum + "): ");
                String input = scanner.nextLine();
                if (input.isEmpty()) {
                    return defaultNum;
                }
                float num = Float.parseFloat(input);
                if (num < floor || num > ceil) {
                    System.err.println("The number must be in " + floor + " ~ " + ceil);
                } else {
                    return num;
                }
            } catch (NumberFormatException err) {
                System.err.println("Your must input a floating number.");
            }
        } while (true);
    }

    public static int inputRangedInteger(String message, Collection<Integer> options) {
        return inputConditionalInteger(message, options::contains,
                num -> String.format("The input number %d is not in the options.", num));
    }

    public static int inputZeroOrPositiveInteger(String message) {
        return inputConditionalInteger(message, i -> i >= 0);
    }

    public static int inputConditionalInteger(String message, Predicate<Integer> predicate) {
        return inputConditionalInteger(message, predicate,
                num -> String.format("The input number %d is invalid.", num));
    }

    public static int inputConditionalInteger(String message, Predicate<Integer> predicate,
                                              Function<Integer, String> errorMessageSupplier) {
        try {
            System.out.println(message + ": ");
            String input = scanner.next();
            if (input.isEmpty()) {
                return inputConditionalInteger(message, predicate, errorMessageSupplier);
            }
            int num = Integer.parseInt(input);
            if (!predicate.test(num)) {
                System.err.println(errorMessageSupplier.apply(num));
                return inputConditionalInteger(message, predicate, errorMessageSupplier);
            } else {
                return num;
            }
        } catch (NumberFormatException err) {
            System.err.println("Your must input an integer number.");
            return inputConditionalInteger(message, predicate, errorMessageSupplier);
        }
    }

    public static int inputRangedIntegerOrDefault(String message, int defaultNum, int floor, int ceil) {
        do {
            try {
                System.out.println(message + floor + "~" + ceil + " (default: " + defaultNum + "): ");
                String input = scanner.nextLine();
                if (input.isEmpty()) {
                    return defaultNum;
                }
                int num = Integer.parseInt(input);
                if (num < floor || num > ceil) {
                    System.err.println("The number must be in " + floor + " ~ " + ceil);
                } else {
                    return num;
                }
            } catch (NumberFormatException err) {
                System.err.println("Your must input a integer number.");
            }
        } while (true);
    }

}
