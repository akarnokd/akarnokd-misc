/*
 * Copyright (C) 2015 José Paumard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package hu.akarnokd.comparison;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public final class Util {

    private Util() { }

    static Path findPath(String resourceName) {
        try {
            String uri = new File(Util.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
            int i = uri.indexOf("akarnokd-misc");
            return Paths.get(uri.substring(0, i + 13), "files", resourceName);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Paths.get("files", resourceName);
        }
    }
    
    public static Set<String> readScrabbleWords() {
        Set<String> scrabbleWords = new HashSet<>() ;
        try (Stream<String> scrabbleWordsStream = Files.lines(findPath("ospd.txt"))) {
            scrabbleWords.addAll(scrabbleWordsStream.map(String::toLowerCase).collect(Collectors.toSet()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scrabbleWords;
    }

    public static Set<String> readShakespeareWords() {
        Set<String> shakespeareWords = new HashSet<>() ;
        try (Stream<String> shakespeareWordsStream = Files.lines(findPath("words.shakespeare.txt"))) {
            shakespeareWords.addAll(shakespeareWordsStream.map(String::toLowerCase).collect(Collectors.toSet()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return shakespeareWords ;
    }
}