package gapp.ulg.game.board;

/** <b>Non modificare le intestazioni dei metodi e non aggiungere metodi.</b>
 * <br>
 * Un oggetto Player rappresenta un giocatore. Può essere un giocatore capace di
 * giocare un solo tipo di gioco (ad es. solamente Scacchi) oppure capace di giocare
 * molti giochi. Un (oggetto) Player può giocare un solo gioco, cioè partita, o più
 * partite. Una partita inizia invocando il metodo {@link Player#setGame(GameRuler)}.
 * Può essere una nuova partita o una partita già iniziata che si chiede di
 * proseguire. Dopo di ciò la partita è giocata tramite i metodi
 * {@link Player#moved(int, Move)} e {@link Player#getMove()} invocati
 * alternativamente fino a quando la partita termina.
 * @param <P>  tipo del modello dei pezzi */
public interface Player<P> {
    /** Ritorna il nome del giocatore. Dovrebbe ritornare sempre lo stesso nome.
     * @return il nome del giocatore */
    String name();

    /** Imposta il gioco a cui il giocatore dovrebbe giocare. Il gioco g può essere
     * nello stato iniziale ma può anche essere in uno stato diverso, ad es. se la
     * partita era stata interrotta ed ora viene ripresa. L'oggetto g è una copia
     * del GameRuler ufficiale del gioco. Il giocatore può usare e modificare g a
     * piacimento senza che questo abbia effetto sul GameRuler ufficiale. In
     * particolare il giocatore può usare g per mantenersi sincronizzato con lo
     * stato del gioco riportando in g le sue mosse e quelle degli altri giocatori,
     * vedi {@link Player#moved(int, Move)}.
     * <br>
     * <b>Il metodo non blocca, non usa altri thread e ritorna velocemente.</b>
     * @param g  un gioco, cioè una partita
     * @throws NullPointerException se g è null
     * @throws IllegalArgumentException se il giocatore, per qualsiasi ragione, non
     * può (o non vuole) giocare il gioco g */
    void setGame(GameRuler<P> g);

    /** Comunica al giocatore la mossa eseguita da un giocatore, compreso il
     * giocatore stesso. Il giocatore dovrebbe usare tale informazione per
     * aggiornare la sua copia del GameRuler.
     * <br>
     * <b>Il metodo non blocca, non usa altri thread e ritorna velocemente.</b>
     * @param i  indice di turnazione di un giocatore
     * @param m  la mossa eseguita dal giocatore
     * @throws IllegalStateException se non c'è un gioco impostato o c'è ma è
     * terminato.
     * @throws NullPointerException se m è null
     * @throws IllegalArgumentException se i non è l'indice di turnazione di un
     * giocatore o m non è una mossa valida nell'attuale situazione di gioco */
    void moved(int i, Move<P> m);

    /** Ritorna la mossa del giocatore, relativamente all'attuale stato di gioco. Il
     * giocatore dovrebbe attendere che sia invocato il metodo
     * {@link Player#moved(int, Move)} prima di aggiornare lo stato di gioco della
     * sua copia.
     * <br>
     * <b>Se il gioco mette un limite sul tempo per effettuare una mossa
     * {@link GameRuler.Mechanics#time}, il metodo fa di tutto per rispettarlo. Può
     * usare thread addizionali per calcolare la mossa, però quando ritorna tutti
     * i thread usati sono stati liberati o terminati, cioè se non sono terminati
     * non ci sono computazioni residue in esecuzione dovute a questo metodo su tali
     * thread.</b>
     * @return la mossa del giocatore
     * @throws IllegalStateException se non c'è un gioco impostato, c'è ma è terminato
     * oppure non è il turno di questo giocatore (l'indice di turnazione di questo
     * giocatore è determinato alla prima mossa e quindi il controllo sul turno è
     * dalla seconda mossa in poi). */
    Move<P> getMove();
}
