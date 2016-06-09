package gapp.ulg.play;

import gapp.ulg.game.board.*;

import java.util.*;
import java.util.concurrent.*;


/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi.</b>
 * <br>
 * Un oggetto {@code MCTSPlayer} è un giocatore che gioca seguendo una strategia
 * basata su Monte-Carlo Tree Search e può giocare a un qualsiasi gioco.
 * <br>
 * La strategia che usa è una MCTS (Monte-Carlo Tree Search) piuttosto semplificata.
 * Tale strategia si basa sul concetto di <i>rollout</i> (srotolamento). Un
 * <i>rollout</i> a partire da una situazione di gioco <i>S</i> è l'esecuzione di
 * una partita fino all'esito finale a partire da <i>S</i> facendo compiere ai
 * giocatori mosse random.
 * <br>
 * La strategia adottata da un {@code MCTSPlayer}, è la seguente. In ogni situazione
 * di gioco <i>S</i> in cui deve muovere, prima di tutto ottiene la mappa delle
 * possibili mosse valide da <i>S</i> con le corrispondenti prossime situazioni. Per
 * ogni prossima situazione <i>NS</i> esegue <i>R</i> rollouts e calcola un punteggio
 * di <i>NS</i> dato dalla somma degli esiti dei rollouts. L'esito di un rollout è
 * rappresentato da un intero che è 0 se finisce in una patta, 1 se finisce con la
 * vittoria del giocatore e -1 altrimenti. Infine sceglie la mossa che porta nella
 * prossima situazione con punteggio massimo. Il numero <i>R</i> di rollouts da
 * compiere è calcolato così <i>R = ceil(RPM/M)</i>, cioè la parte intera superiore
 * della divisione decimale del numero di rollout per mossa <i>RPM</i> diviso il
 * numero <i>M</i> di mosse possibili (è sempre esclusa {@link Move.Kind#RESIGN}).
 * @param <P>  tipo del modello dei pezzi */
public class MCTSPlayer<P> implements Player<P> { //Non ho ancora toccato Parallel, il suo utilizzo sarà importante nel test Mix nascosto
    public String name;
    private int rpm;
    private boolean parallel;
    private GameRuler<P> gameRul = null;
    /** Crea un {@code MCTSPlayer} con un limite dato sul numero di rollouts per
     * mossa.
     *
     * @param name  il nome del giocatore
     * @param rpm   limite sul numero di rollouts per mossa, se < 1 è inteso 1
     * @param parallel  se true la ricerca della mossa da fare è eseguita cercando
     *                  di sfruttare il parallelismo della macchina
     * @throws NullPointerException se {@code name} è null */
    public MCTSPlayer(String name, int rpm, boolean parallel) {
        if(name == null) { throw new NullPointerException("Il nome del player non può essere null"); }
        this.name = name;
        this.parallel = parallel;
        if(rpm < 1) { this.rpm = 1; }
        else this.rpm = rpm;
    }

    @Override
    public String name() { return name; }

    @Override
    public void setGame(GameRuler<P> g) {
        if(g == null) { throw new IllegalArgumentException("Il gioco non può essere null"); }
        gameRul = g;
    }

    @Override
    public void moved(int i, Move<P> m) { //Non ho ancora la minima idea di cosa faccia sta cosa...
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public Move<P> getMove() {
        if (gameRul == null || gameRul.result() > -1 ||
                gameRul.players().indexOf(name) + 1 != gameRul.turn()) {
            throw new IllegalStateException("Il gioco potrebbe non essere impostato, terminato o non è il turno del giocatore"); }

        ConcurrentMap<Move<P>, Integer> mNext = new ConcurrentHashMap<>(); //Mappa contenente mosse -> rollouts vincenti
        double rollouts = Math.ceil(rpm/gameRul.validMoves().size()-1); //Numero di Rollouts da eseguire (RESIGN escluso, dunque -1)

        class Operation implements Callable {
            private GameRuler<P> game;

            private Operation(Move<P> m, GameRuler<P> g) {
                game = g;
                game.move(m); //Eseguo immediatamente la mossa
            }

            @Override
            public Integer call() throws Exception {
                int res = 0; //Risultato delle esecuzioni del gioco

                for(double i = 0; i < rollouts; i++) { //Esegue il gioco tante volte quante i Rollouts impostati
                    GameRuler<P> gameExec = game.copy(); //Copia del gioco che verrà resettata ad ogni esecuzione
                    while(gameExec.turn() < 0 || gameExec.result() < 0) {
                        List<Move<P>> temp = new ArrayList<>(); //Copia di ValidMoves in cui rimuovo RESIGN
                        temp.addAll(gameExec.validMoves()); temp.remove(new Move(Move.Kind.RESIGN)); //Evito la mossa resign
                        gameExec.move(temp.get(new Random().nextInt(temp.size()))); //Eseguo la mossa random
                    }

                    if(gameExec.result() == 0) { res += 0; } //Parità
                    else if((gameExec.result() == 1 && gameExec.players().get(0).equals(name)) || //Se la vittoria è del player res++
                            (gameExec.result() == 2 && gameExec.players().get(1).equals(name))) { res += 1; }
                    else res -= 1; //Sconfitta
                }

                return res;
            }
        }

        ExecutorService service = Executors.newCachedThreadPool(); //Default: parallel = true
        if(!parallel) { service = Executors.newFixedThreadPool(1); } //Se parallel è false esegue il test con un thread solo, in maniera sequenziale
        for(Move<P> m : gameRul.validMoves()) {
            if(!m.getKind().equals(Move.Kind.RESIGN)) {
                Callable<Integer> callable = new Operation(m, gameRul.copy());
                try {
                    mNext.put(m, service.submit(callable).get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        service.shutdown();

        Move<P> result = null;
        Integer counter = -99; //Assurdamente basso, in caso di situazioni di sconfitta certa prende una mossa perdente indipendentemente
        for(Map.Entry<Move<P>, Integer> entry : mNext.entrySet()) {
            if(entry.getValue() > counter) { result = entry.getKey(); counter = entry.getValue(); } //Sceglie la mossa con il tasso di vittoria migliore
        }

        return result;
    }
}
