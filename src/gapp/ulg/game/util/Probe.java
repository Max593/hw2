package gapp.ulg.game.util;

import gapp.ulg.game.board.GameRuler;
import gapp.ulg.game.board.PieceModel;
import gapp.ulg.game.board.Pos;

import static gapp.ulg.game.board.GameRuler.Situation;
import static gapp.ulg.game.board.GameRuler.Next;
import static gapp.ulg.game.board.GameRuler.Mechanics;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

/** <b>IMPLEMENTARE I METODI INDICATI CON "DA IMPLEMENTARE" SECONDO LE SPECIFICHE
 * DATE NEI JAVADOC. Non modificare le intestazioni dei metodi.</b>
 * <br>
 * Metodi per analizzare giochi */
public class Probe {
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
        private Map<Pos, P> temp; //Remove

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

            //Codifica tipo pedine assente al momento, da risolvere

            //Codifica della Situation [ Cambiare sistema di scorrimento, utilizzare w,h e prendere contenuto situation ]
            String coded = "";
            Map<Pos, P> allPcs = s.newMap();
            for(Pos p : gM.positions) { if(!allPcs.containsKey(p)) { allPcs.put(p, null); } } //Mappa completa con pezzi pieni e non

            for(Map.Entry<Pos, P> entry : allPcs.entrySet()) { //Da raffinare in caso di giochi con più di due pedine (abbastanza semplice)
                if(entry.getValue() == null) { coded += 0; }
                else if(entry.getValue().equals(new PieceModel<>(PieceModel.Species.DISC, "nero"))) { coded += 1; }
                else if(entry.getValue().equals(new PieceModel<>(PieceModel.Species.DISC, "bianco"))) { coded += 2; }
            }

            this.table = new BigInteger(coded);
            System.out.println(coded+" "+table); //Remove
            this.turn = s.turn;
            this.time = gM.time;
            this.np = gM.np;
            this.temp = s.newMap(); //Remove
        }

        /** Ritorna la situazione codificata da questo oggetto. Se {@code gM} è null
         * o non è la meccanica del gioco della situazione codificata da questo
         * oggetto, il comportamento è indefinito.
         * @param gM  la meccanica del gioco a cui appartiene la situazione
         * @return la situazione codificata da questo oggetto */
        public Situation<P> decode(Mechanics<P> gM) {
            if(gM == null) { throw new NullPointerException("Nessuna Mechanics inserita, impossibile decodificare"); }

            String recoded = String.valueOf(table);
            Map<Pos, PieceModel<PieceModel.Species>> mapDec = new HashMap<>();
            int add = gM.positions.size() - recoded.length();
            recoded = new String(new char[add]).replace("\0", "0")+recoded;
            System.out.println(recoded); //Remove

            int counter = 0;
            for(Pos p : gM.positions) {
                if(String.valueOf(recoded.charAt(counter)).equals("0")) { counter++; }
                else if(String.valueOf(recoded.charAt(counter)).equals("1")) { counter++; mapDec.put(p, new PieceModel<>(PieceModel.Species.DISC, "nero")); }
                else if(String.valueOf(recoded.charAt(counter)).equals("2")) { counter++; mapDec.put(p, new PieceModel<>(PieceModel.Species.DISC, "bianco")); }
            }
//
            for(Map.Entry<Pos, PieceModel<PieceModel.Species>> entry : mapDec.entrySet()) {
                System.out.println(entry.getKey().getB()+"x"+entry.getKey().getT()+" "+temp.containsKey(entry.getKey()));
            }
            System.out.println();
            for(Map.Entry<Pos, P> entry : temp.entrySet()) {
                System.out.println(entry.getKey().getB()+"x"+entry.getKey().getT()+" "+mapDec.containsKey(entry.getKey()));
            }
//
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
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }
}
