package gapp.ulg.play;

import gapp.ulg.game.GameFactory;
import gapp.ulg.game.Param;
import gapp.ulg.game.PlayerFactory;
import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.Player;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Una MCTSPlayerFactory è una fabbrica di {@link MCTSPlayer}.
 * @param <P>  tipo del modello dei pezzi */
public class MCTSPlayerFactory<P> implements PlayerFactory<Player<P>,GameRuler<P>> {
    @Override
    public String name() { return "Monte-Carlo Tree Search Player"; }

    @Override
    public void setDir(Path dir) { }

    /** Ritorna una lista con i seguenti due parametri:
     * <pre>
     * Primo parametro
     *     - name: "Rollouts"
     *     - prompt: "Number of rollouts per move"
     *     - values: [1,10,50,100,200,500,1000]
     *     - default: 50
     * Secondo parametro
     *     - name: "Execution"
     *     - prompt: "Threaded execution"
     *     - values: ["Sequential","Parallel"]
     *     - default: "Sequential"
     * </pre>
     * @return la lista con i due parametri */
    @Override
    public List<Param<?>> params() {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public Play canPlay(GameFactory<? extends GameRuler<P>> gF) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public String tryCompute(GameFactory<? extends GameRuler<P>> gF, boolean parallel,
                             Supplier<Boolean> interrupt) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    /** Ritorna un {@link MCTSPlayer} che rispetta i parametri impostati
     * {@link MCTSPlayerFactory#params()} e il nome specificato. */
    @Override
    public Player<P> newPlayer(GameFactory<? extends GameRuler<P>> gF, String name) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }
}
