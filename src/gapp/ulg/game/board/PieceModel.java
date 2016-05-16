package gapp.ulg.game.board;

import java.io.Serializable;
import java.util.Objects;

/** <b>IMPLEMENTARE I METODI SECONDO LE SPECIFICHE DATE NEI JAVADOC. Non modificare
 * le intestazioni dei metodi nè i campi pubblici.</b>
 * <br>
 * Un oggetto PieceModel rappresenta un modello di pezzo. Ad esempio il modello
 * di un pezzo per gli scacchi come l'alfiere (bishop) bianco. Gli oggetti
 * PieceModel sono immutabili.
 * @param <S>  il tipo dei valori di specie dei modelli di pezzi */
public class PieceModel<S extends Enum<S>> implements Serializable {
    /** Le specie di alcuni tra i pezzi più comuni */
    public enum Species {
        DISC, DAMA, PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
    }

    /** Specie del modello di pezzo, non è mai null */
    public final S species;
    /** Colore del modello di pezzo, non è mai null */
    public final String color;

    /** Crea un modello di pezzo con le informazioni date.
     * @param s  la specie
     * @param c  il colore
     * @throws NullPointerException se s o c è null */
    public PieceModel(S s, String c) {
        if(s == null || c == null) { throw new NullPointerException("Specie o colore non può essere null"); }
        this.species = s; this.color = c;
    }

    /**
     * Get di species e color.
     * @return species e color
     */
    public S getSpecies() { return species; }
    public String getColor() { return color; }

    /** Ritorna true se e solo se x è un oggetto di tipo compatibile con
     * PieceModel<?> ed ha gli stessi {@link PieceModel#species} e
     * {@link PieceModel#color}.
     * @param x  un oggetto (o null)
     * @return true x se è uguale a questo modello di pezzo */
    @Override
    public boolean equals(Object x) {
        return x instanceof PieceModel && Objects.equals(((PieceModel) x).getColor(), color) && ((PieceModel) x).getSpecies() == species;
    }

    /** Ridefinito coerentemente con la ridefinizione di
     * {@link PieceModel#equals(Object)}.
     * @return hash code di questo modello di pezzo */
    @Override
    public int hashCode() {
        return Objects.hash(species, color);
    }
}
