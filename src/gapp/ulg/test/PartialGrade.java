package gapp.ulg.test;


import gapp.ulg.game.board.*;
import gapp.ulg.game.util.BoardOct;
import gapp.ulg.play.RandPlayer;
import gapp.ulg.game.util.Utils;
import gapp.ulg.games.Othello;
import gapp.ulg.games.OthelloFactory;
import gapp.ulg.games.MNKgame;
import gapp.ulg.games.MNKgameFactory;
import gapp.ulg.game.GameFactory;
import gapp.ulg.game.Param;

import static gapp.ulg.game.board.Board.Dir;
import static gapp.ulg.game.board.PieceModel.Species;
import static gapp.ulg.game.board.GameRuler.Situation;
import static gapp.ulg.game.board.GameRuler.Mechanics;
import static gapp.ulg.game.board.GameRuler.Next;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


/** <b>SECONDA VERSIONE PRELIMINARE 12/5/2016 (sostituisce quella del 10/5/2016)</b>
 * <br>
 * Questa è la seconda versione preliminare del partial grade che dà fino a 30 punti.
 * La versione definitiva del partial grade che darà fino a 40 punti, sarà pubblicata
 * all'inizio della prossima settimana.
 * <br>
 * <b>ATTENZIONE: NON MODIFICARE IN ALCUN MODO QUESTA CLASSE.</b> */
public class PartialGrade {
    public static void main(String[] args) { test(); }

    private static void test() {
        System.setOut(new PrintStream(OUTPUT));
        System.setIn(INPUT);
        OUTPUT.setThread(Thread.currentThread());
        OUTPUT.standardOutput(true);
        INPUT.setThread(Thread.currentThread());
        INPUT.standardInput(true);
        totalGradeInit();
        totalScore = 0;
        boolean ok = true;
        ok = gradeTest1(ok);
        ok = gradeTest2(ok);
        ok = gradeTest3(ok);
        System.out.println("Punteggio parziale: "+Math.round(totalScore*10)/10.0);
    }


    /**************************   G R A D E  1   ******************************/

    private static boolean gradeTest1(boolean ok) {
        if (ok) ok = test_Pos(0.2f, 500);
        if (ok) ok = test_PieceModel(0.2f, 500);
        if (ok) ok = test_BoardDef(0.8f, 500);
        if (ok) ok = test_Action(1f, 500);
        if (ok) ok = test_Move(0.6f, 500);
        if (ok) ok = test_GameRulerDef(0.8f, 500);
        if (ok) ok = test_BoardOct(1f, 1000);
        if (ok) ok = test_UnmodifiableBoard(0.6f, 500);
        if (ok) ok = test_PlayRandPlayer(0.8f, 1000);
        return ok;
    }

