/*
All methods:
getSquare - square at location
verifyLegality - legality of move
getScore - current score
getHand - gives you your hand
canBeDrawnFromHand - can you play that word with your tiles
canBePlacedOnBoard - can this word be played here
placeWord - place the word
wouldBeConnected - would the word be connected to the others
findStartOfWord - start of a word
isValidWord - is the word valid with the dictionary
isOccupied - is this location occupied
wouldCreateOnlyLegalWords - does this word only make legal words
scoreWord - score of playing this word
score - score of playing word including crosswords
play - play the word
exchange - exchange tiles
removeTiles - return string of tiles removed from hand
getCurrentPlayer - current player
gameIsOver - is game over?
neighbor - tile to the right or below current one
antineighbor - tile to the left or above current one
opposite - returns opposite of this direction
isOnBoard - is this location on the board?
playWord - play a word
*/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Test {

    private static String findAnagrams(String[] dictionary, char tile) {
        ArrayList<Character> hand = new ArrayList<>();
        hand.add('a');
        hand.add('r');
        hand.add('c');
        hand.add('a');
        hand.add('r');
        hand.add('e');
        hand.add('z');
        hand.add(tile);

        String bestWord = "";

        for (String word : dictionary) {
            if (bestWord.length() != 0) {
                break;
            } else if (word.length() <= hand.size() && word.indexOf(tile) != -1) {
                bestWord = word;
                ArrayList<Character> handCopy = new ArrayList<>(hand);

                for (int i = 0; i < word.length(); i++) {
                    int index = handCopy.indexOf(word.charAt(i));

                    // letter is not in word
                    if (index == -1) {
                        bestWord = "";
                        break;
                    } else {
                        handCopy.remove(index);
                    }
                }
            }
        }
        return bestWord;
    }

    public static void main(String[] args) {
        In in = new In("src/enable1.txt");
        ArrayList<String> input = new ArrayList<>(Arrays.asList(in.readAllLines()));
        String output = input.stream().distinct().sorted((x, y) -> Integer.compare(y.length(), x.length())).collect(Collectors.joining(","));
        String[] dictionary = output.split(",");

        System.out.println(findAnagrams(dictionary, 'o'));
    }
}
