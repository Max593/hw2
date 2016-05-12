package gapp.ulg.play;

import gapp.ulg.game.board.*;


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
    /** Crea un {@code MCTSPlayer} con un limite dato sul numero di rollouts per
     * mossa.
     *
     * @param name  il nome del giocatore
     * @param rpm   limite sul numero di rollouts per mossa, se < 1 è inteso 1
     * @param parallel  se true la ricerca della mossa da fare è eseguita cercando
     *                  di sfruttare il parallelismo della macchina
     * @throws NullPointerException se {@code name} è null */
    public MCTSPlayer(String name, int rpm, boolean parallel) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public String name() { throw new UnsupportedOperationException("DA IMPLEMENTARE"); }

    @Override
    public void setGame(GameRuler<P> g) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public void moved(int i, Move<P> m) {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }

    @Override
    public Move<P> getMove() {
        throw new UnsupportedOperationException("DA IMPLEMENTARE");
    }
}
