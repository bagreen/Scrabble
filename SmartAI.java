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

public class SmartAI implements ScrabbleAI {

    private static final boolean[] ALL_TILES = {true, true, true, true, true, true, true};
    private static String[] dictionary = new String[172823];
    private GateKeeper gateKeeper;

    @Override
    public void setGateKeeper(GateKeeper gateKeeper) {
        this.gateKeeper = gateKeeper;
    }

    private PlayWord findAnagrams(String[] dictionary) {
        ArrayList<Character> hand = gateKeeper.getHand();
        ArrayList<Location> placedTiles = findPlacedTiles();
        PlayWord bestMove = null;
        int bestScore = 0;
        String chosenWord = "";

        for (Location location : placedTiles) {
            char tile;
            tile = gateKeeper.getSquare(location);
            //System.out.println("Tile: "+ tile + " at location: " + location.getRow() + ", " + location.getColumn());
            for (String word : dictionary) {
                int tileIndex = word.indexOf(tile);
                String bestWord;

                if (word.length() <= hand.size() + 1 && tileIndex != -1) {
                    char[] wordChar = word.toCharArray();
                    //bestWord = new String(wordChar);
                    boolean foundWord = true;
                    boolean foundTile = false;
                    ArrayList<Character> handCopy = new ArrayList<>(hand);
                    handCopy.add(tile);

                    int charIndex = 0;
                    for (char i : wordChar) {
                        int index = handCopy.indexOf(i);

                        if (index == -1) {
                            int blankIndex = handCopy.indexOf(' ');

                            if (blankIndex != -1) {
                                handCopy.remove(blankIndex);
                                wordChar[charIndex] = Character.toUpperCase(i);
                            }
                            else {
                                foundWord = false;
                                break;
                            }
                        }
                        else if (!foundTile) {
                            if (i == tile) {
                                wordChar[charIndex] = ' ';
                                foundTile = true;
                            }
                        }
                        handCopy.remove(index);
                        charIndex++;
                    }

                    if (foundWord && foundTile) {
                        bestWord = new String(wordChar);
                        PlayWord newMove = tryHorizontalAndVertical(bestWord, location, bestMove, bestScore);
                        if (newMove != bestMove) {
                            bestMove = newMove;
                            bestScore = newScore(bestWord, location);
                            chosenWord = bestWord.replace(' ', tile);
                            break;
                        }
                    }
                }
            }
        }

        if (chosenWord.length() > 0) System.out.println("Played:" + chosenWord);
        else System.out.println("Traded in");
        System.out.print("Hand:");
        for (char letter : hand) System.out.print(letter + ",");
        System.out.println();
        System.out.println();

        return bestMove;
    }

    private boolean isOccupied(Location location) {
        return Character.isAlphabetic(gateKeeper.getSquare(location));
    }

    private ArrayList<Location> findPlacedTiles() {
        ArrayList<Location> placedTiles = new ArrayList<>();

        for (int row = 0; row < Board.WIDTH; row++) {
            for (int col = 0; col < Board.WIDTH; col++) {
                Location location = new Location(row, col);
                if (isOccupied(location)) {
                    placedTiles.add(location);
                }
            }
        }

        return placedTiles;
    }

    private int newScore(String wordToPlay, Location location) {
        int wordScore = 0;

        int displace = wordToPlay.indexOf(' ');
        Location horizontalLocation = new Location(location.getRow() - displace, location.getColumn());
        Location verticalLocation = new Location(location.getRow(), location.getColumn() - displace);

        try {
            gateKeeper.verifyLegality(wordToPlay, horizontalLocation, Location.HORIZONTAL);
            wordScore = gateKeeper.score(wordToPlay, horizontalLocation, Location.HORIZONTAL);
        } catch (IllegalMoveException e) {
            // skip!
        }

        try {
            gateKeeper.verifyLegality(wordToPlay, verticalLocation, Location.VERTICAL);
            wordScore = gateKeeper.score(wordToPlay, verticalLocation, Location.VERTICAL);
        } catch (IllegalMoveException e) {
            // skip!
        }

        return wordScore;
    }

    private PlayWord tryHorizontalAndVertical(String wordToPlay, Location location, PlayWord bestMove, int bestScore) {

        int displace = wordToPlay.indexOf(' ');
        Location horizontalLocation = new Location(location.getRow() - displace, location.getColumn());
        Location verticalLocation = new Location(location.getRow(), location.getColumn() - displace);

        try {
            gateKeeper.verifyLegality(wordToPlay, horizontalLocation, Location.HORIZONTAL);
            int wordScore = gateKeeper.score(wordToPlay, horizontalLocation, Location.HORIZONTAL);

            if (wordScore > bestScore) {
                bestMove = new PlayWord(wordToPlay, horizontalLocation, Location.HORIZONTAL);
            }
        } catch (IllegalMoveException e) {
            // skip!
        }

        try {
            gateKeeper.verifyLegality(wordToPlay, verticalLocation, Location.VERTICAL);
            int wordScore = gateKeeper.score(wordToPlay, verticalLocation, Location.VERTICAL);

            if (wordScore > bestScore) {
                bestMove = new PlayWord(wordToPlay, verticalLocation, Location.VERTICAL);
            }
        } catch (IllegalMoveException e) {
            // skip!
        }

        return bestMove;
    }

    @Override
    public ScrabbleMove chooseMove() {
        // has dictionary been made yet?
        // if not, let's set it up
        if (dictionary[0] == null) {
            In in = new In("enable1.txt");
            ArrayList<String> input = new ArrayList<>(Arrays.asList(in.readAllLines()));
            String output = input.stream().distinct().sorted((x, y) -> Integer.compare(y.length(), x.length())).collect(Collectors.joining(","));
            dictionary = output.split(",");
        }

        PlayWord bestMove = findAnagrams(dictionary);

        if (bestMove != null) {
            return bestMove;
        } else {
            return new ExchangeTiles(ALL_TILES);
        }
    }
}
