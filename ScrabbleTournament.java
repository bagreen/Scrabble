/** A tournament between ScrabbleAIs. Edit the constructor to change the contestants. */
public class ScrabbleTournament {

    /** Contestants. */
    private ScrabbleAI[] players;

    public ScrabbleTournament() {
        // List contestants here
        players = new ScrabbleAI[] {
          new Incrementalist(),
          new SmartAI()
        };
    }

    public static void main(String[] args) throws IllegalMoveException {
        new ScrabbleTournament().run();
    }

    private static int smartavg = 0;
    private static int incrementavg = 0;

    /**
     * Plays two games between each pair of contestants, one with each going first. Prints the number of wins for
     * each contestant (including 0.5 wins for each tie).
     */
    public void run() throws IllegalMoveException {
        double[] scores = new double[players.length];

        int totalRuns = 100;

        for (int runs = 0; runs < totalRuns / 2; runs++) {
            for (int i = 0; i < players.length; i++) {
                for (int j = 0; j < players.length; j++) {
                    if (i != j) {
                        double[] result = playGame(players[i], players[j]);
                        scores[i] += result[0];
                        scores[j] += result[1];
                    }
                }
            }

        }
        System.out.println();
        System.out.println("Incrementalist: " + scores[0]);
        System.out.println("SmartAI: " + scores[1]);
        System.out.println();
        System.out.println("Incrementalist Average: " + (incrementavg / totalRuns));
        System.out.println("SmartAI Average: " + (smartavg / totalRuns));
    }

    /**
     * Plays a game between a (going first) and b. Returns their tournament scores, either {1, 0} (if a wins),
     * {0, 1}, or {0.5, 0.5}.
     */
    public double[] playGame(ScrabbleAI a, ScrabbleAI b) throws IllegalMoveException {
        //StdOut.println(a + " vs " + b + ":");
        Board board = new Board();
        a.setGateKeeper(new GateKeeper(board, 0));
        b.setGateKeeper(new GateKeeper(board, 1));
        while (!board.gameIsOver()) {
            playMove(board, a, 0);
            if (!board.gameIsOver()) {
                playMove(board, b, 1);
            }
        }
        int s0 = board.getScore(0);
        int s1 = board.getScore(1);
        //StdOut.print(board);

        int increment = 0;
        int smart = 0;

        if (a.toString().contains("Smart")) {
            System.out.println("Final score: Incrementalist " + s1 + ", Smart AI " + s0);
            increment = s1;
            smart = s0;
        }
        else {
            System.out.println("Final score: Incrementalist " + s0 + ", Smart AI " + s1);
            increment = s0;
            smart = s1;
        }

        incrementavg += increment;
        smartavg += smart;

        if (increment > smart) {
            System.out.println(board);
            System.out.println();
        }

        if (s0 > s1) {
            return new double[] {1, 0};
        } else if (s0 < s1) {
            return new double[] {0, 1};
        }
        // Tie -- half credit to each player.
        return new double[] {0.5, 0.5};
    }

    /**
     * Asks player for a move and plays it on board.
     * @param playerNumber Player's place in the game turn order (0 or 1).
     */
    public void playMove(Board board, ScrabbleAI player, int playerNumber) throws IllegalMoveException {
        player.chooseMove().play(board, playerNumber);
    }

}