    private static boolean test_Pos(float sc, int ms) {
        return runTest("Test Pos", sc, ms, () -> {
            try {
                Pos p1 = new Pos(12,13), p2 = new Pos(12,13), p3 = new Pos(12,11);
                if (p1.equals(null)) return err("Pos.equals true invece di false");
                if (!p1.equals(p2)) return err("Pos.equals false invece di true");
                if (p1.equals(p3)) return err("Pos.equals true invece di false");
                if (p1.hashCode() != p2.hashCode()) return err("Pos.hashCode() diversi invece di uguali");
                boolean err = true;
                for (int i = 0 ; i < 13 ; i++)
                    if (p1.hashCode() != new Pos(i,i).hashCode()) err = false;
                if (err) return err("Pos.hashCode()");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_PieceModel(float sc, int ms) {
        return runTest("Test PieceModel", sc, ms, () -> {
            try {
                PieceModel<Species> pm1 = new PieceModel<>(Species.DAMA, "white"),
                        pm2 = new PieceModel<>(Species.DAMA, "white"),
                        pm3 = new PieceModel<>(Species.DAMA, "black");
                if (pm1.equals(null)) return err("PieceModel.equals true invece di false");
                if (!pm1.equals(pm2)) return err("PieceModel.equals false invece di true");
                if (pm1.equals(pm3)) return err("PieceModel.equals true invece di false");
                if (pm1.hashCode() != pm2.hashCode()) return err("PieceModel.hashCode() diversi invece di uguali");
                boolean err = true;
                for (Species s : Species.values())
                    if (pm1.hashCode() != new PieceModel<>(s, "black").hashCode()) err = false;
                if (err) return err("PieceModel.hashCode()");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_BoardDef(float sc, int ms) {
        return runTest("Test Board default methods", sc, ms, () -> {
            try {
                DummyBoard<PieceModel<Species>> b = new DummyBoard<>();
                if (!b.isPos(new Pos(0,0))) return err("Board.isPos false invece di true");
                if (b.isPos(new Pos(10,0))) return err("Board.isPos true invece di false");
                try {
                    b.isPos(null);
                    return err("Board.isPos(null): non lancia eccezione");
                } catch (Exception ex) {}
                Set<Pos> pSet = b.get();
                if (!pSet.isEmpty()) return err("Board.get(): insieme non vuoto");
                try {
                    pSet.add(new Pos(0,0));
                    return err("Board.get(): insieme modificabile");
                } catch (Exception ex) {}
                PieceModel<Species> pm = new PieceModel<>(Species.BISHOP, "white");
                Pos p = new Pos(2,3);
                b.put(pm, p);
                pSet = b.get();
                if (pSet.size() != 1 || !pSet.contains(p)) return err("Board.get(): insieme errato");
                try {
                    PieceModel<Species> pm2 = null;
                    b.get(pm2);
                    return err("Board.get(P pm) con pm == null"+nE);
                } catch (Exception ex) {}
                pSet = b.get(pm);
                try {
                    pSet.add(new Pos(0,0));
                    return err("Board.get(P pm): insieme modificabile");
                } catch (Exception ex) {}
                if (pSet.size() != 1 || !pSet.contains(p)) return err("Board.get(P pm): insieme errato");
                pSet = b.get(new PieceModel<>(Species.BISHOP, "black"));
                if (!pSet.isEmpty()) return err("Board.get(P pm): insieme errato");
                try {
                    PieceModel<Species> pmNull = null;
                    notEx(() -> b.put(pmNull, new Pos(0,0), Dir.UP, 1), NullPointerException.class, "con pm == null"+nE);
                    notEx(() -> b.put(pm, null, Dir.UP, 1), NullPointerException.class, "con p == null"+nE);
                    notEx(() -> b.put(pm, new Pos(0,0), null, 1), NullPointerException.class, "con d == null"+nE);
                    notEx(() -> b.put(pm, new Pos(0,0), Dir.UP, 0), IllegalArgumentException.class, "con n == 0"+nE);
                    String err = "se una posizione della linea non è nella board"+nE;
                    notEx(() -> b.put(pm, new Pos(0,0), Dir.UP, 15), IllegalArgumentException.class, err);
                    notEx(() -> b.put(pm, new Pos(3,0), Dir.DOWN, 2), IllegalArgumentException.class, err);
                    notEx(() -> b.put(pm, new Pos(10,0), Dir.UP, 1), IllegalArgumentException.class, err);
                } catch (Exception ex) { return err("Board.put(P pm,Pos p,Dir d,int n) "+ex.getMessage()); }
                b.put(new PieceModel<>(Species.KING, "white"), new Pos(1,2), Dir.UP_R, 5);
                pSet = b.get(new PieceModel<>(Species.KING, "white"));
                if (pSet.size() != 5) return err("Board.put(P pm,Pos p,Dir d,int n): insieme pos errato");
                for (Pos pp : new Pos[] {new Pos(1,2),new Pos(2,3),new Pos(3,4),new Pos(4,5),new Pos(5,6)})
                    if (!pSet.contains(pp)) return err("Board.put(P pm,Pos p,Dir d,int n): insieme pos errato");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_Action(float sc, int ms) {
        return runTest("Test Action", sc, ms, () -> {
            try {
                Pos p = new Pos(0,0), p2 = new Pos(1,1);
                PieceModel<Species> pmNull = null;
                PieceModel<Species> pm = new PieceModel<>(Species.KING, "black");
                Action<PieceModel<Species>> a = new Action<>(p, pm);
                if (a.kind != Action.Kind.ADD || !pm.equals(a.piece) || a.pos == null ||
                        a.pos.size() != 1 || !a.pos.contains(p))
                    return err("Action(Pos p, P pm)");
                try {
                    notEx(() -> new Action<>(null, pm), NullPointerException.class, "con p == null"+nE);
                    notEx(() -> new Action<>(new Pos(0,0), pmNull), NullPointerException.class, "con pm == null"+nE);
                } catch (Exception ex) { return err("Action(Pos p, P pm) "+ex.getMessage()); }
                Action<PieceModel<Species>> a1 = new Action<>(p, pm);
                Action<PieceModel<Species>> a2 = new Action<>(p, pm);
                if (!a1.equals(a2)) return err("Action.equals false invece di true");
                PieceModel<Species> pm2 = new PieceModel<>(Species.PAWN, "black");
                Action<PieceModel<Species>> a3 = new Action<>(p, pm2);
                if (a2.equals(a3)) return err("Action.equals true invece di false");
                boolean herr = true;
                for (Species s : Species.values())
                    if (a1.hashCode() != new Action<>(p, new PieceModel<>(s, "a")).hashCode()) herr = false;
                if (herr) return err("Action.hashCode()");

                a = new Action<>(p);
                if (a.kind != Action.Kind.REMOVE || a.piece != null || a.pos == null || a.pos.size() != 1 || !a.pos.contains(p))
                    return err("Action(Pos...pp)");
                try {
                    Action<PieceModel<Species>> af = a;
                    notEx(() -> af.pos.add(p), Exception.class, "lista pos è modificabile");
                    notEx(() -> new Action<>(), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                    notEx(() -> new Action<>(p, (Pos)null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                } catch (Exception ex) { return err("Action(Pos...pp) "+ex.getMessage()); }
                a1 = new Action<>(p);
                a2 = new Action<>(p);
                if (!a1.equals(a2)) return err("Action.equals false invece di true");
                a3 = new Action<>(p, new Pos(1,1));
                if (a2.equals(a3)) return err("Action.equals true invece di false");

                a = new Action<>(Dir.RIGHT, 2, p);
                if (a.kind != Action.Kind.MOVE || a.piece != null || a.pos == null ||
                        a.pos.size() != 1 || !a.pos.contains(p) || a.dir != Dir.RIGHT || a.steps != 2)
                    return err("Action(Dir d,int ns,Pos...pp)");
                try {
                    Action<PieceModel<Species>> af = a;
                    notEx(() -> af.pos.add(p), Exception.class, "la lista pos è modificabile");
                    notEx(() -> new Action<>(Dir.RIGHT, 2), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2, p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2, p, null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                    notEx(() -> new Action<>(null, 2, p), NullPointerException.class, "con d null"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 0, p), IllegalArgumentException.class, "con ns < 1"+nE);
                } catch (Exception ex) { return err("Action(Dir d,int ns,Pos...pp) "+ex.getMessage()); }
                a1 = new Action<>(Dir.UP, 3, p, p2);
                a2 = new Action<>(Dir.UP, 3, p, p2);
                if (!a1.equals(a2)) return err("Action.equals false invece di true");
                a3 = new Action<>(Dir.UP, 3, p);
                if (a2.equals(a3)) return err("Action.equals true invece di false");

                a = new Action<>(p, p2);
                if (a.kind != Action.Kind.JUMP || a.piece != null || a.pos == null ||
                        a.pos.size() != 2 || !p.equals(a.pos.get(0)) || !p2.equals(a.pos.get(1)))
                    return err("Action(Pos p1,Pos p2)");
                try {
                    Action<PieceModel<Species>> af = a;
                    notEx(() -> af.pos.add(p), Exception.class, "la lista pos è modificabile");
                    notEx(() -> new Action<>((Pos)null, p2), NullPointerException.class, "con p1 null"+nE);
                    notEx(() -> new Action<>(p, (Pos)null), NullPointerException.class, "con p2 null"+nE);
                    notEx(() -> new Action<>(p, p), IllegalArgumentException.class, "con p1 == p2"+nE);
                } catch (Exception ex) { return err("Action(Pos p1,Pos p2) "+ex.getMessage());}
                a1 = new Action<>(p, p2);
                a2 = new Action<>(p, p2);
                if (!a1.equals(a2)) return err("Action.equals false invece di true");
                a3 = new Action<>(p, new Pos(4,1));
                if (a2.equals(a3)) return err("Action.equals true invece di false");

                a = new Action<>(pm, p);
                if (a.kind != Action.Kind.SWAP || !pm.equals(a.piece) || a.pos == null ||
                        a.pos.size() != 1 || !p.equals(a.pos.get(0)))
                    return err("Action(P pm,Pos...pp)");
                try {
                    Action<PieceModel<Species>> af = a;
                    notEx(() -> af.pos.add(p), Exception.class, "lista pos è modificabile");
                    notEx(() -> new Action<>(pmNull, p), NullPointerException.class, "con pm null"+nE);
                    notEx(() -> new Action<>(pm), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(pm, p, null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                    notEx(() -> new Action<>(pm, p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                } catch (Exception ex) { return err("Action(P pm,Pos...pp) "+ex.getMessage()); }
                a1 = new Action<>(pm, p, p2);
                a2 = new Action<>(pm, p, p2);
                if (!a1.equals(a2)) return err("Action.equals false invece di true");
                a3 = new Action<>(pm, p, new Pos(4,1));
                if (a2.equals(a3)) return err("Action.equals true invece di false");
                List<Pos> pp = Arrays.asList(new Pos(0,0),new Pos(1,1),new Pos(2,2),new Pos(1,4),new Pos(4,1));
                for (int i = 0 ; i < 10 ; i++) {
                    Action<PieceModel<Species>> aa1 = new Action<>(pp.toArray(new Pos[0]));
                    Collections.shuffle(pp);
                    Action<PieceModel<Species>> aa2 = new Action<>(pp.toArray(new Pos[0]));
                    if (!aa1.equals(aa2)) return err("Action.equals false invece di true");
                    if (aa1.hashCode() != aa1.hashCode()) return err("Action.hashCode diversi");
                }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_Move(float sc, int ms) {
        return runTest("Test Move", sc, ms, () -> {
            try {
                Pos p = new Pos(0,0), p2 = new Pos(2,2);
                Move.Kind kNull = null;
                PieceModel<Species> pm = new PieceModel<>(Species.KING, "black");
                Action<PieceModel<Species>> a = new Action<>(p, p2), a2 = new Action<>(new Pos(1,1));
                Move<PieceModel<Species>> m = new Move<>(Move.Kind.PASS);
                if (!Move.Kind.PASS.equals(m.kind) || m.actions == null || m.actions.size() != 0)
                    return err("Move(Kind k)");
                try {
                    Move<PieceModel<Species>> mf = m;
                    notEx(() -> mf.actions.add(a), Exception.class, "lista actions è modificabile");
                    notEx(() -> new Move<>(kNull), NullPointerException.class, "con k null"+nE);
                    notEx(() -> new Move<>(Move.Kind.ACTION), IllegalArgumentException.class, "con k == ACTION"+nE);
                } catch (Exception ex) { return err("Move(Kind k) "+ex.getMessage()); }
                Move<PieceModel<Species>> m1 = new Move<>(Move.Kind.RESIGN);
                Move<PieceModel<Species>> m2 = new Move<>(Move.Kind.RESIGN);
                if (!m1.equals(m2)) return err("Move.equals false invece di true");
                Move<PieceModel<Species>> m3 = new Move<>(Move.Kind.PASS);
                if (m2.equals(m3)) return err("Move.equals true invece di false");

                m = new Move<>(a, a2);
                if (!Move.Kind.ACTION.equals(m.kind) || m.actions == null ||
                        m.actions.size() != 2 || !a.equals(m.actions.get(0)) || !a2.equals(m.actions.get(1)))
                    return err("Move(Action<P>...aa)");
                try {
                    Move<PieceModel<Species>> mf = m;
                    notEx(() -> mf.actions.add(a), Exception.class, "lista actions è modificabile");
                    notEx(() -> new Move<>(a, null), NullPointerException.class, "con aa che contiene un elemento null"+nE);
                    notEx(() -> new Move<>(), IllegalArgumentException.class, "con aa vuoto"+nE);
                } catch (Exception ex) { return err("Move(Kind k) "+ex.getMessage()); }
                m1 = new Move<>(a, a2);
                m2 = new Move<>(a, a2);
                if (!m1.equals(m2)) return err("Move.equals false invece di true");
                m3 = new Move<>(a);
                if (m2.equals(m3)) return err("Move.equals true invece di false");

                List<Action<PieceModel<Species>>> aa = Arrays.asList(a, a2);
                m = new Move<>(aa);
                if (!Move.Kind.ACTION.equals(m.kind) || m.actions == null ||
                        m.actions.size() != 2 || !a.equals(m.actions.get(0)) || !a2.equals(m.actions.get(1)))
                    return err("Move(List<Action<P>> aa)");
                aa.set(0,a2);
                if (!a.equals(m.actions.get(0)) || !a2.equals(m.actions.get(1)))
                    return err("Move(List<Action<P>> aa): mantiene la lista aa");
                try {
                    Move<PieceModel<Species>> mf = m;
                    notEx(() -> mf.actions.add(a), Exception.class, "lista actions è modificabile");
                    notEx(() -> new Move<>(a, null), NullPointerException.class, "con aa che contiene un elemento null"+nE);
                    notEx(() -> new Move<>(), IllegalArgumentException.class, "con aa vuoto"+nE);
                } catch (Exception ex) { return err("Move(Kind k) "+ex.getMessage()); }
                m1 = new Move<>(a, a2);
                m2 = new Move<>(a, a2);
                if (!m1.equals(m2)) return err("Move.equals false invece di true");
                m3 = new Move<>(a);
                if (m2.equals(m3)) return err("Move.equals true invece di false");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_GameRulerDef(float sc, int ms) {
        return runTest("Test GameRuler default methods", sc, ms, () -> {
            try {
                Pos p = new Pos(0,0), p2 = new Pos(2,2), pNull = null;
                DummyGU gR = new DummyGU("a", "b");
                Set<Move<PieceModel<Species>>> vm = gR.validMoves();
                Move<PieceModel<Species>> last = null;
                for (Move<PieceModel<Species>> m : vm) {
                    if (!gR.isValid(m)) return err("isValid(m) ritorna false invece di true");
                    last = m;
                }
                vm = gR.validMoves(last.actions.get(0).pos.get(0));
                if (vm == null || vm.size() != 1 || !vm.contains(last)) return err("validMoves(p)");
                try {
                    Set<Move<PieceModel<Species>>> vm2 = vm;
                    Move<PieceModel<Species>> m = last;
                    notEx(() -> vm2.add(m), Exception.class, "insieme validMoves(p) è modificabile");
                    notEx(() -> gR.isValid(null), NullPointerException.class, "isValid(null)"+nE);
                    notEx(() -> gR.validMoves(null), NullPointerException.class, "validMoves(null)"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
                gR.move(last);
                if (gR.isValid(last)) return err("isValid(m) ritorna true invece di false");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_BoardOct(float sc, int ms) {
        return runTest("Test BoardOct", sc, ms, () -> {
            try {
                Pos p = new Pos(0,0), p2 = new Pos(2,2), pNull = null;
                PieceModel<Species> pm = new PieceModel<>(Species.KING, "black"), pmNull = null;
                BoardOct<PieceModel<Species>> b = new BoardOct<>(8, 8);
                if (b.width() != 8 || b.height() != 8 || b.system() != Board.System.OCTAGONAL)
                    return err("BoardOct(8,8): width() o height() o system()");
                if (!b.isModifiable())
                    return err("BoardOct(8,8): isModifiable() ritorna false");
                List<Pos> lp = b.positions();
                try {
                    notEx(() -> lp.add(p2), Exception.class, "la lista positions() è modificabile");
                } catch (Exception ex) { return err("BoardOct(8,8): "+ex.getMessage()); }
                Pos pp = b.adjacent(new Pos(0,0), Dir.UP);
                if (!new Pos(0,1).equals(pp)) return err("BoardOct(8,8): adjacent((0,0),UP) errato");
                pp = b.adjacent(new Pos(0,0), Dir.RIGHT);
                if (!new Pos(1,0).equals(pp)) return err("BoardOct(8,8): adjacent((0,0),RIGHT) errato");
                pp = b.adjacent(new Pos(0,0), Dir.LEFT);
                if (pp != null) return err("BoardOct(8,8): adjacent((0,0),LEFT) errato");
                p = new Pos(2,2);
                b.put(pm, p);
                if (!pm.equals(b.get(p))) return err("BoardOct(8,8): put(P pm,Pos p) o get(Pos p) errato");
                b.remove(p);
                if (b.get(p) != null) return err("BoardOct(8,8): remove(Pos p) errato");
                try {
                    notEx(() -> b.adjacent(pNull, Dir.UP), NullPointerException.class, "adjacent(null,d)"+nE);
                    notEx(() -> b.adjacent(new Pos(0,0), null), NullPointerException.class, "adjacent(p,null)"+nE);
                    notEx(() -> b.put(pm, new Pos(10,10)), IllegalArgumentException.class, "put(pm,(10,10))"+nE);
                } catch (Exception ex) { return err("BoardOct(8,8): "+ex.getMessage()); }

                BoardOct<PieceModel<Species>> b2 = new BoardOct<>(12, 15, Arrays.asList(new Pos(0,0),new Pos(5,5)));
                if (b2.width() != 12 || b2.height() != 15 || b2.system() != Board.System.OCTAGONAL)
                    return err("BoardOct(12,15): width() o height() o system()");
                if (!b2.isModifiable())
                    return err("BoardOct(12,15): isModifiable() ritorna false");
                List<Pos> lp2 = b.positions();
                try {
                    notEx(() -> lp2.add(p2), Exception.class, "lista positions() è modificabile");
                } catch (Exception ex) { return err("BoardOct(12,15): "+ex.getMessage()); }
                if (b2.isPos(new Pos(5,5))) return err("BoardOct(12,5,{(0,0),(5,5)}): isPos((5,5)) errato");
                if (b2.adjacent(new Pos(4,4), Dir.UP_R) != null)
                    return err("BoardOct(12,15,{(0,0),(5,5)}): adjacent((4,4),UP_R) errato");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_UnmodifiableBoard(float sc, int ms) {
        return runTest("Test UnmodifiableBoard con BoardOct", sc, ms, () -> {
            try {
                Pos p = new Pos(0,0), p2 = new Pos(2,2), pNull = null;
                PieceModel<Species> pm = new PieceModel<>(Species.KING, "black"), pmNull = null;
                Board<PieceModel<Species>> ub = Utils.UnmodifiableBoard(new BoardOct<>(8, 8));
                if (ub.isModifiable()) return err("isModifiable() ritorna true");
                try {
                    notEx(() -> ub.put(pm, p), UnsupportedOperationException.class, "put(pm,p)"+nE);
                    notEx(() -> ub.put(pm, p, Dir.UP, 1), UnsupportedOperationException.class, "put(pm,p,UP,1)"+nE);
                    notEx(() -> ub.remove(p), UnsupportedOperationException.class, "remove(p)"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_PlayRandPlayer(float sc, int ms) {
        return runTest("Test Play e RandPlayer", sc, ms, () -> {
            try {
                DummyGUFactory gF = new DummyGUFactory();
                Player<PieceModel<Species>> py1 = new RandPlayer<>("A");
                Player<PieceModel<Species>> py2 = new RandPlayer<>("B");
                GameRuler<PieceModel<Species>> gR = Utils.play(gF,py1,py2);
                if (gR.result() != 0) return err("risultato finale");
                try {
                    notEx(() -> Utils.play(null, py1, py2), NullPointerException.class, "play(null,p1,p2)"+nE);
                    notEx(() -> Utils.play(gF,py1), IllegalArgumentException.class, "play(gf,p1) quando il gioco richiede 2 giocatori"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    /******************************************************************************/
    /**************************   G R A D E  2   **********************************/
    /******************************************************************************/

    private static boolean gradeTest2(boolean ok) {
        if (ok) ok = test_DGameInit(0.2f, 1000, "Othello", 8,8, () -> new Othello(PP[0],PP[1]),
                new int[]{4,4, 3,3}, new int[]{3,4, 4,3});
        if (ok) ok = test_DGame(0.2f, 1000, "Othello", 8,8, "1", G_OTH8_1, () -> new Othello(PP[0],PP[1]));
        if (ok) ok = test_DGame(0.4f, 2000, "Othello", 8,8, "2", G_OTH8_2, () -> new Othello(PP[0],PP[1]));
        if (ok) ok = test_DGame(0.8f, 2000, "Othello", 6,6, "3", G_OTH6, () -> new Othello(-1,6,PP[0],PP[1]));
        if (ok) ok = test_DGameUnMove(0.8f, 2000, "Othello", 8,8, G_OTH8_1, () -> new Othello(PP[0],PP[1]));
        if (ok) ok = test_DGameCopy(0.4f, 1000, "Othello", 8,8, G_OTH8_1, () -> new Othello(PP[0],PP[1]));
        if (ok) ok = test_PlayDGameFRandPlayer(0.4f, 2000, "OthelloFactory", null, OthelloFactory::new);
        if (ok) ok = test_Mix(0.8f, 1000);
        return ok;
    }

    @FunctionalInterface
    private interface GRC {
        GameRuler<PieceModel<Species>> get(long t, int w, int h);
    }

    private static final String C1 = "nero", C2 = "bianco", nE = " non lancia eccezione";
    private static final String[] PP = {"Alice","Bob"};

    private static boolean test_DGameInit(float sc, int ms, String gN, int w, int h,
                                          Supplier<GameRuler<PieceModel<Species>>> gC, int[] nn, int[] bb) {
        return runTest("Test "+gN+" "+w+"x"+h+" inizio", sc, ms, () -> {
            try {
                GameRuler<PieceModel<Species>> gR = gC.get();
                if (!Objects.equals(Arrays.asList(PP[0],PP[1]), gR.players()))
                    return err("players()");
                if (!Objects.equals(gR.color(PP[0]), C1) || !Objects.equals(gR.color(PP[1]), C2))
                    return err("color(name)");
                if (gR.result() != -1) return err("result()");
                if (!gR.isPlaying(1) || !gR.isPlaying(2)) return err("isPlaying()");
                if (gR.turn() != 1) return err("turn()");
                Board<PieceModel<Species>> board = gR.getBoard();
                for (int i = 0 ; i < w+1 ; i++)
                    for (int j = 0 ; j < h+1 ; j++)
                        if (i < w && j < h) {
                            if (!board.isPos(new Pos(i, j))) return err("Board.isPos(p)");
                        } else if (board.isPos(new Pos(i, j))) return err("Board.isPos(p)");
                if (!checkDGameBoard(board, w, h, nn, bb)) return err("board iniziale errata");
                if (board.isModifiable()) return err("Board.isModifiable() ritorna true");
                try {
                    notEx(() -> gR.color("nome errato"), IllegalArgumentException.class, "color(\"nome errato\")"+nE);
                    notEx(() -> gR.color(null), NullPointerException.class, "color(null)"+nE);
                    notEx(() -> gR.isPlaying(3), IllegalArgumentException.class, "isPlaying(3)"+nE);
                    notEx(() -> board.remove(new Pos(4,4)), UnsupportedOperationException.class, "Board.remove(p)"+nE);
                    notEx(() -> new Othello(null,null), NullPointerException.class, "new Othello(null,null)"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_DGame(float sc, int ms, String gN, int w, int h, String gName, String g,
                                      Supplier<GameRuler<PieceModel<Species>>> gC) {
        return runTest("Test " + gN + " " + w + "x" + h + " partita " + gName, sc, ms, () -> {
            try {
                GameRuler<PieceModel<Species>> gR = gC.get();
                Board<PieceModel<Species>> board = gR.getBoard();
                String[] game = g.split("\n");
                int k = 0;
                while (true) {
                    String sb = game[k++];
                    if (sb.length() != w * h) {
                        if (Integer.parseInt(sb) != gR.result()) return err("result()");
                        break;
                    }
                    if (!eqBoard(board, sb)) return err("board");
                    String svm = game[k++];
                    Set<Move<PieceModel<Species>>> vms = gR.validMoves();
                    if (!checkValidMoves(vms, svm)) return err("validMoves()");
                    String[] tm = game[k++].split("\\s+");
                    if (gR.turn() != Integer.parseInt(tm[0])) return err("turn()");
                    gR.move(stringToMove(vms, tm[1]));
                }
                try {
                    notEx(gR::validMoves, IllegalStateException.class, "validMoves() a gioco terminato" + nE);
                    notEx(() -> gR.move(new Move<>(Move.Kind.RESIGN)), IllegalStateException.class, "move(m) a gioco terminato" + nE);
                    notEx(() -> gR.isValid(new Move<>(Move.Kind.RESIGN)), IllegalStateException.class, "isValid(m) a gioco terminato" + nE);
                    notEx(() -> gR.validMoves(new Pos(0,0)), IllegalStateException.class, "validMoves(p) a gioco terminato" + nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result();
        });
    }

    private static boolean test_DGameUnMove(float sc, int ms, String gN, int w, int h, String g,
                                            Supplier<GameRuler<PieceModel<Species>>> gC) {
        return runTest("Test "+gN+" "+w+"x"+h+" unMove", sc, ms, () -> {
            try {
                GameRuler<PieceModel<Species>> gR = gC.get();
                Board<PieceModel<Species>> board = gR.getBoard();
                String[] game = g.split("\n");
                List<String> boards = new ArrayList<>();
                List<Move<PieceModel<Species>>> moves = new ArrayList<>();
                int k = 0;
                while (true) {
                    String sb = game[k++];
                    if (sb.length() != w*h) {
                        if (Integer.parseInt(sb) != gR.result()) return err("result()");
                        break;
                    }
                    if (!eqBoard(board, sb)) return err("board");
                    boards.add(sb);
                    String svm = game[k++];
                    Set<Move<PieceModel<Species>>> vms = gR.validMoves();
                    if (!checkValidMoves(vms, svm)) return err("validMoves()");
                    String[] tm = game[k++].split("\\s+");
                    if (gR.turn() != Integer.parseInt(tm[0])) return err("turn()");
                    Move<PieceModel<Species>> m = stringToMove(vms, tm[1]);
                    gR.move(m);
                    moves.add(m);
                    for (int i = moves.size() - 1 ; i >= 0 ; i--) {
                        gR.unMove();
                        if (!eqBoard(board, boards.get(i)))
                            return err("board unMove "+moves.size()+" "+i);
                    }
                    for (int i = 0 ; i < moves.size() ; i++)
                        gR.move(moves.get(i));
                }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_DGameCopy(float sc, int ms, String gN, int w, int h, String g,
                                          Supplier<GameRuler<PieceModel<Species>>> gC) {
        return runTest("Test "+gN+" "+w+"x"+h+" copy", sc, ms, () -> {
            try {
                GameRuler<PieceModel<Species>> gR = gC.get();
                GameRuler<PieceModel<Species>> gRCopy = gR.copy();
                Board<PieceModel<Species>> board = gRCopy.getBoard();
                String[] game = g.split("\n");
                int k = 0;
                while (true) {
                    String sb = game[k++];
                    if (sb.length() != w*h) {
                        if (Integer.parseInt(sb) != gRCopy.result())
                            return err("copy result()");
                        break;
                    }
                    if (!eqBoard(board, sb)) return err("copy board");
                    String svm = game[k++];
                    Set<Move<PieceModel<Species>>> vms = gRCopy.validMoves();
                    if (!checkValidMoves(vms, svm)) return err("copy validMoves()");
                    String[] tm = game[k++].split("\\s+");
                    if (gRCopy.turn() != Integer.parseInt(tm[0])) return err("copy turn()");
                    gRCopy.move(stringToMove(vms, tm[1]));
                    if (!eqBoard(gR.getBoard(), game[0])) return err("original board");
                }
                try {
                    notEx(gRCopy::validMoves, IllegalStateException.class, "validMoves() a gioco terminato"+nE);
                    notEx(() -> gRCopy.move(new Move<>(Move.Kind.RESIGN)), IllegalStateException.class, "move(m) a gioco terminato"+nE);
                    notEx(() -> gRCopy.isValid(new Move<>(Move.Kind.RESIGN)), IllegalStateException.class, "isValid(m) a gioco terminato"+nE);
                    notEx(() -> gRCopy.validMoves(new Pos(0,0)), IllegalStateException.class, "validMoves(p) a gioco terminato"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_PlayDGameFRandPlayer(float sc, int ms, String fN, Map<String,Object> pv,
                                                     Supplier<GameFactory<GameRuler<PieceModel<Species>>>> fC) {
        return runTest("Test "+fN+" con Play e RandPlayer", sc, ms, () -> {
            try {
                GameFactory<GameRuler<PieceModel<Species>>> gF = fC.get();
                if (pv != null)
                    gF.params().stream().filter(p -> pv.containsKey(p.name())).forEach(p -> p.set(pv.get(p.name())));
                Player<PieceModel<Species>> py1 = new RandPlayer<>("A");
                Player<PieceModel<Species>> py2 = new RandPlayer<>("B");
                GameRuler<PieceModel<Species>> gR = Utils.play(gF, py1, py2);
                if (gR.result() == -1) return err("terminazione");
                try {
                    notEx(gR::validMoves, IllegalStateException.class, "validMoves() a gioco terminato"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_Mix(float sc, int ms) {
        return runTest("Test Mix", sc, ms, () -> {
            try {
                try {
                    notEx(() -> new Pos(0,-1), IllegalArgumentException.class, "new Pos(0,-1)"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
                DummyBoard<PieceModel<Species>> b = new DummyBoard<>();
                PieceModel<Species> pm = new PieceModel<>(Species.BISHOP, "white");
                PieceModel<Species> pmNull = null;
                try {
                    notEx(() -> b.put(pmNull, new Pos(0,0), Dir.UP, 1), NullPointerException.class, "con pm == null"+nE);
                    notEx(() -> b.put(pm, null, Dir.UP, 1), NullPointerException.class, "con p == null"+nE);
                    notEx(() -> b.put(pm, new Pos(0,0), null, 1), NullPointerException.class, "con d == null"+nE);
                    notEx(() -> b.put(pm, new Pos(0,0), Dir.UP, 0), IllegalArgumentException.class, "con n == 0"+nE);
                    String err = "se una posizione della linea non è nella board"+nE;
                    notEx(() -> b.put(pm, new Pos(0,0), Dir.UP, 15), IllegalArgumentException.class, err);
                    notEx(() -> b.put(pm, new Pos(3,0), Dir.DOWN, 2), IllegalArgumentException.class, err);
                    notEx(() -> b.put(pm, new Pos(10,0), Dir.UP, 1), IllegalArgumentException.class, err);
                } catch (Exception ex) { return err("Board.put(P pm,Pos p,Dir d,int n) "+ex.getMessage()); }
                try {
                    notEx(() -> new Action<>(null, pm), NullPointerException.class, "con p == null"+nE);
                    notEx(() -> new Action<>(new Pos(0,0), pmNull), NullPointerException.class, "con pm == null"+nE);
                } catch (Exception ex) { return err("Action(Pos p, P pm) "+ex.getMessage()); }
                Pos p = new Pos(0,0), p2 = new Pos(2,2), pNull = null;
                Action<PieceModel<Species>> af = new Action<>(p);
                try {
                    notEx(() -> af.pos.add(p), Exception.class, "lista pos è modificabile,"+nE);
                    notEx(Action::new, IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(new Pos[] {p,p}), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                    notEx(() -> new Action<>(new Pos[] {p,(Pos)null}), NullPointerException.class, "con pp che contiene elementi null"+nE);
                } catch (Exception ex) { return err("Action(Pos...pp) "+ex.getMessage()); }
                Action<PieceModel<Species>> af2 = new Action<>(Dir.RIGHT, 2, p);
                try {
                    notEx(() -> af2.pos.add(p), Exception.class, "la lista pos è modificabile,"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2, p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2, p, null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                    notEx(() -> new Action<>(null, 2, p), NullPointerException.class, "con d null"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 0, p), IllegalArgumentException.class, "con ns < 1"+nE);
                } catch (Exception ex) { return err("Action(Dir d,int ns,Pos...pp) "+ex.getMessage()); }
                Action<PieceModel<Species>> af3 = new Action<>(p, p2);
                try {
                    notEx(() -> af3.pos.add(p), Exception.class, "lista pos è modificabile,"+nE);
                    notEx(() -> new Action<>((Pos)null, p2), NullPointerException.class, "con p1 null"+nE);
                    notEx(() -> new Action<>(p, (Pos)null), NullPointerException.class, "con p2 null"+nE);
                    notEx(() -> new Action<>(p, p), IllegalArgumentException.class, "con p1 == p2"+nE);
                } catch (Exception ex) { return err("Action(Pos p1,Pos p2) "+ex.getMessage());}
                Action<PieceModel<Species>> af4 = new Action<>(pm, p);
                try {
                    notEx(() -> af4.pos.add(p), Exception.class, "lista pos è modificabile,"+nE);
                    notEx(() -> new Action<>(pmNull, p), NullPointerException.class, "con pm null"+nE);
                    notEx(() -> new Action<>(pm), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(pm, p, null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                    notEx(() -> new Action<>(pm, p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                } catch (Exception ex) { return err("Action(P pm,Pos...pp) "+ex.getMessage()); }
                Move.Kind kNull = null;
                Action<PieceModel<Species>> a = new Action<>(p, p2), a2 = new Action<>(new Pos(1,1));
                Move<PieceModel<Species>> m = new Move<>(Move.Kind.PASS);
                try {
                    notEx(() -> m.actions.add(a), Exception.class, "lista actions modificabile,"+nE);
                    notEx(() -> new Move<>(kNull), NullPointerException.class, "con k null"+nE);
                    notEx(() -> new Move<>(Move.Kind.ACTION), IllegalArgumentException.class, "con k == ACTION"+nE);
                } catch (Exception ex) { return new Result("ERRORE Move(Kind k) "+ex.getMessage()); }
                Move<PieceModel<Species>> mf = new Move<>(a, a2);
                try {
                    notEx(() -> mf.actions.add(a), Exception.class, "lista actions modificabile,"+nE);
                    notEx(() -> new Move<>(a, null), NullPointerException.class, "con aa che contiene un elemento null"+nE);
                    notEx(Move<PieceModel<Species>>::new, IllegalArgumentException.class, "con aa vuoto"+nE);
                } catch (Exception ex) { return err("Move(Action<P>...aa) "+ex.getMessage()); }
                List<Action<PieceModel<Species>>> aa = Arrays.asList(a, a2), aaNull = null;
                Move<PieceModel<Species>> mf2 = new Move<>(aa);
                aa.set(0,a2);
                try {
                    notEx(() -> mf2.actions.add(a), Exception.class, "lista actions modificabile,"+nE);
                    notEx(() -> new Move<>(new ArrayList<>()), IllegalArgumentException.class, "con aa vuota"+nE);
                    notEx(() -> new Move<>(aaNull), NullPointerException.class, "con aa null"+nE);
                    notEx(() -> new Move<>(Arrays.asList(a, null, a2)), NullPointerException.class, "con aa che contiene un elemento null"+nE);
                } catch (Exception ex) { return err("Move(List<Action<P>> aa) "+ex.getMessage()); }
                DummyGU gR = new DummyGU("a", "b");
                Set<Move<PieceModel<Species>>> vm = gR.validMoves();
                Move<PieceModel<Species>> last = null;
                for (Move<PieceModel<Species>> m2 : vm) last = m2;
                vm = gR.validMoves(last.actions.get(0).pos.get(0));
                if (vm == null || vm.size() != 1 || !vm.contains(last)) return err("validMoves(p)");
                try {
                    Set<Move<PieceModel<Species>>> vm2 = vm;
                    Move<PieceModel<Species>> m3 = last;
                    notEx(() -> vm2.add(m3), Exception.class, "insieme validMoves(p) modificabile,"+nE);
                    notEx(() -> gR.isValid(null), NullPointerException.class, "isValid(null)"+nE);
                    notEx(() -> gR.validMoves(null), NullPointerException.class, "validMoves(null)"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
                BoardOct<PieceModel<Species>> b88 = new BoardOct<>(8, 8);
                List<Pos> lp = b88.positions();
                try {
                    notEx(() -> lp.add(p2), Exception.class, "lista positions() è modificabile,"+nE);
                    notEx(() -> b88.adjacent(pNull, Dir.UP), NullPointerException.class, "adjacent(null,d)"+nE);
                    notEx(() -> b88.adjacent(new Pos(0,0), null), NullPointerException.class, "adjacent(p,null)"+nE);
                    notEx(() -> b88.put(pm, new Pos(10,10)), IllegalArgumentException.class, "put(pm,(10,10))"+nE);
                } catch (Exception ex) { return err("BoardOct(8,8): "+ex.getMessage()); }
                Board<PieceModel<Species>> ub = Utils.UnmodifiableBoard(new BoardOct<>(8,8));
                try {
                    notEx(() -> ub.put(pm, p), UnsupportedOperationException.class, "put(pm,p)"+nE);
                    notEx(() -> ub.put(pm, p, Dir.UP, 1), UnsupportedOperationException.class, "put(pm,p,UP,1)"+nE);
                    notEx(() -> ub.remove(p), UnsupportedOperationException.class, "remove(p)"+nE);
                } catch (Exception ex) { return err("UnmodifiableBoard: "+ex.getMessage()); }
                DummyGUFactory gF = new DummyGUFactory();
                Player<PieceModel<Species>> py1 = new RandPlayer<>("A");
                Player<PieceModel<Species>> py2 = new RandPlayer<>("B");
                if (!Objects.equals(py1.name(), "A")) return err("Player name()");
                try {
                    notEx(() -> new RandPlayer<PieceModel<Species>>(null), NullPointerException.class, "new RandPlayer(null)"+nE);
                    notEx(() -> Utils.play(null,py1,py2), NullPointerException.class, "Utils.play(null,p1,p2)"+nE);
                    notEx(() -> Utils.play(gF,py1), IllegalArgumentException.class, "Utils.play(gf,p1) quando il gioco richiede 2 giocatori"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    /******************************************************************************/
    /**************************   G R A D E  3   **********************************/
    /******************************************************************************/

    private static boolean gradeTest3(boolean ok) {
        if (ok) ok = test_DGameInit(0.4f, 200, "Othello", 10,10, () -> new Othello(-1,10,PP[0],PP[1]),
                new int[]{5,5, 4,4}, new int[]{4,5, 5,4});
        if (ok) ok = test_DGame(0.5f, 200, "Othello", 12,12, "4", G_OTH12, () -> new Othello(-1,12,PP[0],PP[1]));
        if (ok) ok = test_DGameUnMove(0.8f, 400, "Othello", 6,6, G_OTH6, () -> new Othello(-1,6,PP[0],PP[1]));
        if (ok) ok = test_DGameCopy(0.8f, 200, "Othello", 12,12, G_OTH12, () -> new Othello(-1,12,PP[0],PP[1]));
        if (ok) ok = test_DGame(0.4f, 200, "Othello from factory", 12,12, "5", G_OTH12,
                () -> {
                    GameFactory<GameRuler<PieceModel<Species>>> gF = new OthelloFactory();
                    gF.params().get(1).set("12x12");
                    gF.setPlayerNames(PP[0],PP[1]);
                    return gF.newGame(); });
        if (ok) ok = test_DGameMechanics(1.8f, 4000, "Othello", new int[] {6,6, 8,8, 10,10, 12,12},
                (t,w,h) -> new Othello(t, w, PP[0],PP[1]));
        if (ok) ok = test_DGameFactoryParams(1.8f, 200, "OthelloFactory",
                (pp,start) -> {
                    if (pp.size() != 2) return "params().size() != 2";
                    String err = testParam(pp.get(0), "Time", (start ? "No limit" : null), TIMES);
                    if (err != null) return err;
                    return testParam(pp.get(1), "Board", (start ? "8x8" : null), SIZES);
                },
                (pp,gR) -> {
                    String err = "Othello ";
                    if (!checkTime(gR, pp.get(0))) return err+"time";
                    int index = SIZES.indexOf(pp.get(1).get());
                    if (index < 0) return "valore errato parametro Board";
                    int wh = TO_SIZE[index];
                    if (gR.getBoard().width() != wh || gR.getBoard().height() != wh) return err+"board size";
                    String[] pn = {"Time","Board"};
                    String[] tb = new String[2];
                    for (int i = 0 ; i < 2 ; i++) {
                        tb[i] = paramGet(pp.get(i), String.class, "");
                        if (tb[i].isEmpty()) return "parametro "+pn[i]+": get() non ritorna una String";
                        String e = testGetParam(gR, pn[i], tb[i], String.class);
                        if (e != null) return err+e;
                    }
                    return null;
                },
                OthelloFactory::new);
        if (ok) ok = test_DGameInit(0.5f, 200, "MNKgame", 5,4, () -> new MNKgame(-1,5,4,4,PP[0],PP[1]), null, null);
        if (ok) ok = test_DGame(1.0f, 200, "MNKgame", 3,3, "1", G_333, () -> new MNKgame(-1,3,3,3,PP[0],PP[1]));
        if (ok) ok = test_DGame(2.0f, 200, "MNKgame", 6,5, "2", G_655, () -> new MNKgame(-1,6,5,5,PP[0],PP[1]));
        if (ok) ok = test_DGame(1.0f, 200, "MNKgame", 5,7, "3", G_573, () -> new MNKgame(-1,5,7,3,PP[0],PP[1]));
        if (ok) ok = test_DGameUnMove(1.0f, 200, "MNKgame", 6,5, G_655, () -> new MNKgame(-1,6,5,5,PP[0],PP[1]));
        if (ok) ok = test_DGameCopy(1.0f, 200, "MNKgame", 5,7, G_573, () -> new MNKgame(-1,5,7,3,PP[0],PP[1]));
        if (ok) ok = test_DGame(1.0f, 200, "MNKgame from factory", 6,5, "4", G_655,
                () -> {
                    GameFactory<GameRuler<PieceModel<Species>>> gF = new MNKgameFactory();
                    gF.params().get(1).set(6);
                    gF.params().get(2).set(5);
                    gF.params().get(3).set(5);
                    gF.setPlayerNames(PP[0],PP[1]);
                    return gF.newGame(); });
        if (ok) ok = test_DGameMechanics(3.0f, 6000, "MNKgame", new int[] {3,4, 5,3, 7,7, 5,9},
                (t,w,h) -> new MNKgame(t, w, h, Math.max(w,h), PP[0],PP[1]),
                (t,w,h) -> new MNKgame(t, w, h, 3+(Math.max(w,h)-3)/2, PP[0],PP[1]));
        if (ok) ok = test_DGameFactoryParams(3.0f, 200, "MNKgameFactory",
                (pp,start) -> {
                    if (pp.size() != 4) return "params().size() != 4";
                    String err = testParam(pp.get(0), "Time", (start ? "No limit" : null), TIMES);
                    if (err != null) return err;
                    String[] pn = {"M","N","K"};
                    int[] mnk = new int[3];
                    for (int i = 0; i < 3 ; i++) {
                        mnk[i] = paramGet(pp.get(i+1), Integer.class, 0);
                        if (mnk[i] == 0) return "parametro "+pn[i]+": get() non ritorna un Integer";
                    }
                    err = testParam(pp.get(1), "M", (start ? 3 : null), range(mnk[1] >= mnk[2] ? 1 : mnk[2],20));
                    if (err != null) return err;
                    err = testParam(pp.get(2), "N", (start ? 3 : null), range(mnk[0] >= mnk[2] ? 1 : mnk[2],20));
                    if (err != null) return err;
                    return testParam(pp.get(3), "K", (start ? 3 : null), range(1,Math.max(mnk[0],mnk[1])));
                },
                (pp,gR) -> {
                    String err = "MNKgame ";
                    if (!checkTime(gR, pp.get(0))) return err+"time";
                    String[] pn = {"M","N","K"};
                    int[] mnk = new int[3];
                    for (int i = 0; i < 3 ; i++) {
                        mnk[i] = paramGet(pp.get(i+1), Integer.class, 0);
                        if (mnk[i] == 0) return "parametro "+pn[i]+": get() non ritorna un Integer";
                        String e = testGetParam(gR, pn[i], mnk[i], Integer.class);
                        if (e != null) return err+e;
                    }
                    if (gR.getBoard().width() != mnk[0] || gR.getBoard().height() != mnk[1])
                        return err+"board size";
                    return null;
                },
                MNKgameFactory::new);
        return ok;
    }


    private static boolean test_DGameMechanics(float sc, int ms, String gN, int[] wh, GRC...gCs) {
        String sizes = "";
        for (int i = 0 ; i < wh.length ; i+=2) sizes += (i == 0 ? "" : " ")+wh[i]+"x"+wh[i+1];
        return runTest("Test "+gN+" Mechanics "+sizes, sc, ms, () -> {
            try {
                PieceModel<Species> pN = new PieceModel<>(Species.DISC, C1);
                Set<PieceModel<Species>> pcs = new HashSet<>(Arrays.asList(pN,
                        new PieceModel<>(Species.DISC, C2)));
                long[] millis = {1000, 2000, 3000, 60_000};
                for (int i = 0 ; i < wh.length ; i+=2) {
                    long tm = millis[(i / 2) % millis.length];
                    for (GRC gC : gCs) {
                        GameRuler<PieceModel<Species>> gR = gC.get(tm, wh[i], wh[i+1]);
                        Mechanics<PieceModel<Species>> mc = gR.mechanics();
                        if (mc == null) return err("null");
                        if (mc.time != tm) return err("time");
                        if (mc.np != 2) return err("np");
                        List<PieceModel<Species>> pp = mc.pieces;
                        if (pp == null || !new HashSet<>(pp).equals(pcs)) return err("pieces");
                        if (!Objects.equals(mc.positions, gR.getBoard().positions())) return err("positions");
                        if (!Objects.equals(mc.start, toSituation(gR))) return err("start");
                        try {
                            notEx(() -> mc.pieces.set(0, pN), Exception.class, "pieces modificabile,"+nE);
                            notEx(() -> mc.positions.set(0, new Pos(0, 0)), Exception.class, "positions modificabile,"+nE);
                        } catch (Exception ex) { return err(ex.getMessage()); }
                        Random rnd = new Random(234652);
                        String err = testNext(mc.next, mc.start, rnd);
                        if (err != null) return err(err);
                    }
                }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_DGameFactoryParams(float sc, int ms, String fN,
                                                   BiFunction<List<Param<?>>,Boolean,String> testP,
                                                   BiFunction<List<Param<?>>,GameRuler<?>,String> testG,
                                                   Supplier<GameFactory<GameRuler<PieceModel<Species>>>> fC) {
        return runTest("Test "+fN+" e Params", sc, ms, () -> {
            try {
                GameFactory<GameRuler<PieceModel<Species>>> gF = fC.get();
                List<Param<?>> pp0 = gF.params();
                if (pp0 == null) return err("params() ritorna null");
                String err = testP.apply(pp0, true);
                if (err != null) return err(err);
                gF.setPlayerNames(PP);
                GameRuler<PieceModel<Species>> gR = gF.newGame();
                if (gR == null) return err("newGame() ritorna null");
                err = testG.apply(pp0, gR);
                if (err != null) return err(err);
                try {
                    notEx(() -> pp0.set(0, DUMMYPARAM), Exception.class, "lista params() modificabile,"+nE);
                    for (Param<?> p : pp0)
                        notEx(() -> {p.values().add(0,null); p.values().set(0,null);}, Exception.class, "lista values() modificabile,"+nE);
                } catch (Exception ex) { return err(ex.getMessage()); }
                for (int i = 0 ; i < 5 ; i++) {
                    List<Param<?>> pp = pp0;
                    for (Param<?> p : pp) {
                        err = testParam(p);
                        if (err != null) return err(err);
                    }
                    gR = gF.newGame();
                    err = testP.apply(pp, false);
                    if (err != null) return err(err);
                    if (gR == null) return err("newGame() ritorna null");
                    err = testG.apply(pp, gR);
                }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }



    /****************************  TEST GAME UTILITIES  ***********************/

    private static final List<String> TIMES = Arrays.asList("No limit","1s","2s","3s","5s","10s","20s","30s","1m","2m","5m");
    private static final List<String> SIZES = Arrays.asList("6x6","8x8","10x10","12x12");
    private static final long[] TO_MS = {-1,1000,2000,3000,5000,10_000,20_000,30_000,60_0000,120_000,300_000};
    private static final int[] TO_SIZE = {6,8,10,12};
    private static final Param<?> DUMMYPARAM = new Param() {
        @Override
        public String name() { return null; }
        @Override
        public String prompt() { return null; }
        @Override
        public List values() { return null; }
        @Override
        public void set(Object v) { }
        @Override
        public Object get() { return null; }
    };

    private static List<Integer> range(int min, int max) {
        List<Integer> r = new ArrayList<>();
        for (int i = min ; i <= max ; i++) r.add(i);
        return r;
    }

    private static <T> T paramGet(Param<?> p, Class<T> c, T fail) {
        try {
            return c.cast(p.get());
        } catch (Exception e) {
            //System.err.println("get() "+p.get());
            return fail; }
    }

    private static <T> String testParam(Param<?> p, String name, T val, List<T> vv) {
        String err = "parametro "+name+" ";
        if (p == null) return err+"null";
        if (!name.equals(p.name())) return err+"name()";
        List<?> values = p.values();
        if (values == null) return err+"values() null";
        if (!values.equals(vv)) return err+"lista values() errata";
        if (val != null && !val.equals(p.get())) return err+"get() errato";
        return null;
    }

    private static String testParam(Param<?> p) {
        String err = "parametro "+p.name()+" ";
        List<?> values = p.values();
        Object other = values.get((values.indexOf(p.get()) + 1)%values.size());
        try {
            p.set(other);
        } catch (Exception e) { return err+"set(v) con v in values() lancia eccezione"; }
        if (!Objects.equals(other, p.get())) return err+"get() != v dopo set(v)";
        try {
            notEx(() -> p.set(null), IllegalArgumentException.class, err+"set(null)"+nE);
            notEx(() -> p.set("INVALIDO"), IllegalArgumentException.class, err+"set(x) con x non in values()"+nE);
        } catch (Exception ex) { return ex.getMessage(); }
        if (!Objects.equals(other, p.get())) return err+"get() != valore corretto";
        return null;
    }

    private static boolean checkTime(GameRuler<?> gR, Param<?> time) {
        if (time == null) return false;
        Mechanics<?> m = gR.mechanics();
        int index = TIMES.indexOf(time.get());
        if (index < 0 || m == null || m.time != TO_MS[index]) return false;
        return true;
    }

    private static <T> String testGetParam(GameRuler<?> gR, String name, T val, Class<T> c) {
        if (!val.equals(gR.getParam(name, c))) return "getParam(name,c) valore errato";
        try {
            notEx(() -> gR.getParam(name+"ZZ", c), IllegalArgumentException.class, "getParam(name,c) con name errato"+nE);
            notEx(() -> gR.getParam(name, Move.class), ClassCastException.class, "getParam(name,c) con c errato"+nE);
        } catch (Exception ex) { return ex.getMessage(); }
        return null;
    }

    private static <P> String testNext(Next<P> next, Situation<P> start, Random rnd) {
        class MS {
            final Move<P> m;
            final Situation<P> s;
            MS(Map.Entry<Move<P>,Situation<P>> e) { this.m = e.getKey(); this.s = e.getValue(); }
        }
        String err = "next";
        if (next == null) return err;

        int max = 70, count = 3;
        while (count > 0) {
            List<MS> ro = new ArrayList<>();
            Situation<P> s = start;
            int k = max;
            while (s.turn > 0 && k > 0) {
                Map<Move<P>,Situation<P>> nextM = next.get(s);
                Map.Entry<Move<P>,Situation<P>> e = choose(nextM.entrySet(), rnd::nextInt);
                ro.add(new MS(e));
                if (e != null) s = e.getValue();
                else return "mappa ritornata da next modificata";
                k--;
            }
            s = start;
            for (MS e : ro) {
                Map<Move<P>,Situation<P>> nextM = next.get(s);
                if (!nextM.containsKey(e.m)) return err+" [m "+count+"]";
                if (!Objects.equals(nextM.get(e.m), e.s)) return err+" [s "+count+"]";
                s = e.s;
            }
            count--;
        }
        return null;
    }

    private static <T> T choose(Collection<T> c, Function<Integer,Integer> rnd) {
        int r = rnd.apply(c.size());   // Un intero compreso tra 0 e c.size()-1
        for (Iterator<T> i = c.iterator() ; i.hasNext() ; r--) {
            T v = i.next();
            if (r == 0) return v;
        }
        return null;
    }

    private static <K,V> Map<K,V> getMap(K[] keys, V[] values) {
        Map<K,V> map = new HashMap<>();
        for (int i = 0 ; i < keys.length ; i++)
            map.put(keys[i], values[i]);
        return map;
    }

    private static <P> Situation<P> toSituation(GameRuler<P> gR) {
        Map<Pos,P> c = new HashMap<>();
        Board<P> b = gR.getBoard();
        for (Pos p : b.positions())
            if (b.get(p) != null) c.put(p, b.get(p));
        return new Situation<>(c, gR.turn());
    }

    private static Move<PieceModel<Species>> stringToMove(Set<Move<PieceModel<Species>>> vms, String s) {
        if (s.equals("RESIGN")) return new Move<>(Move.Kind.RESIGN);
        String c = ""+s.charAt(0);
        s = s.replaceFirst(c, "").replace(c, ",");
        List<Pos> cc = new ArrayList<>();
        Integer b = null;
        for (String x : s.split(",")) {
            if (b == null) b = Integer.parseInt(x);
            else {
                cc.add(new Pos(b, Integer.parseInt(x)));
                b = null;
            }
        }
        PieceModel<Species> pm = new PieceModel<>(Species.DISC, (c.equals("X") ? C1 : C2));
        Action<PieceModel<Species>> add = new Action<>(cc.get(0), pm);
        cc.remove(0);
        Move<PieceModel<Species>> m;
        if (cc.size() > 0) {
            Action<PieceModel<Species>> swap = new Action<>(pm, cc.toArray(new Pos[cc.size()]));
            m = new Move<>(add, swap);
        } else m = new Move<>(add);
        if (vms == null) return m;
        for (Move<PieceModel<Species>> vm : vms)
            if (Objects.equals(vm, m)) {
                m = vm;
                break;
            }
        return m;
    }

    private static <P> boolean eqBoard(Board<P> board, String sb) {
        PieceModel<Species> pN = new PieceModel<>(Species.DISC, C1),
                pB = new PieceModel<>(Species.DISC, C2);
        int k = 0;
        for (int b = 0 ; b < board.width() ; b++)
            for (int t = 0 ; t < board.height() ; t++) {
                char c = sb.charAt(k++);
                P pm = board.get(new Pos(b, t));
                switch (c) {
                    case '.':
                        if (pm != null) return false;
                        break;
                    case 'X':
                        if (!pN.equals(pm)) return false;
                        break;
                    case 'O':
                        if (!pB.equals(pm)) return false;
                        break;
                }
            }
        return true;
    }

    private static boolean checkValidMoves(Set<Move<PieceModel<Species>>> vm, String svm) {
        String[] mm = svm.split("\\s+");
        if (vm == null || vm.size() != mm.length) return false;
        for (String sm : mm) {
            Move<PieceModel<Species>> m = stringToMove(null, sm);
            boolean found = false;
            for (Move<PieceModel<Species>> cm : vm) {
                found = Objects.equals(m, cm);
                if (found) break;
            }
            if (!found) return false;
        }
        return true;
    }

    private static boolean checkDGameBoard(Board<PieceModel<Species>> board, int w, int h, int[] nn, int[] bb) {
        Set<Pos> nSet = arrayToSetPos(nn), bSet = arrayToSetPos(bb);
        PieceModel<Species> pN = new PieceModel<>(Species.DISC, C1),
                pB = new PieceModel<>(Species.DISC, C2);
        for (int i = 0 ; i < w ; i++)
            for (int j = 0 ; j < h ; j++) {
                Pos p = new Pos(i, j);
                PieceModel<Species> pm = board.get(p);
                if (nSet.contains(p)) {
                    if (!pN.equals(pm)) return false;
                } else if (bSet.contains(p)) {
                    if (!pB.equals(pm)) return false;
                } else if (pm != null)
                    return false;
            }
        return true;
    }

    private static Set<Pos> arrayToSetPos(int[] pp) {
        Set<Pos> sp = new HashSet<>();
        for (int i = 0 ; i < (pp != null ? pp.length : 0) ; i+=2)
            sp.add(new Pos(pp[i], pp[i+1]));
        return sp;
    }

    private static final String G_OTH8_1 = "...........................XO......OX...........................\n" +
            "X2,4X3,4 X5,3X4,3 X3,5X3,4 X4,2X4,3 RESIGN\n" +
            "1 X2,4X3,4\n" +
            "....................X......XX......OX...........................\n" +
            "O2,3O3,3 O4,5O4,4 O2,5O3,4 RESIGN\n" +
            "2 O2,5O3,4\n" +
            "....................XO.....XO......OX...........................\n" +
            "X5,3X4,3 X3,5X3,4 X4,2X4,3 X2,6X2,5 RESIGN\n" +
            "1 X5,3X4,3\n" +
            "....................XO.....XO......XX......X....................\n" +
            "O5,2O4,3 O5,4O4,4 O3,2O3,3 O1,4O2,4 RESIGN O2,3O2,4\n" +
            "2 O1,4O2,4\n" +
            "............O.......OO.....XO......XX......X....................\n" +
            "X3,5X3,4 X1,6X3,4,2,5 X1,5X2,4 X0,4X3,4,2,4,1,4 RESIGN\n" +
            "1 X0,4X3,4,2,4,1,4\n" +
            "....X.......X.......XO.....XX......XX......X....................\n" +
            "O0,3O1,4 O5,2O4,3,3,4 RESIGN O2,3O2,4\n" +
            "2 O2,3O2,4\n" +
            "....X.......X......OOO.....XX......XX......X....................\n" +
            "X3,2X2,3 X3,6X2,5 X1,3X2,3 X1,5X2,4 X1,6X2,5 X1,2X2,3 RESIGN\n" +
            "1 X1,3X2,3\n" +
            "....X......XX......XOO.....XX......XX......X....................\n" +
            "O4,2O3,3 O5,4O4,4,3,4 O0,2O1,3 O0,3O1,4 O5,2O4,3,3,4 RESIGN O2,2O2,3\n" +
            "2 O2,2O2,3\n" +
            "....X......XX.....OOOO.....XX......XX......X....................\n" +
            "X3,5X2,4 X3,2X2,3 X3,6X2,5 X1,1X2,2 X3,1X2,2 X1,5X2,4 X1,6X2,5 X1,2X2,3 RESIGN\n" +
            "1 X1,2X2,3\n" +
            "....X.....XXX.....OXOO.....XX......XX......X....................\n" +
            "O4,2O3,3 O5,5O3,3,4,4 O5,4O4,4,3,4 O0,3O1,4 O0,2O1,2,1,3 O5,2O4,3,3,4 RESIGN\n" +
            "2 RESIGN\n" +
            "1";
    private static final String G_OTH8_2 = "...........................XO......OX...........................\n" +
            "X2,4X3,4 X5,3X4,3 X3,5X3,4 X4,2X4,3 RESIGN\n" +
            "1 X3,5X3,4\n" +
            "...........................XXX.....OX...........................\n" +
            "O2,3O3,3 O4,5O4,4 O2,5O3,4 RESIGN\n" +
            "2 O2,5O3,4\n" +
            ".....................O.....XOX.....OX...........................\n" +
            "X2,4X3,4 X5,3X4,3 X4,2X4,3 X1,5X2,5 RESIGN\n" +
            "1 X2,4X3,4\n" +
            "....................XO.....XXX.....OX...........................\n" +
            "O4,5O4,4,3,5 O2,3O3,3,2,4 RESIGN\n" +
            "2 O2,3O3,3,2,4\n" +
            "...................OOO.....OXX.....OX...........................\n" +
            "X3,2X3,3 X2,2X3,3 X4,2X4,3 X5,2X4,3 X1,4X2,4 X1,5X2,5 X1,6X2,5 X1,2X2,3 X1,3X2,4 RESIGN\n" +
            "1 X1,2X2,3\n" +
            "..........X........XOO.....OXX.....OX...........................\n" +
            "O4,5O4,4,3,5 O5,5O4,4 O5,4O4,4,3,4 O3,6O3,4,3,5 O4,6O3,5 RESIGN O1,3O2,3 O2,2O2,3\n" +
            "2 O4,5O4,4,3,5\n" +
            "..........X........XOO.....OXO.....OOO..........................\n" +
            "X3,2X3,3 X3,6X3,5 X5,3X4,3,3,3 X5,2X4,3 X5,4X4,4 X5,6X4,5 X1,4X2,4 X2,6X2,4,2,5 X1,6X2,5 RESIGN\n" +
            "1 X5,6X4,5\n" +
            "..........X........XOO.....OXO.....OOX........X.................\n" +
            "O5,5O4,5 O4,6O4,5 RESIGN O1,3O2,3 O2,2O2,3\n" +
            "2 O5,5O4,5\n" +
            "..........X........XOO.....OXO.....OOO.......OX.................\n" +
            "X3,2X3,3 X3,6X3,5 X5,3X4,3,3,3 X5,2X4,3 X1,4X2,4 X2,6X2,4,2,5 X1,6X2,5 X5,4X5,5,4,4 RESIGN\n" +
            "1 X5,4X5,5,4,4\n" +
            "..........X........XOO.....OXO.....OXO......XXX.................\n" +
            "O6,3O5,4 O6,7O5,6 O5,3O4,4 O0,1O3,4,2,3,1,2 O6,5O5,4,5,5 O6,6O4,4,5,5 O6,4O5,4,4,4,3,4 RESIGN O1,3O2,3 O2,2O2,3\n" +
            "2 O6,4O5,4,4,4,3,4\n" +
            "..........X........XOO.....OOO.....OOO......OXX.....O...........\n" +
            "X2,2X4,4,3,3 X7,3X6,4 X2,6X2,4,2,5 X1,5X4,5,3,5,2,5 X5,3X5,4,4,3,3,3 RESIGN\n" +
            "1 X5,3X5,4,4,3,3,3\n" +
            "..........X........XOO.....XOO.....XOO.....XXXX.....O...........\n" +
            "O6,5O5,5 O6,6O5,5 O6,2O5,3 O6,3O5,4 O6,7O5,6 O4,2O4,3,3,3,5,3 O5,2O4,3 O0,1O2,3,1,2 O4,6O5,5 O3,2O3,3 O2,2O3,3,2,3 RESIGN\n" +
            "2 O4,2O4,3,3,3,5,3\n" +
            "..........X........XOO.....OOO....OOOO.....OXXX.....O...........\n" +
            "X3,2X4,3 X1,4X4,4,3,4,2,4 X5,2X5,3 X3,6X4,5 X2,2X4,4,3,3 X7,3X6,4 X2,6X2,4,2,5 X7,4X6,4 X1,5X4,5,3,5,2,5 X6,3X4,3,3,3,5,3 RESIGN\n" +
            "1 X1,4X4,4,3,4,2,4\n" +
            "..........X.X......XXO.....OXO....OOXO.....OXXX.....O...........\n" +
            "O6,3O5,4 O6,7O5,6 O6,6O4,4,5,5 O1,3O2,3,2,4 O1,5O2,4 O5,7O5,4,5,5,5,6 O0,1O3,4,2,3,1,2 O6,5O5,4,5,5 O0,3O1,4 O4,6O5,5 O2,2O2,3,2,4 O0,4O5,4,4,4,3,4,2,4,1,4 RESIGN\n" +
            "2 O0,4O5,4,4,4,3,4,2,4,1,4\n" +
            "....O.....X.O......XOO.....OOO....OOOO.....OOXX.....O...........\n" +
            "X2,2X4,4,3,3 X0,5X1,4 X7,3X6,4 X2,6X2,4,2,5 X5,2X5,4,5,3 X1,5X4,5,3,5,2,5 X6,3X4,3,3,3,5,3 RESIGN\n" +
            "1 X6,3X4,3,3,3,5,3\n" +
            "....O.....X.O......XOO.....XOO....OXOO.....XOXX....XO...........\n" +
            "O6,5O5,5 O6,6O5,5 O6,7O5,6 O3,2O4,3,3,3,2,3 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O5,7O5,5,5,6 O0,1O2,3,1,2 O4,6O5,5 O2,2O3,3,2,3 RESIGN\n" +
            "2 O3,2O4,3,3,3,2,3\n" +
            "....O.....X.O......OOO....OOOO....OOOO.....XOXX....XO...........\n" +
            "X3,1X4,2 X6,5X6,4 X1,3X4,3,3,3,2,3 X3,6X5,4,4,5 X2,2X4,4,3,3 X2,6X4,4,3,5 X7,3X6,4 X7,5X6,4 X1,5X4,5,3,5,2,5 RESIGN\n" +
            "1 X1,3X4,3,3,3,2,3\n" +
            "....O.....XXO......XOO....OXOO....OXOO.....XOXX....XO...........\n" +
            "O6,5O5,5 O6,6O5,5 O6,7O5,6 O5,7O5,5,5,6 O2,2O3,3,2,3,1,3 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O0,1O2,3,1,2 O0,2O1,3 O1,1O1,2,1,3 O4,6O5,5 RESIGN\n" +
            "2 O2,2O3,3,2,3,1,3\n" +
            "....O.....XOO.....OOOO....OOOO....OXOO.....XOXX....XO...........\n" +
            "X2,1X3,2 X3,6X5,4,4,5 X4,6X4,4,4,5 X2,6X4,4,3,5 X4,1X4,2 X1,5X4,5,1,3,3,5,1,4,2,5 X1,1X4,4,3,3,2,2 X5,2X3,2,2,2,4,2 X0,3X3,3,2,3,1,3 X3,1X4,2 X7,3X6,4 X6,5X5,4,6,4 X1,6X3,4,2,5 X7,5X6,4 RESIGN\n" +
            "1 X0,3X3,3,2,3,1,3\n" +
            "...XO.....XXO.....OXOO....OXOO....OXOO.....XOXX....XO...........\n" +
            "O6,5O5,5 O6,6O5,5 O6,7O5,6 O7,2O6,3 O5,2O4,3,5,3 O6,2O6,3,5,3 O5,7O5,5,5,6 O0,1O2,3,1,2 O0,2O1,2,1,3,0,3 O1,1O1,2,1,3 O4,6O5,5 RESIGN\n" +
            "2 O0,2O1,2,1,3,0,3\n" +
            "..OOO.....OOO.....OXOO....OXOO....OXOO.....XOXX....XO...........\n" +
            "X2,6X4,4,2,4,3,5,2,5 X3,1X3,2,4,2 X4,6X4,4,4,5 X3,6X5,4,3,4,4,5,3,5 X1,5X4,5,3,5,2,4,2,5 X1,1X2,2 X2,1X3,2,2,2 X5,1X4,2 X4,1X3,2,4,2 X0,5X1,4 X7,3X6,4 X6,5X5,4,6,4 X1,6X3,4,2,5 X7,5X6,4 X0,1X1,2 RESIGN\n" +
            "1 X6,5X5,4,6,4\n" +
            "..OOO.....OOO.....OXOO....OXOO....OXOO.....XXXX....XXX..........\n" +
            "O6,6O5,5 O6,2O5,3 O6,7O5,6 O7,5O6,5,5,5,5,3,6,4 O5,2O4,3 O7,2O5,4,6,3 O7,4O5,4,6,4 O7,6O4,3,5,4,6,5 RESIGN O7,3O4,3,3,3,2,3,6,3,5,3\n" +
            "2 O7,3O4,3,3,3,2,3,6,3,5,3\n" +
            "..OOO.....OOO.....OOOO....OOOO....OOOO.....OXXX....OXX.....O....\n" +
            "X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X3,6X4,5 X6,2X6,3 X3,1X5,3,4,2 X7,2X6,3 X1,1X4,4,3,3,2,2 X1,5X4,5,3,5,2,5 RESIGN\n" +
            "1 X3,6X4,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOX.....OXXX....OXX.....O....\n" +
            "O5,7O5,4,5,5,5,6 O6,6O6,5,5,5,6,4 O2,7O5,4,4,5,3,6 O6,7O4,5,5,6 O3,7O3,6 O7,4O5,4,6,4 O7,5O6,5,5,5,4,5,6,4 O7,6O5,4,6,5 O4,6O5,5,4,5,6,4 O4,7O3,6 RESIGN\n" +
            "2 O7,5O6,5,5,5,4,5,6,4\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOO.....O.O..\n" +
            "X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X7,6X6,5 X7,2X6,3 X3,1X3,2,3,3,3,4,3,5 RESIGN X7,4X6,5,6,4\n" +
            "1 X7,6X6,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOX.....O.OX.\n" +
            "O6,7O5,6 O6,6O6,5 O7,7O7,6 O3,7O3,6 O5,7O5,6 O4,7O3,6 RESIGN O2,7O3,6\n" +
            "2 O6,6O6,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OOOO....O.OX.\n" +
            "X7,4X6,5,7,5,6,4 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X7,2X6,3 X3,1X3,2,3,3,3,4,3,5 RESIGN\n" +
            "1 X7,4X6,5,7,5,6,4\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OXXO....OXXX.\n" +
            "O6,7O5,6 O3,7O3,6 O4,6O5,6 O5,7O5,6 O7,7O7,6,7,4,7,5 O4,7O3,6 RESIGN O2,7O3,6\n" +
            "2 O7,7O7,6,7,4,7,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOO.....OXOX....OXXO....OOOOO\n" +
            "X6,7X6,6 X3,1X3,2,3,3,3,4,3,5,5,3,4,2 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X5,2X5,3 X6,2X6,3 X4,6X5,5 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
            "1 X4,6X5,5\n" +
            "..OOO.....OOO.....OOOO....OOOOX...OOOOX....OXXX....OXXO....OOOOO\n" +
            "O2,6O5,6,4,6,3,6 O5,7O5,4,5,5,5,6,4,6 O6,7O5,6 O3,7O5,5,4,6,3,6,6,4 O4,7O6,5,5,6,4,6,3,6 RESIGN O2,7O3,6\n" +
            "2 O3,7O5,5,4,6,3,6,6,4\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOX....OOXO....OOOOO\n" +
            "X6,7X6,6 X6,2X6,3,6,4 X0,1X4,5,3,4,2,3,1,2 X2,1X4,3,3,2 X2,6X4,6,3,6 X2,7X4,5,3,6 X5,2X5,3 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
            "1 X6,7X6,6\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOX....OOXXX...OOOOO\n" +
            "O5,7O6,6,5,6,6,7 O4,7O6,5,5,6 RESIGN\n" +
            "2 O5,7O6,6,5,6,6,7\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO....OXOOO...OOXOO...OOOOO\n" +
            "X6,2X6,3,6,4 X2,1X4,3,3,2 X2,7X4,5,3,6 X4,7X5,6 X5,2X5,3 X7,2X6,3 X1,5X5,5,4,5,3,5,2,5 RESIGN\n" +
            "1 X5,2X5,3\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO...XXXOOO...OOXOO...OOOOO\n" +
            "O4,1O5,2 O5,1O5,4,5,2,5,3 O6,1O5,2 O6,2O5,2,5,3 RESIGN\n" +
            "2 O5,1O5,4,5,2,5,3\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO...OOXOO...OOOOO\n" +
            "X6,2X6,3,6,4 X4,7X5,6 X1,5X5,5,4,5,3,5,2,5 X2,1X5,4,4,3,3,2 RESIGN\n" +
            "1 X6,2X6,3,6,4\n" +
            "..OOO.....OOO.....OOOO....OOOOOO..OOOOO..OOOOOOO..XXXXOO...OOOOO\n" +
            "O7,2O6,2,6,3 O7,1O6,2 O6,1O6,5,6,2,6,3,6,4 RESIGN\n" +
            "2 O6,1O6,5,6,2,6,3,6,4\n" +
            "2";

    private static final String G_OTH6 = "..............XO....OX..............\n" +
            "X3,1X3,2 X2,4X2,3 X4,2X3,2 X1,3X2,3 RESIGN\n" +
            "1 X1,3X2,3\n" +
            ".........X....XX....OX..............\n" +
            "O3,4O3,3 RESIGN O1,2O2,2 O1,4O2,3\n" +
            "2 O3,4O3,3\n" +
            ".........X....XX....OOO.............\n" +
            "X4,5X3,4 X4,1X3,2 X4,3X3,3 X4,2X3,2 X4,4X3,3 RESIGN\n" +
            "1 X4,2X3,2\n" +
            ".........X....XX....XOO...X.........\n" +
            "O5,1O4,2 O0,3O2,3,1,3 O1,2O2,3 RESIGN O3,1O3,2 O1,1O2,2\n" +
            "2 O1,1O2,2\n" +
            ".......O.X....OX....XOO...X.........\n" +
            "X2,1X2,2 X4,5X3,4 X3,5X3,3,3,4 X2,4X3,3 X3,1X2,2 X4,3X3,3 X1,2X2,2 RESIGN\n" +
            "1 X2,1X2,2\n" +
            ".......O.X...XXX....XOO...X.........\n" +
            "O3,1O3,2,2,1 O5,1O4,2 O0,3O2,3,1,3 O1,2O2,3 RESIGN\n" +
            "2 O3,1O3,2,2,1\n" +
            ".......O.X...OXX...OOOO...X.........\n" +
            "X2,0X2,1,3,1 X4,5X3,4 X4,1X3,2 X2,4X3,3 X4,3X3,3 X4,0X3,1 X4,4X3,3 RESIGN X0,0X1,1\n" +
            "1 X4,0X3,1\n" +
            ".......O.X...OXX...XOOO.X.X.........\n" +
            "O5,2O4,2 O3,0O3,1 O4,1O3,1 O5,1O4,2 O0,3O2,3,1,3 RESIGN O1,2O2,2,2,3 O2,4O2,2,2,3 O1,4O2,3\n" +
            "2 O3,0O3,1\n" +
            ".......O.X...OXX..OOOOO.X.X.........\n" +
            "X4,5X3,4 X4,1X3,2 X2,4X3,3 X2,0X2,1,3,0,3,1 X4,3X3,3 X4,4X3,3 RESIGN X0,0X1,1\n" +
            "1 X2,0X2,1,3,0,3,1\n" +
            ".......O.X..XXXX..XXOOO.X.X.........\n" +
            "O4,1O2,1,3,1 O5,2O4,2 O5,1O4,2 O0,3O2,3,1,3 RESIGN O1,2O2,2,2,3 O1,0O2,1 O1,4O2,3\n" +
            "2 O4,1O2,1,3,1\n" +
            ".......O.X..XOXX..XOOOO.XOX.........\n" +
            "X4,5X3,4 X2,4X3,3 X5,2X4,1 X4,3X3,3 X3,5X3,2,3,3,3,4,3,1 X4,4X3,3 X5,0X3,2,4,1 X1,2X2,1 X0,2X1,1 RESIGN X0,0X1,1\n" +
            "1 X2,4X3,3\n" +
            ".......O.X..XOXXX.XOOXO.XOX.........\n" +
            "O5,2O4,2 O5,3O4,2 O4,4O2,2,3,3 O4,3O4,2 O1,4O2,3,2,4 RESIGN O0,4O2,2,1,3 O1,2O2,2,2,3 O2,5O2,2,2,3,2,4\n" +
            "2 O1,4O2,3,2,4\n" +
            ".......O.XO.XOXOO.XOOXO.XOX.........\n" +
            "X2,5X2,3,2,4 X3,5X3,4,2,4 X5,2X4,1 X1,2X2,1 X0,2X1,1 RESIGN X0,0X1,1 X1,5X2,4,1,4\n" +
            "1 X1,5X2,4,1,4\n" +
            ".......O.XXXXOXOX.XOOXO.XOX.........\n" +
            "O5,2O4,2 O0,4O2,2,2,4,1,3,1,4 O5,3O4,2 O4,3O3,3,4,2 O4,4O2,2,3,3 O2,5O2,4 RESIGN O0,3O1,3 O0,5O1,4 O1,2O2,2\n" +
            "2 O4,3O3,3,4,2\n" +
            ".......O.XXXXOXOX.XOOOO.XOOO........\n" +
            "X4,4X4,3,3,3,3,4,4,1,4,2 X5,2X3,2,4,1,4,2 X5,0X3,2,2,3,4,1 X3,5X3,2,3,3,3,4,3,1 X5,1X3,3,4,2 X5,3X4,3,3,3,2,3,3,1,4,2 X1,2X2,1 X0,2X1,1 RESIGN X0,0X1,1\n" +
            "1 X5,3X4,3,3,3,2,3,3,1,4,2\n" +
            ".......O.XXXXOXXX.XXOXO.XOXX.....X..\n" +
            "O5,2O4,3,4,2 O4,4O4,3,2,2,3,3,4,2 O5,4O4,3 O0,5O2,3,1,4 O0,4O2,4,1,4 RESIGN O1,2O2,2,2,3 O2,5O2,2,2,3,2,4\n" +
            "2 O0,5O2,3,1,4\n" +
            ".....O.O.XOXXOXOX.XXOXO.XOXX.....X..\n" +
            "X1,0X3,2,2,1 X3,5X3,4 X5,1X4,1 X5,2X4,1 X4,4X3,4 X0,4X1,4 X1,2X2,1 X2,5X3,4 X0,1X2,1,1,1 X0,2X1,1 RESIGN X0,0X1,1\n" +
            "1 X0,4X1,4\n" +
            "....XO.O.XXXXOXOX.XXOXO.XOXX.....X..\n" +
            "O5,2O4,3,4,2 O2,5O2,4,1,5 O4,4O4,3,2,2,3,3,4,2 O5,4O4,3 O0,3O1,3,0,4 RESIGN O1,2O2,2\n" +
            "2 O5,2O4,3,4,2\n" +
            "....XO.O.XXXXOXOX.XXOXO.XOOO....OX..\n" +
            "X4,4X4,3,3,4,4,1,4,2 X5,1X5,2,4,1,4,2 X3,5X3,4 X5,0X3,2,2,3,4,1 X1,2X2,1 X0,1X2,1,1,1 X0,2X1,1 RESIGN X0,0X1,1\n" +
            "1 X5,1X5,2,4,1,4,2\n" +
            "....XO.O.XXXXOXOX.XXOXO.XXXO...XXX..\n" +
            "O2,5O2,4,1,5 O5,0O4,1 O4,4O2,2,3,3 O0,3O1,3,0,4 RESIGN O1,2O2,2\n" +
            "2 O5,0O4,1\n" +
            "....XO.O.XXXXOXOX.XXOXO.XOXO..OXXX..\n" +
            "X3,5X3,4 X4,4X4,3,3,4 X1,2X2,1 X2,5X4,3,3,4 X0,1X2,1,1,1 X0,2X1,1 RESIGN X0,0X1,1\n" +
            "1 X0,1X2,1,1,1\n" +
            ".X..XO.X.XXXXXXOX.XXOXO.XOXO..OXXX..\n" +
            "O2,5O2,4,1,5 O0,3O1,3,0,4 O1,0O2,1,4,0,3,0,2,0 O5,4O5,1,5,2,5,3 RESIGN O1,2O2,2\n" +
            "2 O1,0O2,1,4,0,3,0,2,0\n" +
            ".X..XOOX.XXXOOXOX.OXOXO.OOXO..OXXX..\n" +
            "X3,5X3,4 X4,4X4,3,3,4 X2,5X4,3,3,4 RESIGN\n" +
            "1 X4,4X4,3,3,4\n" +
            ".X..XOOX.XXXOOXOX.OXOXX.OOXXX.OXXX..\n" +
            "O2,5O2,4,1,5 O0,3O1,3,0,4 O5,4O4,3,5,1,5,2,5,3 RESIGN O4,5O4,3,4,4,3,4,4,2 O3,5O3,3,3,4 O1,2O1,1,2,2 O0,2O1,1\n" +
            "2 O5,4O4,3,5,1,5,2,5,3\n" +
            ".X..XOOX.XXXOOXOX.OXOXX.OOXOX.OOOOO.\n" +
            "RESIGN X1,2X2,3\n" +
            "1 X1,2X2,3\n" +
            ".X..XOOXXXXXOOXXX.OXOXX.OOXOX.OOOOO.\n" +
            "O0,3O3,3,2,3,1,2,1,3,0,4 O2,5O2,2,2,3,3,4,2,4,1,5 O4,5O4,4 RESIGN O3,5O3,3,4,4,3,4 O0,2O2,2,1,1,1,2\n" +
            "2 O0,2O2,2,1,1,1,2\n" +
            ".XO.XOOOOXXXOOOXX.OXOXX.OOXOX.OOOOO.\n" +
            "X0,0X2,2,1,1 X0,3X0,2 RESIGN\n" +
            "1 X0,3X0,2\n" +
            ".XXXXOOOOXXXOOOXX.OXOXX.OOXOX.OOOOO.\n" +
            "O4,5O4,4,2,3,3,4 O5,5O3,3,4,4 O2,5O2,3,3,4,2,4,1,5 O0,0O0,1,0,2,0,3,0,4 RESIGN O3,5O3,3,4,4,3,4\n" +
            "2 O2,5O2,3,3,4,2,4,1,5\n" +
            ".XXXXOOOOXXOOOOOOOOXOXO.OOXOX.OOOOO.\n" +
            "X0,0X2,2,1,1 X3,5X3,4,2,4 X4,5X1,2,2,3,3,4 RESIGN\n" +
            "1 X3,5X3,4,2,4\n" +
            ".XXXXOOOOXXOOOOOXOOXOXXXOOXOX.OOOOO.\n" +
            "O5,5O3,3,4,4 O0,0O0,1,0,2,0,3,0,4 RESIGN O4,5O4,4,3,4,3,5\n" +
            "2 O4,5O4,4,3,4,3,5\n" +
            ".XXXXOOOOXXOOOOOXOOXOXOOOOXOOOOOOOO.\n" +
            "X0,0X2,2,1,1 X5,5X4,4 RESIGN\n" +
            "1 X5,5X4,4\n" +
            ".XXXXOOOOXXOOOOOXOOXOXOOOOXOXOOOOOOX\n" +
            "O0,0O0,1,0,2,0,3,0,4 RESIGN\n" +
            "2 O0,0O0,1,0,2,0,3,0,4\n" +
            "2";

    public static final String G_OTH12 = ".................................................................XO..........OX.................................................................\n" +
            "X6,4X6,5 X4,6X5,6 X5,7X5,6 X7,5X6,5 RESIGN\n" +
            "1 X7,5X6,5\n" +
            ".................................................................XO..........XX..........X......................................................\n" +
            "O7,6O6,6 O5,4O5,5 O7,4O6,5 RESIGN\n" +
            "2 O7,4O6,5\n" +
            ".................................................................XO..........OX.........OX......................................................\n" +
            "X6,4X6,5 X4,6X5,6 X5,7X5,6 X7,3X7,4 RESIGN\n" +
            "1 X6,4X6,5\n" +
            ".................................................................XO.........XXX.........OX......................................................\n" +
            "O7,6O6,6,7,5 O5,4O5,5,6,4 RESIGN\n" +
            "2 O7,6O6,6,7,5\n" +
            ".................................................................XO.........XXO.........OOO.....................................................\n" +
            "X6,7X6,6 X4,7X5,6 X7,7X6,6 X5,7X5,6 X8,4X7,4 X8,6X7,5 X8,3X7,4 X8,5X7,5 X8,7X7,6 RESIGN\n" +
            "1 X8,4X7,4\n" +
            ".................................................................XO.........XXO.........XOO.........X...........................................\n" +
            "O6,3O6,5,6,4 O5,3O6,4 O5,4O6,5,5,5 O8,3O6,5,7,4 O7,3O7,4 O9,3O8,4 O4,5O6,5,5,5 RESIGN O4,4O5,5\n" +
            "2 O9,3O8,4\n" +
            ".................................................................XO.........XXO.........XOO.........O..........O................................\n" +
            "X7,7X7,6,6,6,7,5 X6,7X6,6 X4,7X5,6 X5,7X5,6 X9,4X8,4 X8,6X7,5 X8,5X7,5 X8,7X7,6 RESIGN\n" +
            "1 X9,4X8,4\n" +
            ".................................................................XO.........XXO.........XOO.........X..........OX...............................\n" +
            "O6,3O6,5,6,4 O5,3O6,4 O5,4O6,5,5,5 O9,5O9,4 O8,3O6,5,7,4 O7,3O7,4 O4,5O6,5,5,5 RESIGN O4,4O5,5\n" +
            "2 O9,5O9,4\n" +
            ".................................................................XO.........XXO.........XOO.........X..........OOO..............................\n" +
            "X7,7X7,6,6,6,7,5 X6,7X6,6 X4,7X5,6 X5,7X6,6,5,6,7,5 X8,6X7,5 X8,5X7,5 X8,7X7,6 RESIGN X10,2X9,3 X10,4X9,4 X10,6X9,5\n" +
            "1 X10,4X9,4\n" +
            ".................................................................XO.........XXO.........XOO.........X..........OXO..........X...................\n" +
            "O6,3O6,5,6,4 O5,3O6,4 O5,4O6,5,5,5 O8,3O6,5,7,4 O7,3O8,4,7,4 O4,5O6,5,5,5 O11,5O10,4 O11,3O10,4 RESIGN O4,4O5,5\n" +
            "2 O5,4O6,5,5,5\n" +
            "................................................................OOO.........XOO.........XOO.........X..........OXO..........X...................\n" +
            "X7,7X7,6,7,5 X5,7X6,6,7,5 X4,4X5,4 X4,6X5,5 X9,2X9,3 RESIGN X9,6X9,5 X6,7X6,5,6,6 X8,6X9,5,7,5 X4,7X6,5,5,6 X8,2X9,3 X10,2X9,3 X10,6X9,5\n" +
            "1 X4,4X5,4\n" +
            "....................................................X...........XOO.........XOO.........XOO.........X..........OXO..........X...................\n" +
            "O6,3O6,4 O5,3O5,4,6,4 O7,3O8,4,7,4,6,4 O8,3O7,4 O11,5O10,4 O11,3O10,4 RESIGN O4,3O5,4 O3,3O4,4\n" +
            "2 O11,3O10,4\n" +
            "....................................................X...........XOO.........XOO.........XOO.........X..........OXO..........O..........O........\n" +
            "X5,7X5,5,6,6,5,6,7,5 X4,6X5,5 X7,7X7,6,5,5,6,6,7,5 X9,2X9,3 RESIGN X11,4X10,4 X9,6X9,5 X6,7X6,5,6,6 X4,7X6,5,5,6 X8,7X6,5,7,6 X8,6X7,5 X10,2X9,3 X10,6X9,5\n" +
            "1 X11,4X10,4\n" +
            "....................................................X...........XOO.........XOO.........XOO.........X..........OXO..........X..........OX.......\n" +
            "O6,3O6,4 O5,3O5,4,6,4 O7,3O8,4,7,4,6,4 O8,3O7,4 RESIGN O4,3O5,4 O3,3O4,4 O11,5O11,4,10,4\n" +
            "2 O11,5O11,4,10,4\n" +
            "....................................................X...........XOO.........XOO.........XOO.........X..........OXO..........O..........OOO......\n" +
            "X6,7X6,5,6,6 X5,7X5,5,6,6,5,6,7,5 X4,7X6,5,5,6 X8,7X6,5,7,6 X4,6X5,5 X7,7X7,6,5,5,6,6,7,5 X9,2X9,3 X8,6X7,5 RESIGN X10,2X9,3 X9,6X9,5 X10,6X9,5\n" +
            "1 X9,2X9,3\n" +
            "....................................................X...........XOO.........XOO.........XOO.........X.........XXXO..........O..........OOO......\n" +
            "O6,3O6,4 O5,3O5,4,6,4 O8,2O9,3 O7,3O8,4,7,4,6,4 O8,3O7,4 O9,1O9,2,9,3,9,4 O3,4O5,4,4,4,9,4,8,4,7,4,6,4 RESIGN O4,3O5,4 O10,2O9,3,8,4 O3,3O4,4\n" +
            "2 O3,4O5,4,4,4,9,4,8,4,7,4,6,4\n" +
            "........................................O...........O...........OOO.........OOO.........OOO.........O.........XXOO..........O..........OOO......\n" +
            "X5,7X6,6,8,4,7,5 RESIGN X9,6X9,4,9,5\n" +
            "1 X9,6X9,4,9,5\n" +
            "........................................O...........O...........OOO.........OOO.........OOO.........O.........XXXXX.........O..........OOO......\n" +
            "O8,6O9,5 O10,6O9,5 O8,2O9,3 O10,2O9,3 RESIGN\n" +
            "2 O8,2O9,3\n" +
            "........................................O...........O...........OOO.........OOO.........OOO.......O.O.........XOXXX.........O..........OOO......\n" +
            "X2,4X5,4,4,4,3,4,8,4,7,4,6,4 X7,3X8,4 RESIGN X7,2X8,2\n" +
            "1 X7,2X8,2\n" +
            "........................................O...........O...........OOO.........OOO.......X.OOO.......X.O.........XOXXX.........O..........OOO......\n" +
            "O8,6O9,5 O10,6O9,5 O7,1O8,2 O9,1O9,2 O9,7O9,4,9,5,9,6 RESIGN\n" +
            "2 O9,7O9,4,9,5,9,6\n" +
            "........................................O...........O...........OOO.........OOO.......X.OOO.......X.O.........XOOOOO........O..........OOO......\n" +
            "X9,8X9,3,9,4,9,5,9,6,9,7 RESIGN\n" +
            "1 X9,8X9,3,9,4,9,5,9,6,9,7\n" +
            "........................................O...........O...........OOO.........OOO.......X.OOO.......X.O.........XXXXXXX.......O..........OOO......\n" +
            "O8,6O9,5 O10,6O9,5 O10,2O9,3 O7,1O9,3,8,2 RESIGN\n" +
            "2 O7,1O9,3,8,2\n" +
            "........................................O...........O...........OOO.........OOO......OX.OOO.......O.O.........XOXXXXX.......O..........OOO......\n" +
            "X2,4X5,4,4,4,3,4,8,4,7,4,6,4 X7,0X7,1 X7,3X8,4 RESIGN\n" +
            "1 X7,0X7,1\n" +
            "........................................O...........O...........OOO.........OOO.....XXX.OOO.......O.O.........XOXXXXX.......O..........OOO......\n" +
            "O8,6O9,5 O10,6O9,5 O9,9O9,8,9,4,9,5,9,6,9,7 O6,0O7,1 O6,2O7,2 O9,1O9,2 O10,2O9,2 RESIGN\n" +
            "2 O9,9O9,8,9,4,9,5,9,6,9,7\n" +
            "........................................O...........O...........OOO.........OOO.....XXX.OOO.......O.O.........XOOOOOOO......O..........OOO......\n" +
            "X9,10X9,8,9,9,9,3,9,4,9,5,9,6,9,7 RESIGN\n" +
            "1 X9,10X9,8,9,9,9,3,9,4,9,5,9,6,9,7\n" +
            "........................................O...........O...........OOO.........OOO.....XXX.OOO.......O.O.........XXXXXXXXX.....O..........OOO......\n" +
            "O10,2O9,2,9,3 O8,6O9,5 O10,6O9,5 O6,0O7,1 O6,2O7,2 RESIGN\n" +
            "2 O10,2O9,2,9,3\n" +
            "........................................O...........O...........OOO.........OOO.....XXX.OOO.......O.O.........OOXXXXXXX...O.O..........OOO......\n" +
            "X2,4X5,4,4,4,3,4,8,4,7,4,6,4 X11,2X10,2,9,2,8,2 X7,3X8,4 RESIGN X9,1X9,2,9,3\n" +
            "1 X11,2X10,2,9,2,8,2\n" +
            "........................................O...........O...........OOO.........OOO.....XXX.OOO.......X.O.........XOXXXXXXX...X.O.........XOOO......\n" +
            "O8,6O9,5 O11,1O11,2,10,2 O10,6O9,5 O6,0O8,2,7,1 O9,11O9,8,9,9,9,10,9,4,9,5,9,6,9,7 RESIGN O9,1O10,2,9,2\n" +
            "2 O11,1O11,2,10,2\n" +
            "........................................O...........O...........OOO.........OOO.....XXX.OOO.......X.O.........XOXXXXXXX...O.O........OOOOO......\n" +
            "X2,4X5,4,4,4,3,4,8,4,7,4,6,4 X7,3X8,4 RESIGN\n" +
            "1 X2,4X5,4,4,4,3,4,8,4,7,4,6,4\n" +
            "............................X...........X...........X...........XOO.........XOO.....XXX.XOO.......X.X.........XOXXXXXXX...O.O........OOOOO......\n" +
            "O7,3O7,4,6,4 O6,3O6,4 O8,6O9,5 O5,3O5,4,6,4 O8,3O7,4 O6,0O8,2,7,1 O9,1O9,2 RESIGN O4,3O5,4 O1,4O5,4,4,4,3,4,2,4,9,4,8,4,7,4,6,4 O6,2O9,2,8,2,7,2 O9,11O9,8,9,9,9,10,9,4,9,5,9,6,9,7 O3,3O4,4\n" +
            "2 O1,4O5,4,4,4,3,4,2,4,9,4,8,4,7,4,6,4\n" +
            "................O...........O...........O...........O...........OOO.........OOO.....XXX.OOO.......X.O.........XOOXXXXXX...O.O........OOOOO......\n" +
            "X7,3X8,4 RESIGN\n" +
            "1 X7,3X8,4\n" +
            "................O...........O...........O...........O...........OOO.........OOO.....XXXXOOO.......X.X.........XOOXXXXXX...O.O........OOOOO......\n" +
            "O6,2O9,2,8,2,7,2 O8,6O9,5 O9,11O9,8,9,9,9,10,9,5,9,6,9,7 O6,0O8,2,7,1 RESIGN O9,1O9,2,8,2,7,3\n" +
            "2 O9,11O9,8,9,9,9,10,9,5,9,6,9,7\n" +
            "................O...........O...........O...........O...........OOO.........OOO.....XXXXOOO.......X.X.........XOOOOOOOOO..O.O........OOOOO......\n" +
            "X7,7X7,6,7,4,7,5 X5,7X6,6,7,5 X4,6X5,5,6,4 RESIGN X0,4X5,4,4,4,3,4,2,4,1,4,7,4,6,4 X10,6X9,5\n" +
            "1 X0,4X5,4,4,4,3,4,2,4,1,4,7,4,6,4\n" +
            "....X...........X...........X...........X...........X...........XOO.........XOO.....XXXXXOO.......X.X.........XOOOOOOOOO..O.O........OOOOO......\n" +
            "O6,3O6,4 O5,3O5,4,6,4 O8,3O7,4 O6,2O9,2,8,2,7,2,8,4,7,3 O6,0O8,2,7,1 RESIGN O4,3O5,4 O3,3O4,4 O9,1O9,2,8,2,7,3,6,4\n" +
            "2 RESIGN\n" +
            "1";
    private static final String G_333 = ".........\n" +
            "X1,1 X2,2 X0,0 X1,0 X2,1 X0,1 X0,2 X1,2 X2,0 RESIGN\n" +
            "1 X1,1\n" +
            "....X....\n" +
            "O2,0 O0,0 O1,2 RESIGN O0,2 O1,0 O2,1 O0,1 O2,2\n" +
            "2 O0,2\n" +
            "..O.X....\n" +
            "X2,2 X0,0 X1,0 X2,1 X0,1 X1,2 X2,0 RESIGN\n" +
            "1 X2,0\n" +
            "..O.X.X..\n" +
            "O0,0 O1,2 RESIGN O1,0 O2,1 O0,1 O2,2\n" +
            "2 O2,2\n" +
            "..O.X.X.O\n" +
            "X0,0 X1,0 X2,1 X0,1 X1,2 RESIGN\n" +
            "1 X1,2\n" +
            "..O.XXX.O\n" +
            "O0,0 RESIGN O1,0 O2,1 O0,1\n" +
            "2 O1,0\n" +
            "..OOXXX.O\n" +
            "X0,0 X2,1 X0,1 RESIGN\n" +
            "1 X0,0\n" +
            "0";
    private static final String G_655 = "..............................\n" +
            "X0,3 X0,4 X2,2 X0,0 X2,1 X0,1 X2,4 X4,3 X0,2 X2,3 X4,4 RESIGN X3,0 X5,3 X3,1 X5,2 X5,1 X5,0 X1,4 X1,1 X3,4 X1,0 X1,3 X3,2 X1,2 X3,3 X5,4 X4,1 X4,2 X2,0 X4,0\n" +
            "1 X2,2\n" +
            "............X.................\n" +
            "RESIGN O1,4 O3,1 O5,4 O3,0 O1,2 O5,0 O1,3 O3,4 O5,1 O1,0 O3,3 O5,2 O1,1 O3,2 O5,3 O0,4 O0,3 O4,2 O2,0 O4,1 O4,0 O0,0 O2,3 O2,4 O0,2 O2,1 O4,4 O0,1 O4,3\n" +
            "2 O3,2\n" +
            "............X....O............\n" +
            "X0,3 X0,4 X0,0 X2,1 X0,1 X2,4 X4,3 X0,2 X2,3 X4,4 RESIGN X3,0 X5,3 X3,1 X5,2 X5,1 X5,0 X1,4 X1,1 X3,4 X1,0 X1,3 X1,2 X3,3 X5,4 X4,1 X4,2 X2,0 X4,0\n" +
            "1 X4,3\n" +
            "............X....O.....X......\n" +
            "RESIGN O1,4 O3,1 O5,4 O3,0 O1,2 O5,0 O1,3 O3,4 O5,1 O1,0 O3,3 O5,2 O1,1 O5,3 O0,4 O0,3 O4,2 O2,0 O4,1 O4,0 O0,0 O2,3 O2,4 O0,2 O2,1 O4,4 O0,1\n" +
            "2 O1,1\n" +
            "......O.....X....O.....X......\n" +
            "X0,3 X0,4 X0,0 X2,1 X0,1 X2,4 X0,2 X2,3 X4,4 RESIGN X3,0 X5,3 X3,1 X5,2 X5,1 X5,0 X1,4 X3,4 X1,0 X1,3 X1,2 X3,3 X5,4 X4,1 X4,2 X2,0 X4,0\n" +
            "1 X5,0\n" +
            "......O.....X....O.....X.X....\n" +
            "RESIGN O1,4 O3,1 O5,4 O3,0 O1,2 O1,3 O3,4 O5,1 O1,0 O3,3 O5,2 O5,3 O0,4 O0,3 O4,2 O2,0 O4,1 O4,0 O0,0 O2,3 O2,4 O0,2 O2,1 O4,4 O0,1\n" +
            "2 O0,4\n" +
            "....O.O.....X....O.....X.X....\n" +
            "X0,3 X0,0 X2,1 X0,1 X2,4 X0,2 X2,3 X4,4 RESIGN X3,0 X5,3 X3,1 X5,2 X5,1 X1,4 X3,4 X1,0 X1,3 X1,2 X3,3 X5,4 X4,1 X4,2 X2,0 X4,0\n" +
            "1 X0,0\n" +
            "X...O.O.....X....O.....X.X....\n" +
            "RESIGN O0,3 O1,4 O3,1 O4,2 O5,4 O2,0 O3,0 O4,1 O4,0 O1,2 O2,3 O1,3 O2,4 O3,4 O5,1 O0,2 O1,0 O2,1 O3,3 O4,4 O5,2 O0,1 O5,3\n" +
            "2 O5,4\n" +
            "X...O.O.....X....O.....X.X...O\n" +
            "X0,3 X1,4 X3,4 X1,0 X2,1 X0,1 X1,3 X2,4 X0,2 X1,2 X2,3 X3,3 X4,4 RESIGN X3,0 X4,1 X5,3 X3,1 X4,2 X5,2 X2,0 X5,1 X4,0\n" +
            "1 X1,4\n" +
            "X...O.O..X..X....O.....X.X...O\n" +
            "RESIGN O0,3 O3,1 O4,2 O2,0 O3,0 O4,1 O4,0 O1,2 O2,3 O1,3 O2,4 O3,4 O5,1 O0,2 O1,0 O2,1 O3,3 O4,4 O5,2 O0,1 O5,3\n" +
            "2 O4,0\n" +
            "X...O.O..X..X....O..O..X.X...O\n" +
            "X0,3 X3,4 X1,0 X2,1 X0,1 X1,3 X2,4 X0,2 X1,2 X2,3 X3,3 X4,4 RESIGN X3,0 X4,1 X5,3 X3,1 X4,2 X5,2 X2,0 X5,1\n" +
            "1 X3,1\n" +
            "X...O.O..X..X...XO..O..X.X...O\n" +
            "RESIGN O0,3 O4,2 O2,0 O3,0 O4,1 O1,2 O2,3 O1,3 O2,4 O3,4 O5,1 O0,2 O1,0 O2,1 O3,3 O4,4 O5,2 O0,1 O5,3\n" +
            "2 O2,3\n" +
            "0";
    private static final String G_573 = "...................................\n" +
            "X0,3 X2,6 X0,4 X2,5 X0,5 X0,6 X2,2 X4,5 X0,0 X2,1 X4,6 X0,1 X2,4 X4,3 X0,2 X2,3 X4,4 RESIGN X3,0 X3,1 X1,5 X1,4 X3,6 X1,6 X1,1 X3,4 X1,0 X3,5 X1,3 X3,2 X1,2 X3,3 X4,1 X4,2 X2,0 X4,0\n" +
            "1 X4,0\n" +
            "............................X......\n" +
            "RESIGN O1,6 O1,4 O1,5 O3,6 O3,1 O3,0 O1,2 O3,5 O1,3 O3,4 O1,0 O3,3 O1,1 O3,2 O0,4 O0,3 O0,6 O2,5 O0,5 O2,6 O4,2 O2,0 O4,1 O0,0 O2,3 O4,6 O2,4 O4,5 O0,2 O2,1 O4,4 O0,1 O2,2 O4,3\n" +
            "2 O0,6\n" +
            "......O.....................X......\n" +
            "X0,3 X2,6 X0,4 X2,5 X0,5 X2,2 X4,5 X0,0 X2,1 X4,6 X0,1 X2,4 X4,3 X0,2 X2,3 X4,4 RESIGN X3,0 X3,1 X1,5 X1,4 X3,6 X1,6 X1,1 X3,4 X1,0 X3,5 X1,3 X3,2 X1,2 X3,3 X4,1 X4,2 X2,0\n" +
            "1 X2,2\n" +
            "......O.........X...........X......\n" +
            "RESIGN O1,6 O1,4 O1,5 O3,6 O3,1 O3,0 O1,2 O3,5 O1,3 O3,4 O1,0 O3,3 O1,1 O3,2 O0,4 O0,3 O2,5 O0,5 O2,6 O4,2 O2,0 O4,1 O0,0 O2,3 O4,6 O2,4 O4,5 O0,2 O2,1 O4,4 O0,1 O4,3\n" +
            "2 O4,6\n" +
            "......O.........X...........X.....O\n" +
            "X0,3 X2,6 X0,4 X2,5 X0,5 X4,5 X0,0 X2,1 X0,1 X2,4 X4,3 X0,2 X2,3 X4,4 RESIGN X3,0 X3,1 X1,5 X1,4 X3,6 X1,6 X1,1 X3,4 X1,0 X3,5 X1,3 X3,2 X1,2 X3,3 X4,1 X4,2 X2,0\n" +
            "1 X3,1\n" +
            "1";

/*******************************************************************************/
/*******************************************************************************/

    private static void totalGradeInit() {
        if (!TOTAL_GRADE) return;
        System.setErr(new PrintStream(SYSERR));
        SYSERR.setThread(Thread.currentThread());
        SYSERR.standardSysErr(true);
    }

    private static void totalGradeRunStart(Thread t) {
        if (!TOTAL_GRADE) return;
        SYSERR.standardSysErr(false);
        SYSERR.setThread(t);
    }

    private static void totalGradeRunEnd() {
        if (!TOTAL_GRADE) return;
        SYSERR.setThread(Thread.currentThread());
        SYSERR.standardSysErr(true);
    }

    private static class SysErr extends OutputStream {
        public void standardSysErr(boolean std) {
            if (!lock.acquireLock()) return;
            standardErr = std;
            lock.releaseLock();
        }
        @Override
        public void write(int b) throws IOException {
            if (!lock.acquireLock()) return;
            try {
                if (currThread != null && Thread.currentThread().getId() == currThread.getId()) {
                    if (standardErr) {
                        STD_ERR.write(b);
                    } else
                        buffer.offer(b);
                }
            } catch (Throwable e) {}
            finally { lock.releaseLock(); }
        }

        public String getBuffer() {
            if (!lock.acquireLock()) return null;
            String s = "";
            try {
                if (currThread != null && Thread.currentThread().getId() == currThread.getId()) {
                    int size = buffer.size();
                    if (size > 0) {
                        Integer[] aux = buffer.toArray(new Integer[size]);
                        int[] cps = new int[size];
                        for (int i = 0; i < cps.length; i++) cps[i] = aux[i];
                        s = new String(cps, 0, cps.length);
                        buffer.clear();
                    }
                }
            } catch (Throwable e) {}
            finally { lock.releaseLock(); }
            return s;
        }

        public void setThread(Thread t) {
            if (!lock.acquireLock()) return;
            currThread = t;
            lock.releaseLock();
        }

        private final Locker lock = new Locker();
        private final Queue<Integer> buffer = new ConcurrentLinkedDeque<>();
        private volatile Thread currThread = null;
        private volatile boolean standardErr = false;
    }

    private static final boolean TOTAL_GRADE = false;
    private static final PrintStream STD_ERR = System.err;
    private static final SysErr SYSERR = new SysErr();

    /******************************************************************************/

    private static class DummyBoard<P> implements Board<P> {
        private DummyBoard() {
            List<Pos> pl = new ArrayList<>();
            for (int b = 0 ; b < 10 ; b++)
                for (int t = 0 ; t < 10 ; t++)
                    pl.add(new Pos(b,t));
            posList = Collections.unmodifiableList(pl);
            pmMap = new HashMap<>();
        }
        @Override
        public System system() { return System.OCTAGONAL; }
        @Override
        public int width() { return 10; }
        @Override
        public int height() { return 10; }
        @Override
        public Pos adjacent(Pos p, Dir d) {
            Objects.requireNonNull(p);
            Objects.requireNonNull(d);
            if (!(p.b >= 0 && p.b < 10 && p.t >= 0 && p.t < 10)) return null;
            Disp ds = toDisp.get(d);
            int b = p.b + ds.db, t = p.t + ds.dt;
            if (!(b >= 0 && b < 10 && t >= 0 && t < 10)) return null;
            return new Pos(b, t);
        }
        @Override
        public List<Pos> positions() { return posList; }
        @Override
        public P get(Pos p) {
            Objects.requireNonNull(p);
            return pmMap.get(p);
        }
        @Override
        public boolean isModifiable() { return true; }
        @Override
        public P put(P pm, Pos p) {
            Objects.requireNonNull(pm);
            Objects.requireNonNull(p);
            check(p);
            return pmMap.put(p, pm);
        }
        @Override
        public P remove(Pos p) {
            Objects.requireNonNull(p);
            check(p);
            return pmMap.remove(p);
        }
        private DummyBoard(DummyBoard<P> b) {
            List<Pos> lp = new ArrayList<>();
            lp.addAll(b.posList);
            posList = Collections.unmodifiableList(lp);
            pmMap = new HashMap<>();
            pmMap.putAll(b.pmMap);
        }
        static class Disp {
            final int db, dt;
            Disp(int db, int dt) { this.db = db; this.dt = dt; }
        }
        static EnumMap<Dir, Disp> toDisp = new EnumMap<>(Dir.class);
        static {
            toDisp.put(Dir.UP, new Disp(0,1));
            toDisp.put(Dir.UP_R, new Disp(1,1));
            toDisp.put(Dir.RIGHT, new Disp(1,0));
            toDisp.put(Dir.DOWN_R, new Disp(1,-1));
            toDisp.put(Dir.DOWN, new Disp(0,-1));
            toDisp.put(Dir.DOWN_L, new Disp(-1,-1));
            toDisp.put(Dir.LEFT, new Disp(-1,0));
            toDisp.put(Dir.UP_L, new Disp(-1,1));
        }
        private void check(Pos p) {
            if (!(p.b >= 0 && p.b < 10 && p.t >= 0 && p.t < 10)) throw new IllegalArgumentException();
        }
        private final List<Pos> posList;
        private final Map<Pos,P> pmMap;
    }

    private static class DummyGU implements GameRuler<PieceModel<Species>> {
        private DummyGU(String p1, String p2) {
            board = new DummyBoard<>();
            unModB = new Board<PieceModel<Species>>() {
                @Override
                public System system() { return board.system(); }
                @Override
                public int width() { return board.width(); }
                @Override
                public int height() { return board.height(); }
                @Override
                public Pos adjacent(Pos p, Dir d) { return board.adjacent(p, d); }
                @Override
                public List<Pos> positions() { return board.positions(); }
                @Override
                public PieceModel<Species> get(Pos p) { return board.get(p); }
            };
            pNames = Collections.unmodifiableList(Arrays.asList(p1, p2));
        }
        @Override
        public String name() { return "DummyGU"; }
        @Override
        public <T> T getParam(String name, Class<T> c) { return null; }
        @Override
        public List<String> players() { return pNames; }
        @Override
        public String color(String name) {
            Objects.requireNonNull(name);
            if (!pNames.contains(name)) throw new IllegalArgumentException();
            return "white";
        }
        @Override
        public Board<PieceModel<Species>> getBoard() { return unModB; }
        @Override
        public int turn() { return gRes == -1 ? cTurn : 0; }
        @Override
        public boolean move(Move<PieceModel<Species>> m) {
            Objects.requireNonNull(m);
            if (gRes != -1) throw new IllegalArgumentException();
            if (!validMoves().contains(m)) {
                gRes = 3 - cTurn;
                return false;
            }
            board.put(disc, m.actions.get(0).pos.get(0));
            if (validMoves().isEmpty()) gRes = 0;
            cTurn = 3 - cTurn;
            return true;
        }
        @Override
        public boolean unMove() { return false; }
        @Override
        public boolean isPlaying(int i) {
            if (i != 1 && i != 2) throw new IllegalArgumentException();
            if (gRes != -1) return false;
            return true;
        }
        @Override
        public int result() { return gRes; }
        @Override
        public Set<Move<PieceModel<Species>>> validMoves() {
            Set<Move<PieceModel<Species>>> vm = new HashSet<>();
            for (Pos p : board.positions()) {
                if (board.get(p) == null) {
                    Move<PieceModel<Species>> m = new Move<>(new Action<>(p, disc));
                    vm.add(m);
                }
            }
            return Collections.unmodifiableSet(vm);
        }
        @Override
        public GameRuler<PieceModel<Species>> copy() {
            return new DummyGU(this);
        }

        private DummyGU(DummyGU g) {
            board = new DummyBoard<>(g.board);
            unModB = new Board<PieceModel<Species>>() {
                @Override
                public System system() { return board.system(); }
                @Override
                public int width() { return board.width(); }
                @Override
                public int height() { return board.height(); }
                @Override
                public Pos adjacent(Pos p, Dir d) { return board.adjacent(p, d); }
                @Override
                public List<Pos> positions() { return board.positions(); }
                @Override
                public PieceModel<Species> get(Pos p) { return board.get(p); }
            };
            List<String> nn = new ArrayList<>();
            nn.addAll(g.pNames);
            pNames = Collections.unmodifiableList(nn);
            gRes = g.gRes;
            cTurn = g.cTurn;
        }

        private final DummyBoard<PieceModel<Species>> board;
        private final Board<PieceModel<Species>> unModB;
        private final List<String> pNames;
        private final PieceModel<Species> disc = new PieceModel<>(Species.DISC, "white");
        private int gRes = -1, cTurn = 1;
    }

    private static class DummyGUFactory implements GameFactory<DummyGU> {
        @Override
        public String name() { return "DummyGU"; }
        @Override
        public int minPlayers() { return 2; }
        @Override
        public int maxPlayers() { return 2; }
        @Override
        public List<Param<?>> params() { return null; }
        @Override
        public void setPlayerNames(String...names) {
            for (String n : names)
                Objects.requireNonNull(n);
            if (names.length != 2) throw new IllegalArgumentException();
            pNames = names;
        }
        @Override
        public DummyGU newGame() {
            if (pNames == null) throw new IllegalStateException();
            return new DummyGU(pNames[0], pNames[1]);
        }

        private String[] pNames;
    }

    private static void notEx(Runnable r, Class<?> exClass, String err) throws Exception {
        err += " "+exClass.getSimpleName();
        try {
            r.run();
        } catch (Exception ex) {
            if (!exClass.isInstance(ex)) throw new Exception(err);
            return;
        }
        throw new Exception(err);
    }

    private static final boolean ONLY_ERRS = false;
    private static String printBuffer = "";
    private static void printClear() { printBuffer = ""; }
    private static void print_(String m) {
        System.out.print(printBuffer+m);
        printClear();
    }
    private static void print(String m) { if (ONLY_ERRS) printBuffer += m; else print_(m); }
    private static void printErr(String m) { print_(m); }
    private static void println(String m) { print(m+"\n"); }
    private static void printlnErr(String m) { printErr(m+"\n"); }

    private static Result handleThrowable(Throwable t) {
        String msg = "";
        boolean fatal = false;
        if (t instanceof Exception) {
            Exception ex = (Exception)t;
            StackTraceElement[] st = ex.getStackTrace();
            String s = st.length > 0 ? st[0].toString() : "";
            //String s = Arrays.toString(st);
            msg += "Eccezione inattesa"+(!s.isEmpty() ? " lanciata da "+s+": " : ": ");
        } else {
            msg += "ERRORE GRAVE, impossibile continuare il test: ";
            fatal = true;
        }
        return new Result(msg+t, fatal);
    }

    private static boolean runTest(String msg, float score, int ms, Callable<Result> test) {
        //long time = System.currentTimeMillis();
        //Supplier<String> tm = () -> String.format("  Time %d  Timeout %d", (System.currentTimeMillis()-time), ms);
        Supplier<String> tm = () -> "";
        FutureTask<Result> future = new FutureTask<>(test);
        Thread t = new Thread(future);
        t.setDaemon(true);
        printClear();
        print(msg+" ");
        OUTPUT.standardOutput(false);
        INPUT.standardInput(false);
        OUTPUT.setThread(t);
        INPUT.setThread(t);
        totalGradeRunStart(t);
        t.start();
        Result res = null;
        try {
            res = future.get(ms, TimeUnit.MILLISECONDS);
        } catch (CancellationException | InterruptedException | TimeoutException | ExecutionException e) {}
        future.cancel(true);
        OUTPUT.setThread(Thread.currentThread());
        INPUT.setThread(Thread.currentThread());
        OUTPUT.standardOutput(true);
        INPUT.standardInput(true);
        totalGradeRunEnd();
        String noScore = "  (-"+score+")";
        if (res == null)
            printlnErr("ERRORE: limite di tempo superato ("+ms+" ms)"+noScore+tm.get());
        else if (res.fatal) {
            printlnErr(res.err+noScore+tm.get());
            return false;
        } else if (res.err != null) {
            printlnErr(res.err+noScore+tm.get());
        } else {
            println(" score "+score+tm.get());
            totalScore += score;
        }
        return true;
    }

    private static class Result {
        public final String err;
        public final boolean fatal;

        public Result(String e, boolean f) {
            err = e;
            fatal = f;
        }

        public Result() { this(null, false); }
        public Result(String e) { this(e, false); }
    }

    private static Result err(String e) { return new Result("ERRORE "+e); }

    private static class Locker {
        public Locker() {
            lock = new ReentrantLock(true);
        }

        public synchronized boolean acquireLock() {
            try {
                if (Thread.currentThread().getId() == MAIN_THREAD.getId()) {
                    while (!lock.tryLock()) ;
                } else
                    lock.lockInterruptibly();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false; }
            return true;
        }

        public synchronized void releaseLock() { lock.unlock(); }


        private final ReentrantLock lock;
    }

    private static class Output extends OutputStream {
        public void standardOutput(boolean std) {
            if (!lock.acquireLock()) return;
            standardOut = std;
            lock.releaseLock();
        }
        @Override
        public void write(int b) throws IOException {
            if (!lock.acquireLock()) return;
            try {
                if (currThread != null && Thread.currentThread().getId() == currThread.getId()) {
                    if (standardOut) {
                        STD_OUT.write(b);
                    } else
                        buffer.offer(b);
                }
            } catch (Throwable e) {}
            finally { lock.releaseLock(); }
        }

        public String getBuffer() {
            if (!lock.acquireLock()) return null;
            String s = "";
            try {
                if (currThread != null && Thread.currentThread().getId() == currThread.getId()) {
                    int size = buffer.size();
                    if (size > 0) {
                        Integer[] aux = buffer.toArray(new Integer[size]);
                        int[] cps = new int[size];
                        for (int i = 0; i < cps.length; i++) cps[i] = aux[i];
                        s = new String(cps, 0, cps.length);
                        buffer.clear();
                    }
                }
            } catch (Throwable e) {}
            finally { lock.releaseLock(); }
            return s;
        }

        public void setThread(Thread t) {
            if (!lock.acquireLock()) return;
            currThread = t;
            lock.releaseLock();
        }

        private final Locker lock = new Locker();
        private final Queue<Integer> buffer = new ConcurrentLinkedDeque<>();
        private volatile Thread currThread = null;
        private volatile boolean standardOut = false;
    }

    private static class Input extends InputStream {
        public void standardInput(boolean std) {
            if (!lock.acquireLock()) return;
            standardIn = std;
            lock.releaseLock();
        }

        public void setContent(String s) {
            if (!lock.acquireLock()) return;
            try {
                content = new ByteArrayInputStream(s.getBytes());
            } catch (Throwable e) {}
            finally { lock.releaseLock(); }
        }

        @Override
        public int read() throws IOException {
            int b = -1;
            try {
                if (!lock.acquireLock()) return b;
                if (currThread != null && Thread.currentThread().getId() == currThread.getId()) {
                    if (standardIn) {
                        b = STD_IN.read();
                    } else
                        b = content.read();
                }
            } catch (Throwable e) {}
            finally { lock.releaseLock(); }
            return b;
        }

        public void setThread(Thread t) {
            if (!lock.acquireLock()) return;
            currThread = t;
            lock.releaseLock();
        }

        private final Locker lock = new Locker();
        private volatile Thread currThread = null;
        private volatile ByteArrayInputStream content = null;
        private volatile boolean standardIn = false;
    }

    private static String sessionToString(String[][] session) {
        String s = "";
        for (String[] t : session)
            s += t[1]+"\n";
        return s;
    }


    private static final PrintStream STD_OUT = System.out;
    private static final InputStream STD_IN = System.in;
    private static final Output OUTPUT = new Output();
    private static final Input INPUT = new Input();
    private static final Thread MAIN_THREAD = Thread.currentThread();

    private static double totalScore;
    private static final Random RND = new Random();
}
