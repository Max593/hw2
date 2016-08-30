package gapp.ulg.game.util;

import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.PieceModel;
import gapp.ulg.game.board.Pos;

import static gapp.ulg.game.board.GameRuler.Situation;
import static gapp.ulg.game.board.GameRuler.Next;
import static gapp.ulg.game.board.GameRuler.Mechanics;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/** <b>IMPLEMENTARE I METODI INDICATI CON "DA IMPLEMENTARE" SECONDO LE SPECIFICHE
 * DATE NEI JAVADOC. Non modificare le intestazioni dei metodi.</b>
 * <br>
 * Metodi per analizzare giochi */
public class Probe { //Questo sistema attualmente funziona solo per Othello e Mnk
    /** Un oggetto {@code EncS} è la codifica compatta di una situazione di gioco
     * {@link GameRuler.Situation}. È utile per mantenere in memoria insiemi con
     * moltissime situazioni minimizzando la memoria richiesta.
     * @param <P>  tipo del modello dei pezzi */
    public static class EncS<P> {
        private long time;
        private int np;
        private int w = 1;
        private int h = 1;
        private BigInteger table;
        private int turn;

        /** Crea una codifica compatta della situazione data relativa al gioco la
         * cui meccanica è specificata. La codifica è compatta almeno quanto quella
         * che si ottiene codificando la situazione con un numero e mantenendo in
         * questo oggetto solamente l'array di byte che codificano il numero in
         * binario. Se i parametri di input sono null o non sono tra loro compatibili,
         * il comportamento è indefinito.
         * @param gM  la meccanica di un gioco
         * @param s  una situazione dello stesso gioco */
        public EncS(Mechanics<P> gM, Situation<P> s) {
            if(gM == null || s == null) { throw new NullPointerException("Parametri non definiti, nulla da codificare"); } //Non so se sia necessario al momento

            for(Pos p : gM.positions) { //Per determinare le dimensioni del tavolo da gioco e dunque tutte le posizioni
                if(p.getB()+1 > this.w) { this.w = p.getB()+1; }
                if(p.getT()+1 > this.h) { this.h = p.getT()+1; }
            }

            String coded = "";
            Map<Pos, P> allPcs = s.newMap();
            for(int i = 0; i < w; i++) {
                for(int j = 0; j < h; j++) {
                    Pos p = new Pos(i, j);
                    if(!gM.positions.contains(p)) { continue; } //Dovrebbe evitare di scrivere le posizioni non presenti nella board
                    else if(!allPcs.containsKey(p)) { coded += 0; }
                    else if(allPcs.get(p).equals(new PieceModel<>(PieceModel.Species.DISC, "nero"))) { coded += 1; }
                    else if(allPcs.get(p).equals(new PieceModel<>(PieceModel.Species.DISC, "bianco"))) { coded += 2; }
                }
            }
            coded = new BigInteger(coded, 3).toString(10);

            this.table = new BigInteger(coded);
            this.turn = s.turn;
            this.time = gM.time;
            this.np = gM.np;
        }

        /** Ritorna la situazione codificata da questo oggetto. Se {@code gM} è null
         * o non è la meccanica del gioco della situazione codificata da questo
         * oggetto, il comportamento è indefinito.
         * @param gM  la meccanica del gioco a cui appartiene la situazione
         * @return la situazione codificata da questo oggetto */
        public Situation<P> decode(Mechanics<P> gM) {
            if(gM == null) { throw new NullPointerException("Nessuna Mechanics inserita, impossibile decodificare"); }

            String recoded = table.toString(3);
            Map<Pos, PieceModel<PieceModel.Species>> mapDec = new HashMap<>();
            int add = gM.positions.size() - recoded.length(); //Devo verificare se è ancora utile, ma non dovrebbe pesare tanto
            recoded = new String(new char[add]).replace("\0", "0")+recoded;

            int counter = 0;
            for(int i = 0; i < w; i++) {
                for(int j = 0; j < h; j++) {
                    Pos p = new Pos(i, j);
                    if(gM.positions.contains(p)){
                        if(String.valueOf(recoded.charAt(counter)).equals("0")) { counter++; }
                        else if(String.valueOf(recoded.charAt(counter)).equals("1")) { counter++; mapDec.put(p, new PieceModel<>(PieceModel.Species.DISC, "nero")); }
                        else if(String.valueOf(recoded.charAt(counter)).equals("2")) { counter++; mapDec.put(p, new PieceModel<>(PieceModel.Species.DISC, "bianco")); }
                    }
                }
            }

            return new Situation<>((Map<Pos, P>) mapDec, turn);
        }

