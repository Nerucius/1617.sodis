/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.io;

import poker5cardgame.io.Source;
import java.util.Arrays;
import java.util.Scanner;
import poker5cardgame.game.Card;
import poker5cardgame.game.Game;

/**
 * Simple Keyboard source to play the game on the same computer.
 */
public class KeyboardSource implements Source {

    Scanner scan;

    public KeyboardSource() {
        scan = new Scanner(System.in);
    }

    public Game.Move getNextMove() {
        Game.Move move = new Game.Move();

        String command = scan.nextLine();
        String[] args = command.split(" ");
        try {
            move.action = Game.Action.valueOf(args[0]);
        } catch (Exception e) {
            System.err.println("INVALID ACTION");
            move.action = Game.Action.NOOP;
        }

        try{
        switch (move.action) {
            case BET:
                move.chips = Integer.valueOf(args[1]);
                break;

            case RAISE:
                move.chips = Integer.valueOf(args[1]);
                break;

            case DRAW:
                // Read a list of cards from console
                int numCards = Integer.valueOf(args[1]);
                move.cards = new Card[numCards];
                for (int i = 0; i < numCards; i++)
                    move.cards[i] = Card.fromCode(args[2 + i]);
                break;

        }
        }catch(Exception e){
            System.err.println("SYNTAX ERROR");
            move.action = Game.Action.NOOP;
        }

        return move;
    }

    public void sendMove(Game.Move move) {
        // PRINTS SERVER REPLY TO Console
        System.out.println("SERVER: " + move.action);

        // Optional Arguments
        switch (move.action) {
            case SEND_ANTE_STAKES:
                System.out.println("ANTE: " + move.chips);
                System.out.println("STAKES: " + move.cStakes + " " + move.sStakes);
                break;
            case DEALER_HAND:
                System.out.println("DEALER:" + move.dealer);
                System.out.println("HAND:" + Arrays.toString(move.cards));
                break;
            case BET:
                System.out.println("" + move.chips);
                break;
            case RAISE:
                System.out.println("" + move.chips);
                break;
            case DRAW_SERVER:
                System.out.println("DRAWN" + Arrays.toString(move.cards));
                break;
            case SHOW:
                System.out.println("SHOW" + Arrays.toString(move.cards));
                break;
            case NOOP:
                break;
        }
    }

}
