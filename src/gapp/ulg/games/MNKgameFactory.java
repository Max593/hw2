package gapp.ulg.games;

import gapp.ulg.game.Param;
import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.PieceModel;
import gapp.ulg.game.GameFactory;
import gapp.ulg.game.util.Utils;

import static gapp.ulg.game.board.PieceModel.Species;

import java.util.*;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Una {@code MNKgameFactory} è una fabbrica di {@link GameRuler} per giocare a
 * (m,n,k)-game. I {@link GameRuler} fabbricati dovrebbero essere oggetti
 * {@link MNKgame}. */
public class MNKgameFactory implements GameFactory<GameRuler<PieceModel<Species>>> {
    private String[] pNames;
    private long tempo = -1; //Valore di default di tempo
    private Map<String, Long> map = Utils.mapTime();
    private Integer mw = 3; //Valore di default per m
    private Integer nh = 3; //Valore di default per n
    private Integer kk = 3; //Valore di default per k

    @Override
    public String name() { return "m,n,k-game"; }

    @Override
    public int minPlayers() { return 2; }

    @Override
    public int maxPlayers() { return 2; }

    /** Ritorna una lista con i seguenti quattro parametri:
     * <pre>
     * Primo parametro, valori di tipo String
     *     - name: "Time"
     *     - prompt: "Time limit for a move"
     *     - values: ["No limit","1s","2s","3s","5s","10s","20s","30s","1m","2m","5m"]
     *     - default: "No limit"
     * Secondo parametro, valori di tipo Integer
     *     - name: "M"
     *     - prompt: "Board width"
     *     - values: [1,2,3,...,20]
     *     - default: 3
     * Terzo parametro, valori di tipo Integer
     *     - name: "N"
     *     - prompt: "Board height"
     *     - values: [1,2,3,...,20]
     *     - default: 3
     * Quarto parametro, valori di tipo Integer
     *     - name: "K"
     *     - prompt: "Length of line"
     *     - values: [1,2,3]
     *     - default: 3
     * </pre>
     * Per i parametri "M","N" e "K" i valori ammissibili possono cambiare a seconda
     * dei valori impostati. Più precisamente occorre che i valori ammissibili
     * garantiscano sempre le seguenti condizioni
     * <pre>
     *     1 <= K <= max{M,N} <= 20   AND   1 <= min{M,N}
     * </pre>
     * dove M,N,K sono i valori impostati. Indicando con minX, maxX il minimo e il
     * massimo valore per il parametro X le condizioni da rispettare sono:
     * <pre>
     *     minM <= M <= maxM
     *     minN <= N <= maxN
     *     minK <= K <= maxK
     *     minK = 1  AND  maxK = max{M,N}  AND  maxN = 20  AND  maxN = 20
     *     N >= K  IMPLICA  minM = 1
     *     N < K   IMPLICA  minM = K
     *     M >= K  IMPLICA  minN = 1
     *     M < K   IMPLICA  minN = K
     * </pre>
     * @return la lista con i quattro parametri */
    @Override
    public List<Param<?>> params() {
        Param<String> time = new Param<String>() {
            private String value = "No limit"; //Default String per time

            @Override
            public String name() { return "Time"; }

            @Override
            public String prompt() { return "Time limit for a move"; }

            @Override
            public List<String> values() {
                return Arrays.asList("No limit", "1s", "2s", "3s", "5s", "10s", "20s", "30s", "1m", "2m", "5m");
            }

            @Override
            public void set(Object v) {
                if(values().contains(String.valueOf(v))) {
                    value = String.valueOf(v);
                    tempo = map.get(String.valueOf(v));
                }
                else throw new IllegalArgumentException("Il valore non è nella lista");
            }

            @Override
            public String get() { return value; }
        };

        Param<Integer> m = new Param<Integer>() {
            private Integer value = 3;

            @Override
            public String name() { return "M"; }

            @Override
            public String prompt() { return "Board width"; }

            @Override
            public List<Integer> values() {
                List<Integer> val = new ArrayList<>();
                for(int i = 0; i<21; i++) {
                    val.add(i);
                }
                return val;
            }

            @Override
            public void set(Object v) {
                if((Integer) v < 21 || (Integer) v > 0) {
                    value = (Integer) v; mw = (Integer) v;
                } else throw new IllegalArgumentException("Il valore non è accettabile");
            }

            @Override
            public Integer get() { return value; }
        };

        Param<Integer> n = new Param<Integer>() {
            private Integer value = 3;

            @Override
            public String name() { return "N"; }

            @Override
            public String prompt() { return "Board height"; }

            @Override
            public List<Integer> values() {
                List<Integer> val = new ArrayList<>();
                for(int i = 0; i < 21; i++) {
                    val.add(i);
                }
                return val;
            }

            @Override
            public void set(Object v) {
                if((Integer) v < 21 || (Integer) v > 0) {
                    value = (Integer) v; nh = (Integer) v;
                } else throw new IllegalArgumentException("Il valore non è accettabile");
            }

            @Override
            public Integer get() { return value; }
        };

        Param<Integer> k = new Param<Integer>() {
            private Integer value = 3;
            @Override
            public String name() { return "K"; }

            @Override
            public String prompt() { return "Length of line"; }

            @Override
            public List<Integer> values() {
                return Arrays.asList(1,2,3);
            }

            @Override
            public void set(Object v) {
                if((Integer) v <= Math.max(mw,nh) || (Integer) v > 0) {
                    value = (Integer) v; kk = (Integer) v;
                }
            }

            @Override
            public Integer get() { return value; }
        };

        return Collections.unmodifiableList(Arrays.asList(time, m, n, k));
    }

    @Override
    public void setPlayerNames(String... names) {
        for(String i : names) { if(i == null) { throw new NullPointerException("Uno dei nomi è null"); } }
        if(names.length != 2) { throw new IllegalArgumentException("Numero di player non consono alla modalità di gioco"); }
        pNames = names;
    }

    @Override
    public GameRuler<PieceModel<Species>> newGame() {
        if(pNames == null) { throw new IllegalStateException("Nomi dei player non impostati"); }

        return new MNKgame(tempo, mw, nh, kk, pNames[0], pNames[1]);
    }
}