        /** Questa oggetto è uguale a {@code x} se e solo se {@code x} è della stessa
         * classe e la situazione codificata è la stessa. Il test è effettuato senza
         * decodificare la situazione, altrimenti sarebbe troppo lento.
         * @param x  un oggetto
         * @return true se {@code x} rappresenta la stessa situazione di questo
         * oggetto */
        @Override
        public boolean equals(Object x) {
            return x instanceof EncS && Objects.equals(((EncS) x).table, table) && ((EncS) x).time == time &&
                    ((EncS) x).np == np && ((EncS) x).turn == turn;
        }

        /** Ridefinito coerentemente con la ridefinizione di {@link EncS#equals(Object)}.
         * @return l'hash code di questa situazione codificata */
        @Override
        public int hashCode() { return Objects.hash(table,time,np,turn); }
    }

    /** Un oggetto per rappresentare il risultato del metodo
     * {@link Probe#nextSituations(boolean, Next, Function, Function, Set)}.
     * Chiamiamo grado di una situazione <i>s</i> il numero delle prossime situazioni
     * a cui si può arrivare dalla situazione <i>s</i>.
     * @param <S>  tipo della codifica delle situazioni */
    public static class NSResult<S> {
        /** Insieme delle prossime situazioni */
        public final Set<S> next;
        /** Statistiche: il minimo e il massimo grado delle situazioni di partenza
         * e la somma di tutti gradi */
        public final long min, max, sum;

        public NSResult(Set<S> nx, long mn, long mx, long s) {
            next = nx;
            min = mn;
            max = mx;
            sum = s;
        }
    }

    /** Ritorna l'insieme delle prossime situazioni dell'insieme di situazioni date.
     * Per ogni situazione nell'insieme {@code start} ottiene le prossime situazioni
     * tramite {@code nextF}, previa decodifica con {@code dec}, e le aggiunge
     * all'insieme che ritorna, previa codifica con {@code cod}. La computazione può
     * richiedere tempi lunghi per questo è sensibile all'interruzione del thread
     * in cui il metodo è invocato. Se il thread è interrotto, il metodo ritorna
     * immediatamente o quasi, sia che l'esecuzione è parallela o meno, e ritorna
     * null. Se qualche parametro è null o non sono coerenti (ad es. {@code dec} non
     * è il decodificatore del codificatore {@code end}), il comportamento è
     * indefinito.
     * @param parallel  se true il metodo cerca di sfruttare il parallelismo della
     *                  macchina
     * @param nextF  la funzione che ritorna le prossime situazioni di una situazione
     * @param dec  funzione che decodifica una situazione
     * @param enc  funzione che codifica una situazione
     * @param start  insieme delle situazioni di partenza
     * @param <P>  tipo del modello dei pezzi
     * @param <S>  tipo della codifica delle situazioni
     * @return l'insieme delle prossime situazioni dell'insieme di situazioni date o
     * null se l'esecuzione è interrotta. */
    public static <P,S> NSResult<S> nextSituations(boolean parallel, Next<P> nextF,
                                                   Function<S,Situation<P>> dec,
                                                   Function<Situation<P>,S> enc,
                                                   Set<S> start) {
        if(nextF == null || dec == null || enc == null || start == null) { throw new NullPointerException("Uno o più parametri non sono utilizzabili"); }

        ConcurrentMap<Set<S>, Integer> nxtS = new ConcurrentHashMap<>(); //Mappa per produrre finalSet

        class Operation implements Callable{
            private Situation<P> decSit; //Situazione decodificata
            private Operation(S t) { this.decSit = dec.apply(t); }

            @Override
            public Map<Set<S>, Integer> call() throws Exception {
                Map<Set<S>, Integer> res = new HashMap<>();

                Set<S> setSit = new HashSet<>(); //Set di risultato
                for(Map.Entry<?, Situation<P>> entry : nextF.get(decSit).entrySet()) { setSit.add(enc.apply(entry.getValue())); }
                res.put(setSit, setSit.size());
                return res;
            }
        }

        ExecutorService executor = Executors.newCachedThreadPool();
        Set<Future<Map<Set<S>, Integer>>> listFut = new HashSet<>();
        for(S s : start) {
            Operation callable = new Operation(s);
            Future<Map<Set<S>, Integer>> future = executor.submit(callable);
            listFut.add(future);
        }

        for(Future<Map<Set<S>, Integer>> future : listFut) {
            try {
                nxtS.putAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();

        Set<S> finalSet = new HashSet<>();
        int min = Collections.min(nxtS.values());
        int max = Collections.max(nxtS.values());
        int sum = 0;
        for(Map.Entry<Set<S>, Integer> entry : nxtS.entrySet()) {
            sum += entry.getValue();
            finalSet.addAll(entry.getKey());
        }

        return new NSResult<>(finalSet, min, max, sum);
    }
}
