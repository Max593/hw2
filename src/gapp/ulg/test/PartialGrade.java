package gapp.ulg.test;


import gapp.ulg.game.board.*;
import gapp.ulg.game.util.BoardOct;
import gapp.ulg.play.RandPlayer;
import gapp.ulg.game.util.Utils;
import gapp.ulg.games.Othello;
import gapp.ulg.games.OthelloFactory;
import gapp.ulg.game.GameFactory;
import gapp.ulg.game.Param;

import static gapp.ulg.game.board.Board.Dir;
import static gapp.ulg.game.board.PieceModel.Species;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;



/** VERSIONE PRELIMINARE
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
        testing = true;
        testWord = null;
        totalScore = 0;
        boolean ok = true;
        ok = gradeTest1(ok);
        ok = gradeTest2(ok);
        System.out.println("Punteggio parziale: "+Math.round(totalScore*10)/10.0);
        testing = false;
    }


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
                if (p1.equals(null)) return new Result("ERRORE Pos.equals true invece di false");
                if (!p1.equals(p2)) return new Result("ERRORE Pos.equals false invece di true");
                if (p1.equals(p3)) return new Result("ERRORE Pos.equals true invece di false");
                if (p1.hashCode() != p2.hashCode()) new Result("ERRORE Pos.hashCode() diversi invece di uguali");
                boolean err = true;
                for (int i = 0 ; i < 13 ; i++)
                    if (p1.hashCode() != new Pos(i,i).hashCode()) err = false;
                if (err) new Result("ERRORE Pos.hashCode()");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_PieceModel(float sc, int ms) {
        return runTest("Test PieceModel", sc, ms, () -> {
            try {
                PieceModel<Species> pm1 = new PieceModel<>(Species.DAMA, "white"),
                        pm2 = new PieceModel<>(Species.DAMA, "white"),
                        pm3 = new PieceModel<>(Species.DAMA, "black");
                if (pm1.equals(null)) return new Result("ERRORE PieceModel.equals true invece di false");
                if (!pm1.equals(pm2)) return new Result("ERRORE PieceModel.equals false invece di true");
                if (pm1.equals(pm3)) return new Result("ERRORE PieceModel.equals true invece di false");
                if (pm1.hashCode() != pm2.hashCode()) new Result("ERRORE PieceModel.hashCode() diversi invece di uguali");
                boolean err = true;
                for (Species s : Species.values())
                    if (pm1.hashCode() != new PieceModel<>(s, "black").hashCode()) err = false;
                if (err) new Result("ERRORE PieceModel.hashCode()");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_BoardDef(float sc, int ms) {
        return runTest("Test Board default methods", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                DummyBoard<PieceModel<Species>> b = new DummyBoard<>();
                if (!b.isPos(new Pos(0,0))) return new Result("ERRORE Board.isPos false invece di true");
                if (b.isPos(new Pos(10,0))) return new Result("ERRORE Board.isPos true invece di false");
                try {
                    b.isPos(null);
                    return new Result("ERRORE Board.isPos(null): non lancia eccezione");
                } catch (Exception ex) {}
                Set<Pos> pSet = b.get();
                if (!pSet.isEmpty()) return new Result("ERRORE Board.get(): insieme non vuoto");
                try {
                    pSet.add(new Pos(0,0));
                    return new Result("ERRORE Board.get(): insieme modificabile");
                } catch (Exception ex) {}
                PieceModel<Species> pm = new PieceModel<>(Species.BISHOP, "white");
                Pos p = new Pos(2,3);
                b.put(pm, p);
                pSet = b.get();
                if (pSet.size() != 1 || !pSet.contains(p)) return new Result("ERRORE Board.get(): insieme errato");
                try {
                    PieceModel<Species> pm2 = null;
                    b.get(pm2);
                    return new Result("ERRORE Board.get(P pm) con pm == null"+nE);
                } catch (Exception ex) {}
                pSet = b.get(pm);
                try {
                    pSet.add(new Pos(0,0));
                    return new Result("ERRORE Board.get(P pm): insieme modificabile");
                } catch (Exception ex) {}
                if (pSet.size() != 1 || !pSet.contains(p)) return new Result("ERRORE Board.get(P pm): insieme errato");
                pSet = b.get(new PieceModel<>(Species.BISHOP, "black"));
                if (!pSet.isEmpty()) return new Result("ERRORE Board.get(P pm): insieme errato");
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
                } catch (Exception ex) { return new Result("ERRORE Board.put(P pm,Pos p,Dir d,int n) "+ex.getMessage()); }
                b.put(new PieceModel<>(Species.KING, "white"), new Pos(1,2), Dir.UP_R, 5);
                pSet = b.get(new PieceModel<>(Species.KING, "white"));
                if (pSet.size() != 5) new Result("ERRORE Board.put(P pm,Pos p,Dir d,int n): insieme pos errato");
                for (Pos pp : new Pos[] {new Pos(1,2),new Pos(2,3),new Pos(3,4),new Pos(4,5),new Pos(5,6)})
                    if (!pSet.contains(pp)) new Result("ERRORE Board.put(P pm,Pos p,Dir d,int n): insieme pos errato");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_Action(float sc, int ms) {
        return runTest("Test Action", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Pos p = new Pos(0,0), p2 = new Pos(1,1);
                PieceModel<Species> pmNull = null;
                PieceModel<Species> pm = new PieceModel<>(Species.KING, "black");
                Action<PieceModel<Species>> a = new Action<>(p, pm);
                if (a.kind != Action.Kind.ADD || !pm.equals(a.piece) || a.pos == null ||
                        a.pos.size() != 1 || !a.pos.contains(p))
                    return new Result("ERRORE Action(Pos p, P pm)");
                try {
                    notEx(() -> new Action<>(null, pm), NullPointerException.class, "con p == null"+nE);
                    notEx(() -> new Action<>(new Pos(0,0), pmNull), NullPointerException.class, "con pm == null"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(Pos p, P pm) "+ex.getMessage()); }
                Action<PieceModel<Species>> a1 = new Action<>(p, pm);
                Action<PieceModel<Species>> a2 = new Action<>(p, pm);
                if (!a1.equals(a2)) return new Result("ERRORE Action.equals false invece di true");
                PieceModel<Species> pm2 = new PieceModel<>(Species.PAWN, "black");
                Action<PieceModel<Species>> a3 = new Action<>(p, pm2);
                if (a2.equals(a3)) return new Result("ERRORE Action.equals true invece di false");
                boolean herr = true;
                for (Species s : Species.values())
                    if (a1.hashCode() != new Action<>(p, new PieceModel<>(s, "a")).hashCode()) herr = false;
                if (herr) new Result("ERRORE Action.hashCode()");

                a = new Action<>(p);
                if (a.kind != Action.Kind.REMOVE || a.piece != null || a.pos == null || a.pos.size() != 1 || !a.pos.contains(p))
                    return new Result("ERRORE Action(Pos...pp)");
                try {
                    Action<PieceModel<Species>> af = a;
                    notEx(() -> af.pos.add(p), Exception.class, "la lista pos è modificabile");
                    notEx(() -> new Action<>(), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                    notEx(() -> new Action<>(p, (Pos)null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(Pos...pp) "+ex.getMessage()); }
                a1 = new Action<>(p);
                a2 = new Action<>(p);
                if (!a1.equals(a2)) return new Result("ERRORE Action.equals false invece di true");
                a3 = new Action<>(p, new Pos(1,1));
                if (a2.equals(a3)) return new Result("ERRORE Action.equals true invece di false");

                a = new Action<>(Dir.RIGHT, 2, p);
                if (a.kind != Action.Kind.MOVE || a.piece != null || a.pos == null ||
                        a.pos.size() != 1 || !a.pos.contains(p) || a.dir != Dir.RIGHT || a.steps != 2)
                    return new Result("ERRORE Action(Dir d,int ns,Pos...pp)");
                try {
                    Action<PieceModel<Species>> af = a;
                    notEx(() -> af.pos.add(p), Exception.class, "la lista pos è modificabile");
                    notEx(() -> new Action<>(Dir.RIGHT, 2), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2, p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2, p, null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                    notEx(() -> new Action<>(null, 2, p), NullPointerException.class, "con d null"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 0, p), IllegalArgumentException.class, "con ns < 1"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(Dir d,int ns,Pos...pp) "+ex.getMessage()); }
                a1 = new Action<>(Dir.UP, 3, p, p2);
                a2 = new Action<>(Dir.UP, 3, p, p2);
                if (!a1.equals(a2)) return new Result("ERRORE Action.equals false invece di true");
                a3 = new Action<>(Dir.UP, 3, p);
                if (a2.equals(a3)) return new Result("ERRORE Action.equals true invece di false");

                a = new Action<>(p, p2);
                if (a.kind != Action.Kind.JUMP || a.piece != null || a.pos == null ||
                        a.pos.size() != 2 || !p.equals(a.pos.get(0)) || !p2.equals(a.pos.get(1)))
                    return new Result("ERRORE Action(Pos p1,Pos p2)");
                try {
                    Action<PieceModel<Species>> af = a;
                    notEx(() -> af.pos.add(p), Exception.class, "la lista pos è modificabile");
                    notEx(() -> new Action<>((Pos)null, p2), NullPointerException.class, "con p1 null"+nE);
                    notEx(() -> new Action<>(p, (Pos)null), NullPointerException.class, "con p2 null"+nE);
                    notEx(() -> new Action<>(p, p), IllegalArgumentException.class, "con p1 == p2"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(Pos p1,Pos p2) "+ex.getMessage());}
                a1 = new Action<>(p, p2);
                a2 = new Action<>(p, p2);
                if (!a1.equals(a2)) return new Result("ERRORE Action.equals false invece di true");
                a3 = new Action<>(p, new Pos(4,1));
                if (a2.equals(a3)) return new Result("ERRORE Action.equals true invece di false");

                a = new Action<>(pm, p);
                if (a.kind != Action.Kind.SWAP || !pm.equals(a.piece) || a.pos == null ||
                        a.pos.size() != 1 || !p.equals(a.pos.get(0)))
                    return new Result("ERRORE Action(P pm,Pos...pp)");
                try {
                    Action<PieceModel<Species>> af = a;
                    notEx(() -> af.pos.add(p), Exception.class, "la lista pos è modificabile");
                    notEx(() -> new Action<>(pmNull, p), NullPointerException.class, "con pm null"+nE);
                    notEx(() -> new Action<>(pm), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(pm, p, null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                    notEx(() -> new Action<>(pm, p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(P pm,Pos...pp) "+ex.getMessage()); }
                a1 = new Action<>(pm, p, p2);
                a2 = new Action<>(pm, p, p2);
                if (!a1.equals(a2)) return new Result("ERRORE Action.equals false invece di true");
                a3 = new Action<>(pm, p, new Pos(4,1));
                if (a2.equals(a3)) return new Result("ERRORE Action.equals true invece di false");
                List<Pos> pp = Arrays.asList(new Pos(0,0),new Pos(1,1),new Pos(2,2),new Pos(1,4),new Pos(4,1));
                for (int i = 0 ; i < 10 ; i++) {
                    Action<PieceModel<Species>> aa1 = new Action<>(pp.toArray(new Pos[0]));
                    Collections.shuffle(pp);
                    Action<PieceModel<Species>> aa2 = new Action<>(pp.toArray(new Pos[0]));
                    if (!aa1.equals(aa2)) new Result("ERRORE Action.equals false invece di true");
                    if (aa1.hashCode() != aa1.hashCode()) new Result("ERRORE Action.hashCode diversi");
                }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_Move(float sc, int ms) {
        return runTest("Test Move", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Pos p = new Pos(0,0), p2 = new Pos(2,2);
                Move.Kind kNull = null;
                PieceModel<Species> pm = new PieceModel<>(Species.KING, "black");
                Action<PieceModel<Species>> a = new Action<>(p, p2), a2 = new Action<>(new Pos(1,1));
                Move<PieceModel<Species>> m = new Move<>(Move.Kind.PASS);
                if (!Move.Kind.PASS.equals(m.kind) || m.actions == null || m.actions.size() != 0)
                    return new Result("ERRORE Move(Kind k)");
                try {
                    Move<PieceModel<Species>> mf = m;
                    notEx(() -> mf.actions.add(a), Exception.class, "la lista actions è modificabile");
                    notEx(() -> new Move<>(kNull), NullPointerException.class, "con k null"+nE);
                    notEx(() -> new Move<>(Move.Kind.ACTION), IllegalArgumentException.class, "con k == ACTION"+nE);
                } catch (Exception ex) { return new Result("ERRORE Move(Kind k) "+ex.getMessage()); }
                Move<PieceModel<Species>> m1 = new Move<>(Move.Kind.RESIGN);
                Move<PieceModel<Species>> m2 = new Move<>(Move.Kind.RESIGN);
                if (!m1.equals(m2)) return new Result("ERRORE Move.equals false invece di true");
                Move<PieceModel<Species>> m3 = new Move<>(Move.Kind.PASS);
                if (m2.equals(m3)) return new Result("ERRORE Move.equals true invece di false");

                m = new Move<>(a, a2);
                if (!Move.Kind.ACTION.equals(m.kind) || m.actions == null ||
                        m.actions.size() != 2 || !a.equals(m.actions.get(0)) || !a2.equals(m.actions.get(1)))
                    return new Result("ERRORE Move(Action<P>...aa)");
                try {
                    Move<PieceModel<Species>> mf = m;
                    notEx(() -> mf.actions.add(a), Exception.class, "la lista actions è modificabile");
                    notEx(() -> new Move<>(a, null), NullPointerException.class, "con aa che contiene un elemento null"+nE);
                    notEx(() -> new Move<>(), IllegalArgumentException.class, "con aa vuoto"+nE);
                } catch (Exception ex) { return new Result("ERRORE Move(Kind k) "+ex.getMessage()); }
                m1 = new Move<>(a, a2);
                m2 = new Move<>(a, a2);
                if (!m1.equals(m2)) return new Result("ERRORE Move.equals false invece di true");
                m3 = new Move<>(a);
                if (m2.equals(m3)) return new Result("ERRORE Move.equals true invece di false");

                List<Action<PieceModel<Species>>> aa = Arrays.asList(a, a2);
                m = new Move<>(aa);
                if (!Move.Kind.ACTION.equals(m.kind) || m.actions == null ||
                        m.actions.size() != 2 || !a.equals(m.actions.get(0)) || !a2.equals(m.actions.get(1)))
                    return new Result("ERRORE Move(List<Action<P>> aa)");
                aa.set(0,a2);
                if (!a.equals(m.actions.get(0)) || !a2.equals(m.actions.get(1)))
                    return new Result("ERRORE Move(List<Action<P>> aa): mantiene la lista aa");
                try {
                    Move<PieceModel<Species>> mf = m;
                    notEx(() -> mf.actions.add(a), Exception.class, "la lista actions è modificabile");
                    notEx(() -> new Move<>(a, null), NullPointerException.class, "con aa che contiene un elemento null"+nE);
                    notEx(() -> new Move<>(), IllegalArgumentException.class, "con aa vuoto"+nE);
                } catch (Exception ex) { return new Result("ERRORE Move(Kind k) "+ex.getMessage()); }
                m1 = new Move<>(a, a2);
                m2 = new Move<>(a, a2);
                if (!m1.equals(m2)) return new Result("ERRORE Move.equals false invece di true");
                m3 = new Move<>(a);
                if (m2.equals(m3)) return new Result("ERRORE Move.equals true invece di false");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_GameRulerDef(float sc, int ms) {
        return runTest("Test GameRuler default methods", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Pos p = new Pos(0,0), p2 = new Pos(2,2), pNull = null;
                DummyGU gR = new DummyGU("a", "b");
                Set<Move<PieceModel<Species>>> vm = gR.validMoves();
                Move<PieceModel<Species>> last = null;
                for (Move<PieceModel<Species>> m : vm) {
                    if (!gR.isValid(m))
                        return new Result("ERRORE isValid(m) ritorna false invece di true");
                    last = m;
                }
                vm = gR.validMoves(last.actions.get(0).pos.get(0));
                if (vm == null || vm.size() != 1 || !vm.contains(last))
                    return new Result("ERRORE validMoves(p)");
                try {
                    Set<Move<PieceModel<Species>>> vm2 = vm;
                    Move<PieceModel<Species>> m = last;
                    notEx(() -> vm2.add(m), Exception.class, "l'insieme ritornato da validMoves(p) è modificabile");
                    notEx(() -> gR.isValid(null), NullPointerException.class, "isValid(null)"+nE);
                    notEx(() -> gR.validMoves(null), NullPointerException.class, "validMoves(null)"+nE);
                } catch (Exception ex) { return new Result("ERRORE "+ex.getMessage()); }
                gR.move(last);
                if (gR.isValid(last))
                    return new Result("ERRORE isValid(m) ritorna true invece di false");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_BoardOct(float sc, int ms) {
        return runTest("Test BoardOct", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Pos p = new Pos(0,0), p2 = new Pos(2,2), pNull = null;
                PieceModel<Species> pm = new PieceModel<>(Species.KING, "black"), pmNull = null;
                BoardOct<PieceModel<Species>> b = new BoardOct<>(8, 8);
                if (b.width() != 8 || b.height() != 8 || b.system() != Board.System.OCTAGONAL)
                    return new Result("ERRORE BoardOct(8,8): width() o height() o system()");
                if (!b.isModifiable())
                    return new Result("ERRORE BoardOct(8,8): isModifiable() ritorna false");
                List<Pos> lp = b.positions();
                try {
                    notEx(() -> lp.add(p2), Exception.class, "la lista ritornata da positions() è modificabile");
                } catch (Exception ex) { return new Result("ERRORE BoardOct(8,8): "+ex.getMessage()); }
                Pos pp = b.adjacent(new Pos(0,0), Dir.UP);
                if (!new Pos(0,1).equals(pp)) return new Result("ERRORE BoardOct(8,8): adjacent((0,0),UP) errato");
                pp = b.adjacent(new Pos(0,0), Dir.RIGHT);
                if (!new Pos(1,0).equals(pp)) return new Result("ERRORE BoardOct(8,8): adjacent((0,0),RIGHT) errato");
                pp = b.adjacent(new Pos(0,0), Dir.LEFT);
                if (pp != null) return new Result("ERRORE BoardOct(8,8): adjacent((0,0),LEFT) errato");
                p = new Pos(2,2);
                b.put(pm, p);
                if (!pm.equals(b.get(p))) return new Result("ERRORE BoardOct(8,8): put(P pm,Pos p) o get(Pos p) errato");
                b.remove(p);
                if (b.get(p) != null) return new Result("ERRORE BoardOct(8,8): remove(Pos p) errato");
                try {
                    notEx(() -> b.adjacent(pNull, Dir.UP), NullPointerException.class, "adjacent(null,d)"+nE);
                    notEx(() -> b.adjacent(new Pos(0,0), null), NullPointerException.class, "adjacent(p,null)"+nE);
                    notEx(() -> b.put(pm, new Pos(10,10)), IllegalArgumentException.class, "put(pm,(10,10))"+nE);
                } catch (Exception ex) { return new Result("ERRORE BoardOct(8,8): "+ex.getMessage()); }

                BoardOct<PieceModel<Species>> b2 = new BoardOct<>(12, 15, Arrays.asList(new Pos(0,0),new Pos(5,5)));
                if (b2.width() != 12 || b2.height() != 15 || b2.system() != Board.System.OCTAGONAL)
                    return new Result("ERRORE BoardOct(12,15): width() o height() o system()");
                if (!b2.isModifiable())
                    return new Result("ERRORE BoardOct(12,15): isModifiable() ritorna false");
                List<Pos> lp2 = b.positions();
                try {
                    notEx(() -> lp2.add(p2), Exception.class, "la lista ritornata da positions() è modificabile");
                } catch (Exception ex) { return new Result("ERRORE BoardOct(12,15): "+ex.getMessage()); }
                if (b2.isPos(new Pos(5,5))) return new Result("ERRORE BoardOct(12,5,{(0,0),(5,5)}): isPos((5,5)) errato");
                if (b2.adjacent(new Pos(4,4), Dir.UP_R) != null)
                    return new Result("ERRORE BoardOct(12,15,{(0,0),(5,5)}): adjacent((4,4),UP_R) errato");
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_UnmodifiableBoard(float sc, int ms) {
        return runTest("Test UnmodifiableBoard con BoardOct", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Pos p = new Pos(0,0), p2 = new Pos(2,2), pNull = null;
                PieceModel<Species> pm = new PieceModel<>(Species.KING, "black"), pmNull = null;
                Board<PieceModel<Species>> ub = Utils.UnmodifiableBoard(new BoardOct<>(8, 8));
                if (ub.isModifiable()) return new Result("ERRORE isModifiable() ritorna true");
                try {
                    notEx(() -> ub.put(pm, p), UnsupportedOperationException.class, "put(pm,p)"+nE);
                    notEx(() -> ub.put(pm, p, Dir.UP, 1), UnsupportedOperationException.class, "put(pm,p,UP,1)"+nE);
                    notEx(() -> ub.remove(p), UnsupportedOperationException.class, "remove(p)"+nE);
                } catch (Exception ex) { return new Result("ERRORE: "+ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_PlayRandPlayer(float sc, int ms) {
        return runTest("Test Play e RandPlayer", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                DummyGUFactory gF = new DummyGUFactory();
                Player<PieceModel<Species>> py1 = new RandPlayer<>("A");
                Player<PieceModel<Species>> py2 = new RandPlayer<>("B");
                GameRuler<PieceModel<Species>> gR = Utils.play(gF,py1,py2);
                if (gR.result() != 0) return new Result("ERRORE");
                try {
                    notEx(() -> Utils.play(null, py1, py2), NullPointerException.class, "play(null,p1,p2)"+nE);
                    notEx(() -> Utils.play(gF,py1), IllegalArgumentException.class, "play(gf,p1) quando il gioco richiede 2 giocatori"+nE);
                } catch (Exception ex) { return new Result("ERRORE: "+ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    /**************************  G R A D E  *****************************/

    private static boolean gradeTest2(boolean ok) {
        //if (!TOTAL_GRADE) return true;
        if (ok) ok = test_OthelloInit(0.2f, 1000);
        if (ok) ok = test_OthelloGame1(0.2f, 1000);
        if (ok) ok = test_OthelloGame2(0.4f, 2000);
        if (ok) ok = test_OthelloGame3(0.8f, 2000);
        if (ok) ok = test_OthelloUnMove(0.8f, 2000);
        if (ok) ok = test_OthelloCopy(0.4f, 1000);
        if (ok) ok = test_PlayOthelloRandPlayer(0.4f, 2000);
        if (ok) ok = test_Mix(0.8f, 1000);
        return ok;
    }

    private static boolean test_OthelloInit(float sc, int ms) {
        return runTest("Test Othello inizio", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Othello ot = new Othello("Alice", "Bob");
                if (!Objects.equals(Arrays.asList("Alice","Bob"), ot.players()))
                    return new Result("ERRORE players()");
                if (!Objects.equals(ot.color("Alice"), "nero") || !Objects.equals(ot.color("Bob"), "bianco"))
                    return new Result("ERRORE color(name)");
                if (ot.result() != -1) return new Result("ERRORE result()");
                if (!ot.isPlaying(1) || !ot.isPlaying(2)) return new Result("ERRORE isPlaying()");
                if (ot.turn() != 1) return new Result("ERRORE turn()");
                Board<PieceModel<Species>> board = ot.getBoard();
                for (int i = 0 ; i < 9 ; i++)
                    for (int j = 0 ; j < 9 ; j++)
                        if (i < 8 && j < 8) {
                            if (!board.isPos(new Pos(i, j)))
                                return new Result("ERRORE Board.isPos(p)");
                        } else if (board.isPos(new Pos(i, j)))
                            return new Result("ERRORE Board.isPos(p)");
                if (!checkOthelloBoard(board, new int[] {4,4, 3,3}, new int[] {3,4, 4,3}))
                    return new Result("ERRORE board iniziale errata");
                if (board.isModifiable())
                    return new Result("ERRORE Board.isModifiable() ritorna true");
                try {
                    notEx(() -> ot.color("nome errato"), IllegalArgumentException.class, "color(\"nome errato\")"+nE);
                    notEx(() -> ot.color(null), NullPointerException.class, "color(null)"+nE);
                    notEx(() -> ot.isPlaying(3), IllegalArgumentException.class, "isPlaying(3)"+nE);
                    notEx(() -> board.remove(new Pos(4,4)), UnsupportedOperationException.class, "Board.remove(p)"+nE);
                    notEx(() -> new Othello(null,null), NullPointerException.class, "new Othello(null,null)"+nE);
                } catch (Exception ex) { return new Result("ERRORE "+ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_OthelloGame1(float sc, int ms) {
        return test_OthelloGame(sc, ms, 8, "1", GAME_1);
    }

    private static boolean test_OthelloGame2(float sc, int ms) {
        return test_OthelloGame(sc, ms, 8, "2", GAME_2);
    }

    private static boolean test_OthelloGame3(float sc, int ms) {
        return test_OthelloGame(sc, ms, 6, "3", GAME_3);
    }

    private static boolean test_OthelloUnMove(float sc, int ms) {
        return runTest("Test Othello unMove", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Othello ot = new Othello("Alice", "Bob");
                Board<PieceModel<Species>> board = ot.getBoard();
                String[] game = GAME_1.split("\n");
                List<String> boards = new ArrayList<>();
                List<Move<PieceModel<Species>>> moves = new ArrayList<>();
                int k = 0;
                while (true) {
                    String sb = game[k++];
                    if (sb.length() != 64) {
                        if (Integer.parseInt(sb) != ot.result())
                            return new Result("ERRORE result()");
                        break;
                    }
                    if (!eqBoard(board, sb))
                        return new Result("ERRORE board");
                    boards.add(sb);
                    String svm = game[k++];
                    Set<Move<PieceModel<Species>>> vms = ot.validMoves();
                    if (!checkValidMoves(vms, svm))
                        return new Result("ERRORE validMoves()");
                    String[] tm = game[k++].split("\\s+");
                    if (ot.turn() != Integer.parseInt(tm[0]))
                        return new Result("ERRORE turn()");
                    Move<PieceModel<Species>> m = stringToMove(vms, tm[1]);
                    ot.move(m);
                    moves.add(m);
                    for (int i = moves.size() - 1 ; i >= 0 ; i--) {
                        ot.unMove();
                        if (!eqBoard(board, boards.get(i)))
                            return new Result("ERRORE board unMove "+moves.size()+" "+i);
                    }
                    for (int i = 0 ; i < moves.size() ; i++)
                        ot.move(moves.get(i));
                }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_OthelloCopy(float sc, int ms) {
        return runTest("Test Othello copy", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Othello ot = new Othello("Alice", "Bob");
                GameRuler<PieceModel<Species>> otCopy = ot.copy();
                Board<PieceModel<Species>> board = otCopy.getBoard();
                String[] game = GAME_1.split("\n");
                int k = 0;
                while (true) {
                    String sb = game[k++];
                    if (sb.length() != 64) {
                        if (Integer.parseInt(sb) != otCopy.result())
                            return new Result("ERRORE copy result()");
                        break;
                    }
                    if (!eqBoard(board, sb))
                        return new Result("ERRORE copy board");
                    String svm = game[k++];
                    Set<Move<PieceModel<Species>>> vms = otCopy.validMoves();
                    if (!checkValidMoves(vms, svm))
                        return new Result("ERRORE copy validMoves()");
                    String[] tm = game[k++].split("\\s+");
                    if (otCopy.turn() != Integer.parseInt(tm[0]))
                        return new Result("ERRORE copy turn()");
                    otCopy.move(stringToMove(vms, tm[1]));
                    if (!eqBoard(ot.getBoard(), game[0]))
                        return new Result("ERRORE original board");
                }
                try {
                    notEx(otCopy::validMoves, IllegalStateException.class, "validMoves() a gioco terminato"+nE);
                    notEx(() -> otCopy.move(new Move<>(Move.Kind.RESIGN)), IllegalStateException.class, "move(m) a gioco terminato"+nE);
                    notEx(() -> otCopy.isValid(new Move<>(Move.Kind.RESIGN)), IllegalStateException.class, "isValid(m) a gioco terminato"+nE);
                    notEx(() -> otCopy.validMoves(new Pos(0,0)), IllegalStateException.class, "validMoves(p) a gioco terminato"+nE);
                } catch (Exception ex) { return new Result("ERRORE "+ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_PlayOthelloRandPlayer(float sc, int ms) {
        return runTest("Test OthelloFactory con Play e RandPlayer", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                OthelloFactory oF = new OthelloFactory();
                Player<PieceModel<Species>> py1 = new RandPlayer<>("A");
                Player<PieceModel<Species>> py2 = new RandPlayer<>("B");
                GameRuler<PieceModel<Species>> gR = Utils.play(oF, py1, py2);
                if (gR.result() == -1) return new Result("ERRORE terminazione");
                try {
                    notEx(gR::validMoves, IllegalStateException.class, "validMoves() a gioco terminato"+nE);
                } catch (Exception ex) { return new Result("ERRORE: "+ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_Mix(float sc, int ms) {
        return runTest("Test Mix", sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                try {
                    notEx(() -> new Pos(0,-1), IllegalArgumentException.class, "new Pos(0,-1)"+nE);
                } catch (Exception ex) { return new Result("ERRORE: "+ex.getMessage()); }
                DummyBoard<PieceModel<Species>> b = new DummyBoard<>();
                PieceModel<Species> pm = new PieceModel<>(Species.BISHOP, "white");
                PieceModel<Species> pmNull = null;
                try {
                    notEx(() -> b.put(pmNull, new Pos(0,0), Dir.UP, 1), NullPointerException.class, "con pm == null non lancia eccezione");
                    notEx(() -> b.put(pm, null, Dir.UP, 1), NullPointerException.class, "con p == null non lancia eccezione");
                    notEx(() -> b.put(pm, new Pos(0,0), null, 1), NullPointerException.class, "con d == null non lancia eccezione");
                    notEx(() -> b.put(pm, new Pos(0,0), Dir.UP, 0), IllegalArgumentException.class, "con n == 0 non lancia eccezione");
                    String err = "se una posizione della linea non è nella board non lancia eccezione";
                    notEx(() -> b.put(pm, new Pos(0,0), Dir.UP, 15), IllegalArgumentException.class, err);
                    notEx(() -> b.put(pm, new Pos(3,0), Dir.DOWN, 2), IllegalArgumentException.class, err);
                    notEx(() -> b.put(pm, new Pos(10,0), Dir.UP, 1), IllegalArgumentException.class, err);
                } catch (Exception ex) { return new Result("ERRORE Board.put(P pm,Pos p,Dir d,int n) "+ex.getMessage()); }
                try {
                    notEx(() -> new Action<>(null, pm), NullPointerException.class, "con p == null"+nE);
                    notEx(() -> new Action<>(new Pos(0,0), pmNull), NullPointerException.class, "con pm == null"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(Pos p, P pm) "+ex.getMessage()); }
                Pos p = new Pos(0,0), p2 = new Pos(2,2), pNull = null;
                Action<PieceModel<Species>> af = new Action<>(p);
                try {
                    notEx(() -> af.pos.add(p), Exception.class, "la lista pos è modificabile, non lancia");
                    notEx(Action::new, IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(new Pos[] {p,p}), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                    notEx(() -> new Action<>(new Pos[] {p,(Pos)null}), NullPointerException.class, "con pp che contiene elementi null"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(Pos...pp) "+ex.getMessage()); }
                Action<PieceModel<Species>> af2 = new Action<>(Dir.RIGHT, 2, p);
                try {
                    notEx(() -> af2.pos.add(p), Exception.class, "la lista pos è modificabile, non lancia");
                    notEx(() -> new Action<>(Dir.RIGHT, 2), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2, p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 2, p, null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                    notEx(() -> new Action<>(null, 2, p), NullPointerException.class, "con d null"+nE);
                    notEx(() -> new Action<>(Dir.RIGHT, 0, p), IllegalArgumentException.class, "con ns < 1"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(Dir d,int ns,Pos...pp) "+ex.getMessage()); }
                Action<PieceModel<Species>> af3 = new Action<>(p, p2);
                try {
                    notEx(() -> af3.pos.add(p), Exception.class, "la lista pos è modificabile, non lancia");
                    notEx(() -> new Action<>((Pos)null, p2), NullPointerException.class, "con p1 null"+nE);
                    notEx(() -> new Action<>(p, (Pos)null), NullPointerException.class, "con p2 null"+nE);
                    notEx(() -> new Action<>(p, p), IllegalArgumentException.class, "con p1 == p2"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(Pos p1,Pos p2) "+ex.getMessage());}
                Action<PieceModel<Species>> af4 = new Action<>(pm, p);
                try {
                    notEx(() -> af4.pos.add(p), Exception.class, "la lista pos è modificabile, non lancia");
                    notEx(() -> new Action<>(pmNull, p), NullPointerException.class, "con pm null"+nE);
                    notEx(() -> new Action<>(pm), IllegalArgumentException.class, "con pp vuoto"+nE);
                    notEx(() -> new Action<>(pm, p, null), NullPointerException.class, "con pp che contiene elementi null"+nE);
                    notEx(() -> new Action<>(pm, p, p), IllegalArgumentException.class, "con pp che contiene duplicati"+nE);
                } catch (Exception ex) { return new Result("ERRORE Action(P pm,Pos...pp) "+ex.getMessage()); }
                Move.Kind kNull = null;
                Action<PieceModel<Species>> a = new Action<>(p, p2), a2 = new Action<>(new Pos(1,1));
                Move<PieceModel<Species>> m = new Move<>(Move.Kind.PASS);
                try {
                    notEx(() -> m.actions.add(a), Exception.class, "la lista actions è modificabile, non lancia");
                    notEx(() -> new Move<>(kNull), NullPointerException.class, "con k null"+nE);
                    notEx(() -> new Move<>(Move.Kind.ACTION), IllegalArgumentException.class, "con k == ACTION"+nE);
                } catch (Exception ex) { return new Result("ERRORE Move(Kind k) "+ex.getMessage()); }
                Move<PieceModel<Species>> mf = new Move<>(a, a2);
                try {
                    notEx(() -> mf.actions.add(a), Exception.class, "la lista actions è modificabile, non lancia");
                    notEx(() -> new Move<>(a, null), NullPointerException.class, "con aa che contiene un elemento null"+nE);
                    notEx(Move<PieceModel<Species>>::new, IllegalArgumentException.class, "con aa vuoto"+nE);
                } catch (Exception ex) { return new Result("ERRORE Move(Action<P>...aa) "+ex.getMessage()); }
                List<Action<PieceModel<Species>>> aa = Arrays.asList(a, a2), aaNull = null;
                Move<PieceModel<Species>> mf2 = new Move<>(aa);
                aa.set(0,a2);
                try {
                    notEx(() -> mf2.actions.add(a), Exception.class, "la lista actions è modificabile, non lancia");
                    notEx(() -> new Move<>(new ArrayList<>()), IllegalArgumentException.class, "con aa vuota"+nE);
                    notEx(() -> new Move<>(aaNull), NullPointerException.class, "con aa null"+nE);
                    notEx(() -> new Move<>(Arrays.asList(a, null, a2)), NullPointerException.class, "con aa che contiene un elemento null"+nE);
                } catch (Exception ex) { return new Result("ERRORE Move(List<Action<P>> aa) "+ex.getMessage()); }
                DummyGU gR = new DummyGU("a", "b");
                Set<Move<PieceModel<Species>>> vm = gR.validMoves();
                Move<PieceModel<Species>> last = null;
                for (Move<PieceModel<Species>> m2 : vm) last = m2;
                vm = gR.validMoves(last.actions.get(0).pos.get(0));
                if (vm == null || vm.size() != 1 || !vm.contains(last))
                    return new Result("ERRORE validMoves(p)");
                try {
                    Set<Move<PieceModel<Species>>> vm2 = vm;
                    Move<PieceModel<Species>> m3 = last;
                    notEx(() -> vm2.add(m3), Exception.class, "l'insieme ritornato da validMoves(p) è modificabile, non lancia");
                    notEx(() -> gR.isValid(null), NullPointerException.class, "isValid(null)"+nE);
                    notEx(() -> gR.validMoves(null), NullPointerException.class, "validMoves(null)"+nE);
                } catch (Exception ex) { return new Result("ERRORE "+ex.getMessage()); }
                BoardOct<PieceModel<Species>> b88 = new BoardOct<>(8, 8);
                List<Pos> lp = b88.positions();
                try {
                    notEx(() -> lp.add(p2), Exception.class, "la lista ritornata da positions() è modificabile, non lancia");
                    notEx(() -> b88.adjacent(pNull, Dir.UP), NullPointerException.class, "adjacent(null,d)"+nE);
                    notEx(() -> b88.adjacent(new Pos(0,0), null), NullPointerException.class, "adjacent(p,null)"+nE);
                    notEx(() -> b88.put(pm, new Pos(10,10)), IllegalArgumentException.class, "put(pm,(10,10))"+nE);
                } catch (Exception ex) { return new Result("ERRORE BoardOct(8,8): "+ex.getMessage()); }
                Board<PieceModel<Species>> ub = Utils.UnmodifiableBoard(new BoardOct<>(8,8));
                try {
                    notEx(() -> ub.put(pm, p), UnsupportedOperationException.class, "put(pm,p)"+nE);
                    notEx(() -> ub.put(pm, p, Dir.UP, 1), UnsupportedOperationException.class, "put(pm,p,UP,1)"+nE);
                    notEx(() -> ub.remove(p), UnsupportedOperationException.class, "remove(p)"+nE);
                } catch (Exception ex) { return new Result("ERRORE UnmodifiableBoard: "+ex.getMessage()); }
                DummyGUFactory gF = new DummyGUFactory();
                Player<PieceModel<Species>> py1 = new RandPlayer<>("A");
                Player<PieceModel<Species>> py2 = new RandPlayer<>("B");
                if (!Objects.equals(py1.name(), "A"))
                    return new Result("ERRORE: Player name()");
                try {
                    notEx(() -> new RandPlayer<PieceModel<Species>>(null), NullPointerException.class, "new RandPlayer(null)"+nE);
                    notEx(() -> Utils.play(null,py1,py2), NullPointerException.class, "Utils.play(null,p1,p2)"+nE);
                    notEx(() -> Utils.play(gF,py1), IllegalArgumentException.class, "Utils.play(gf,p1) quando il gioco richiede 2 giocatori"+nE);
                } catch (Exception ex) { return new Result("ERRORE: "+ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
    }

    private static boolean test_OthelloGame(float sc, int ms, int size, String gName, String g) {
        return runTest("Test Othello"+size+"x"+size+" partita "+gName, sc, ms, () -> {
            try {
                String nE = " non lancia eccezione";
                Othello ot = new Othello(-1, size, "Alice", "Bob");
                Board<PieceModel<Species>> board = ot.getBoard();
                String[] game = g.split("\n");
                int k = 0;
                while (true) {
                    String sb = game[k++];
                    if (sb.length() != size*size) {
                        if (Integer.parseInt(sb) != ot.result())
                            return new Result("ERRORE result()");
                        break;
                    }
                    if (!eqBoard(board, sb))
                        return new Result("ERRORE board");
                    String svm = game[k++];
                    Set<Move<PieceModel<Species>>> vms = ot.validMoves();
                    if (!checkValidMoves(vms, svm))
                        return new Result("ERRORE validMoves()");
                    String[] tm = game[k++].split("\\s+");
                    if (ot.turn() != Integer.parseInt(tm[0]))
                        return new Result("ERRORE turn()");
                    ot.move(stringToMove(vms, tm[1]));
                }
                try {
                    notEx(ot::validMoves, IllegalStateException.class, "validMoves() a gioco terminato"+nE);
                    notEx(() -> ot.move(new Move<>(Move.Kind.RESIGN)), IllegalStateException.class, "move(m) a gioco terminato"+nE);
                    notEx(() -> ot.isValid(new Move<>(Move.Kind.RESIGN)), IllegalStateException.class, "isValid(m) a gioco terminato"+nE);
                    notEx(() -> ot.validMoves(new Pos(0,0)), IllegalStateException.class, "validMoves(p) a gioco terminato"+nE);
                } catch (Exception ex) { return new Result("ERRORE "+ex.getMessage()); }
            } catch (Throwable t) { return handleThrowable(t); }
            return new Result(); });
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
        PieceModel<Species> pm = new PieceModel<>(Species.DISC, (c.equals("X") ? "nero" : "bianco"));
        Action<PieceModel<Species>> add = new Action<>(cc.get(0), pm);
        cc.remove(0);
        Action<PieceModel<Species>> swap = new Action<>(pm, cc.toArray(new Pos[cc.size()]));
        Move<PieceModel<Species>> m = new Move<>(add, swap);
        if (vms == null) return m;
        for (Move<PieceModel<Species>> vm : vms)
            if (Objects.equals(vm, m)) {
                m = vm;
                break;
            }
        return m;
    }

    private static <P> boolean eqBoard(Board<P> board, String sb) {
        PieceModel<Species> pN = new PieceModel<>(Species.DISC, "nero"),
                pB = new PieceModel<>(Species.DISC, "bianco");
        int k = 0;
        for (int b = 0 ; b < board.width() ; b++)
            for (int t = 0 ; t < board.width() ; t++) {
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
                //found = eqMoves(m, cm);
                found = Objects.equals(m, cm);
                if (found) break;
            }
            if (!found) return false;
        }
        return true;
    }

    private static final String GAME_1 = "...........................XO......OX...........................\n" +
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
    private static final String GAME_2 = "...........................XO......OX...........................\n" +
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

    private static final String GAME_3 = "..............XO....OX..............\n" +
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

    private static boolean checkOthelloBoard(Board<PieceModel<Species>> board, int[] neri, int[] bianchi) {
        Set<Pos> neriSet = arrayToSetPos(neri), bianchiSet = arrayToSetPos(bianchi);
        PieceModel<Species> pN = new PieceModel<>(Species.DISC, "nero"),
                pB = new PieceModel<>(Species.DISC, "bianco");
        for (int i = 0 ; i < 8 ; i++)
            for (int j = 0 ; j < 8 ; j++) {
                Pos p = new Pos(i, j);
                PieceModel<Species> pm = board.get(p);
                if (neriSet.contains(p)) {
                    if (!pN.equals(pm)) return false;
                } else if (bianchiSet.contains(p)) {
                    if (!pB.equals(pm)) return false;
                } else if (pm != null)
                    return false;
            }
        return true;
    }

    private static Set<Pos> arrayToSetPos(int[] pp) {
        Set<Pos> sp = new HashSet<>();
        for (int i = 0 ;  i < pp.length ; i+=2)
            sp.add(new Pos(pp[i], pp[i+1]));
        return sp;
    }

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

    private static final boolean TOTAL_GRADE = true;
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

    private static void print(Result r) {
        System.out.println(!r.fatal && r.err == null ? "OK" : r.err);
    }
    private static void print(String m) { System.out.print(m); }
    private static void println(String m) { print(m+"\n"); }

    private static Result handleThrowable(Throwable t) {
        String msg = "";
        boolean fatal = false;
        if (t instanceof Exception) {
            Exception ex = (Exception)t;
            StackTraceElement[] st = ex.getStackTrace();
            String s = st.length > 0 ? st[0].toString() : "";
            msg += "Eccezione inattesa"+(!s.isEmpty() ? " lanciata da "+s+": " : ": ");
        } else {
            msg += "ERRORE GRAVE, impossibile continuare il test: ";
            fatal = true;
        }
        return new Result(msg+t, fatal);
    }

    private static boolean runTest(String msg, float score, int ms, Callable<Result> test) {
        FutureTask<Result> future = new FutureTask<>(test);
        Thread t = new Thread(future);
        t.setDaemon(true);
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
        if (res == null)
            println("ERRORE: limite di tempo superato ("+ms+" ms)");
        else if (res.fatal) {
            println(res.err);
            return false;
        } else if (res.err != null) {
            println(res.err);
        } else {
            println(" score "+score);
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
    private static volatile boolean testing = false;
    private static volatile String testWord = null;
    private static final Random RND = new Random();
}
