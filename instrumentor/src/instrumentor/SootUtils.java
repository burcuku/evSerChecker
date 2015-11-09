package instrumentor;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import java.util.Iterator;

public class SootUtils {

    public static InvokeStmt specialInvocation(SootMethod m, Local tmpRef, Value... args) {
        SootMethod toCall = Scene.v().getSootClass("java.io.PrintStream").getMethod("void println(java.lang.String)");
        return Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), args));
    }

    public static InvokeStmt staticInvocation(SootMethod m, Value... args) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef(), args));
    }
    
    public static InvokeStmt staticInvocation(SootMethod m) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef()));
    }

    public static InvokeStmt staticInvocation(SootMethod m, Local arg) {
        return Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(m.makeRef(),arg));
    }
    
    public static boolean hasParentClass(SootClass clazz, SootClass ancestor) {
        if(clazz == ancestor)
            return true;
        if(clazz.getName().equalsIgnoreCase("java.lang.Object"))
            return false;

        return hasParentClass(clazz.getSuperclass(), ancestor);
    }

    public static Local addTmpRef(Body body, String s)
    {
        Local tmpRef = Jimple.v().newLocal(s, RefType.v("java.io.PrintStream"));
        body.getLocals().add(tmpRef);
        return tmpRef;
    }

    public static Local addTmpBool(Body body, String s)
    {
        Local tmpBool = Jimple.v().newLocal(s, BooleanType.v());
        body.getLocals().add(tmpBool);
        return tmpBool;
    }

    public static Local addTmpString(Body body, String s)
    {
        Local tmpString = Jimple.v().newLocal(s, RefType.v("java.lang.String"));
        body.getLocals().add(tmpString);
        return tmpString;
    }

    public static Local addTmpPrimitiveInt(Body body, String s)
    {
        Local tmpInt =  Jimple.v().newLocal(s, IntType.v());
        body.getLocals().add(tmpInt);
        return tmpInt;
    }

    public static Stmt getLastIdentityStmt(Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        Stmt lastIdentityStmt = null, firstNonIdentityStmt = null;
        firstNonIdentityStmt = (Stmt)iter.next();

        while(iter.hasNext() && (firstNonIdentityStmt instanceof IdentityStmt)) {
            lastIdentityStmt = firstNonIdentityStmt;
            firstNonIdentityStmt = (Stmt)iter.next();
        }

        return lastIdentityStmt;
    }

    public static boolean hasReturningTrap(Body b) {
        Chain<Trap> traps = b.getTraps();

        for(Trap t:traps) {
            Unit endUnit = t.getEndUnit();
            //System.out.println("=================End unit: " + endUnit.toString());
            if(endUnit.toString().contains("return")) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasSynchronizedBlock(Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        while(iter.hasNext()) {
            Stmt s = (Stmt)iter.next();
            if(s instanceof MonitorStmt) return true;
        }

        return false;
    }
}
