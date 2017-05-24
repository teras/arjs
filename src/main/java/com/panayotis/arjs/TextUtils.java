/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.panayotis.arjs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author teras
 */
public class TextUtils {

    public static String spaces(int i) {
        char[] chars = new char[i];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    public static Collection<String> split(String text, boolean justified, int size) {
        return split(text, justified, size, size);
    }

    public static Collection<String> split(String text, boolean justified, int firstline, int size) {
        if (firstline < 2)
            firstline = 2;
        if (size < 2)
            size = 2;

        Collection<String> lines = new ArrayList<>();
        List<String> words = new ArrayList<>(Arrays.asList(text.split(" ", -1)));
        Collections.reverse(words);

        int csize = firstline;
        List<String> cur = new ArrayList<>();
        int runningLength = 0;
        while (!words.isEmpty()) {
            String nextWord = words.remove(words.size() - 1);
            int nextWordSize = (cur.isEmpty() ? 0 : 1) + nextWord.length();
            if (runningLength + nextWordSize <= csize) {
                // Next word fits
                cur.add(nextWord);
                runningLength += nextWordSize;
            } else if (!cur.isEmpty()) {
                // Already have some words, display those and put the current word back
                lines.add(convertToDistributedLine(cur, justified ? csize - runningLength : 0));
                words.add(nextWord);
                cur.clear();
                runningLength = 0;
                csize = size;
            } else {
                // This is the only word, should display it truncted anyways
                lines.add(nextWord.substring(0, csize));
                words.add(nextWord.substring(csize));
                cur.clear();
                runningLength = 0;
                csize = size;
            }
        }
        if (!cur.isEmpty())
            lines.add(convertToDistributedLine(cur, 0));
        return lines;
    }

    private static String convertToDistributedLine(List<String> words, int unassigned) {
        if (words.size() > 1)
            while (unassigned > 0)
                for (int i = 1; i < words.size() && unassigned > 0; i++) {
                    unassigned--;
                    words.set(i, " " + words.get(i));
                }
        StringBuilder out = new StringBuilder();
        for (String word : words)
            out.append(" ").append(word);
        return out.substring(1);
    }

}
