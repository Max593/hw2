package gapp.ulg.play;

import gapp.ulg.game.board.*;
import gapp.ulg.game.util.Utils;
import gapp.ulg.games.MNKgame;
import gapp.ulg.games.MNKgameFactory;

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
public class MCTSPlayer<P> implements Player<P> {
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
        ConcurrentMap<Move<P>, Integer> mNext = new ConcurrentHashMap<>(); //Mappa contenente mosse, rollouts vincenti

        double rollouts = Math.ceil(rpm/gameRul.validMoves().size()-1); //Numero di Rollouts da eseguire (RESIGN escluso, dunque -1)

        class Operation implements Callable {
            Move<P> mov;
            GameRuler<P> game;

            public Operation(Move<P> m, GameRuler<P> g) {
                mov = m;
                game = g;
                game.move(mov); //Esegue immediatamente la mossa che stiamo analizzando del validMoves()
            }

            @Override
            public Integer call() throws Exception {
                int res = 0; //Risultato modificato dal numero di Rollout del gioco

                for(int i = 0; i < rollouts; i++) { //Esegue il gioco tante volte quante i Rollouts impostati
                    while(game.turn() != 0 || game.result() == -1) {
                        List<Move<P>> temp = new ArrayList<>();
                        temp.addAll(game.validMoves()); temp.remove(new Move(Move.Kind.RESIGN)); //Evito la mossa resign
                        game.move(temp.get(new Random().nextInt(temp.size()))); //Eseguo la mossa
                    }

                    if(game.result() == 0) { res += 0; }
                    else if((game.result() == 1 && game.players().get(0).equals(name)) || //Se la vittoria è del player res++
                            (game.result() == 2 && game.players().get(1).equals(name))) { res += 1; }
                    else res -= 1; //Sconfitta
                }

                return res;
            }
        }

        ExecutorService service = Executors.newCachedThreadPool(); //Vedere se un fixed(1) è uguale a fare un esecuzione sequenziale
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
        Integer counter = -1;
        for(Map.Entry<Move<P>, Integer> entry : mNext.entrySet()) {
            if(entry.getValue() > counter) { result = entry.getKey(); counter = entry.getValue(); }
        }

        return result;
    }
}
