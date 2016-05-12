package gapp.ulg.play;

import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.Move;
import gapp.ulg.game.board.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Un oggetto RandPlayer è un oggetto che può giocare un qualsiasi gioco regolato
 * da un {@link GameRuler} perché, ad ogni suo turno, sceglie in modo random una
 * mossa tra quelle valide esclusa {@link Move.Kind#RESIGN}.
 * @param <P>  tipo del modello dei pezzi */
public class RandPlayer<P> implements Player<P> {
    public String name;
    public GameRuler<P> gameRul;

    /** Crea un giocatore random, capace di giocare a un qualsiasi gioco, che ad
     * ogni suo turno fa una mossa scelta in modo random tra quelle valide.
     * @param name  il nome del giocatore random
     * @throws NullPointerException se name è null */
    public RandPlayer(String name) {
        if(name == null) { throw new NullPointerException("Il nome del player non può essere null"); }
        this.name = name;
        this.gameRul = null;
    }

    @Override
    public String name() { return name; }

    @Override
    public void setGame(GameRuler<P> g) {
        if(g == null) { throw new IllegalArgumentException("Il gioco non può essere null"); }
        gameRul = g;
    }

    @Override
    public void moved(int i, Move<P> m) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public Move<P> getMove() {
        /*throw new UnsupportedOperationException("DA IMPLEMENTARE");*/
        if(gameRul == null || gameRul.result() >= 0 ||
                gameRul.players().indexOf(name)+1 != gameRul.turn()) { throw new IllegalStateException("Il gioco potrebbe non essere impostato, terminato o non è il turno del giocatore"); }

        List temp = new ArrayList<>();
        temp.addAll(gameRul.validMoves());
        Random rand = new Random();
        return (Move) temp.get(rand.nextInt(temp.size())); } //Mossa random
}
