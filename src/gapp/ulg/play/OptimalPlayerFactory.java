package gapp.ulg.play;

import gapp.ulg.game.GameFactory;
import gapp.ulg.game.Param;
import gapp.ulg.game.PlayerFactory;
import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.Move;
import gapp.ulg.game.board.Player;

import static gapp.ulg.game.board.GameRuler.Situation;
import static gapp.ulg.game.board.GameRuler.Next;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Una OptimalPlayerFactory è una fabbrica di {@link OptimalPlayer}
 * @param <P>  tipo del modello dei pezzi */
public class OptimalPlayerFactory<P> implements PlayerFactory<Player<P>,GameRuler<P>> {
    /** Una {@code Strategy} rappresenta una strategia ottimale per uno specifico
     * gioco.
     * @param <P>  tipo del modello dei pezzi */
    interface Strategy<P> {
        /** @return il nome del gioco di cui questa è una strategia ottimale */
        String gName();

        /** Ritorna la mossa (ottimale) nella situazione di gioco specificata. Se
         * {@code s} o {@code next} non sono compatibili con il gioco di questa
         * strategia, il comportamento del metodo è indefinito.
         * @param s  una situazione di gioco
         * @param next  la funzione delle mosse valide e prossime situazioni del
         *              gioco, cioè quella di {@link GameRuler.Mechanics#next}.
         * @return la mossa (ottimale) nella situazione di gioco specificata */
        Move<P> move(Situation<P> s, Next<P> next);
    }

    @Override
    public String name() { return "Optimal Player"; }

    /** Se la directory non è null, in essa salva e recupera file che contengono le
     * strategie ottimali per giochi specifici. Ogni strategia è salvata nella
     * directory in un file il cui nome rispetta il seguente formato:
     * <pre>
     *     strategy_<i>gameName</i>.dat
     * </pre>
     * dove <code><i>gameName</i></code> è il nome del gioco, cioè quello ritornato
     * dal metodo {@link GameRuler#name()}. La directory di default non è impostata
     * e quindi è null. */
    @Override
    public void setDir(Path dir) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    /** Ritorna una lista con il seguente parametro:
     * <pre>
     *     - name: "Execution"
     *     - prompt: "Threaded execution"
     *     - values: ["Sequential","Parallel"]
     *     - default: "Sequential"
     * </pre>
     * @return la lista con il parametro */
    @Override
    public List<Param<?>> params() {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    /** Ritorna {@link Play#YES} se conosce già la strategia ottimale per il gioco
     * specificato o perché è in un file (nella directory impostata con
     * {@link OptimalPlayerFactory#setDir(Path)}) o perché è in memoria, altrimenti
     * stima se può essere praticamente possibile imparare la strategia
     * ottimale e allora ritorna {@link Play#TRY_COMPUTE} altrimenti ritorna
     * {@link Play#NO}. Il gioco, cioè il {@link GameRuler}, valutato è quello
     * ottenuto dalla {@link GameFactory} specificata. Se non conosce già la
     * strategia ritorna sempre {@link Play#TRY_COMPUTE} eccetto che per i giochi
     * con i seguenti nomi che sa che è impossibile calcolarla:
     * <pre>
     *     Othello8x8, Othello10x10, Othello12x12
     * </pre>
     * Il controllo sull'esistenza di un file con la strategia è effettuato solamente
     * in base al nome (senza tentare di leggere il file, perché potrebbe richiedere
     * troppo tempo). */
    @Override
    public Play canPlay(GameFactory<? extends GameRuler<P>> gF) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    /** Tenta di calcolare la strategia ottimale per il gioco specificato. Ovviamente
     * effettua il calcolo solo se il metodo
     * {@link OptimalPlayerFactory#canPlay(GameFactory)} ritorna {@link Play#TRY_COMPUTE}
     * per lo stesso gioco. Il gioco, cioè il {@link GameRuler}, da valutare è quello
     * ottenuto dalla {@link GameFactory} specificata. Se il calcolo ha successo e
     * una directory ({@link OptimalPlayerFactory#setDir(Path)} ) è impostata, tenta
     * di salvare il file con la strategia calcolata, altrimenti la mantiene in
     * memoria. */
    @Override
    public String tryCompute(GameFactory<? extends GameRuler<P>> gF, boolean parallel,
                             Supplier<Boolean> interrupt) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    /** Se il metodo {@link OptimalPlayerFactory#canPlay(GameFactory)} ritorna
     * {@link Play#YES} tenta di creare un {@link OptimalPlayer} con la strategia
     * per il gioco specificato cercandola tra quelle in memoria e se la directory
     * è impostata ({@link OptimalPlayerFactory#setDir(Path)}) anche nel file. */
    @Override
    public Player<P> newPlayer(GameFactory<? extends GameRuler<P>> gF, String name) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }
}
