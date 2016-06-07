package gapp.ulg.games;

import gapp.ulg.game.GameFactory;
import gapp.ulg.game.Param;
import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.PieceModel;

import static gapp.ulg.game.board.PieceModel.Species;

import java.util.*;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Una OthelloFactory è una fabbrica di {@link GameRuler} per giocare a Othello.
 * I {@link GameRuler} fabbricati dovrebbero essere oggetti {@link Othello}. */
public class OthelloFactory implements GameFactory<GameRuler<PieceModel<Species>>> {
    private String[] pNames;
    private Map<String, Long> map = new HashMap<>();
    private long tempo;
    private int dimensione;

    /** Crea una fattoria di {@link GameRuler} per giocare a Othello */
    public OthelloFactory() {
        for(Object i : params().get(0).values()) {
            String s = (String) i;
            if(s.equals("No limit")) { map.put(s, (long) -1); }
            else if(s.equals("1s")) { map.put(s, (long) 1000); }
            else if(s.equals("2s")) { map.put(s, (long) 2000); }
            else if(s.equals("3s")) { map.put(s, (long) 3000); }
            else if(s.equals("5s")) { map.put(s, (long) 5000); }
            else if(s.equals("10s")) { map.put(s, (long) 10000); }
            else if(s.equals("20s")) { map.put(s, (long) 20000); }
            else if(s.equals("30s")) { map.put(s, (long) 30000); }
            else if(s.equals("1m")) { map.put(s, (long) 60_000); }
            else if(s.equals("2m")) { map.put(s, (long) 120_000); }
            else if(s.equals("5m")) { map.put(s, (long) 300_000); }
        }

        this.tempo = -1; //Tempo di default
        this.dimensione = 8; //Dimensione di default
    }

    @Override
    public String name() { return "Othello"; }

    @Override
    public int minPlayers() { return 2; }

    @Override
    public int maxPlayers() { return 2; }

    /** Ritorna una lista con i seguenti due parametri:
     * <pre>
     * Primo parametro, valori di tipo String
     *     - name: "Time"
     *     - prompt: "Time limit for a move"
     *     - values: ["No limit","1s","2s","3s","5s","10s","20s","30s","1m","2m","5m"]
     *     - default: "No limit"
     * Secondo parametro, valori di tipo String
     *     - name: "Board"
     *     - prompt: "Board size"
     *     - values: ["6x6","8x8","10x10","12x12"]
     *     - default: "8x8"
     * </pre>
     * @return la lista con i due parametri */
    @Override
    @SuppressWarnings("unchecked")
    public List<Param<?>> params() {

        Param<String> time = new Param<String>() {
            private String value = "No limit"; //Valore di default ritornato da get

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

        Param<String> board = new Param<String>() {
            private String value = "8x8"; //Valore di default

            @Override
            public String name() { return "Board"; }

            @Override
            public String prompt() { return "Board size"; }

            @Override
            public List<String> values() {
                return Arrays.asList("6x6", "8x8", "10x10", "12x12");
            }

            @Override
            public void set(Object v) {
                if(values().contains(String.valueOf(v))) {
                    value = String.valueOf(v);
                    dimensione = Integer.parseInt(((String) v).split("x")[0]);
                }
                else throw new IllegalArgumentException("Il valore non è nella lista");
            }

            @Override
            public String get() { return value; }
        };

        return Collections.unmodifiableList(Arrays.asList(time, board));
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

        return new Othello(tempo, dimensione, pNames[0], pNames[1]);
    }
}
