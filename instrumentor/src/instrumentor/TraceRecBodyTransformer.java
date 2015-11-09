package instrumentor;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JIdentityStmt;
import soot.util.Chain;

import java.util.*;

public class TraceRecBodyTransformer extends BodyTransformer {

    private SootClass eventUtilitiesClass, recorderUtilitiesClass, systemClass, integerClass;

    private SootMethod initRecorderUtilitiesMethod, recordMethod, identityHashCodeMethod, toHexMethod;
    private SootMethod initEventUtilitiesMethod, preProcessEHForEventIdMethod, postProcessEHForEventIdMethod, getEventOfCurrentThreadMethod,
            setCurrentThreadToObjectMethod, setObjectToCurrentThreadMethod, resetCurrentThreadFromObjectMethod;
    private SootMethod logObjectEventsMethod, logThreadEventsMethod, setRandomDelayMethod, setDefaultExceptionHandlerMethod;

    private Local hashInt = null, hashStr = null, accessTypeStr = null, objNameStr = null, fieldNameStr = null, methodNameStr = null; // to be used in recording accesses
    private Local methodNameLocal = null, classNameLocal = null; // to be used in set object/thread id

    private boolean isRWLocalsDeclared = false;
    private boolean isEventLocalsDeclared = false;
    private String packageName;

    private Set<String> lifeCycleMethods =  new HashSet<String>();
    private boolean initiated = false;

    // stat variables
    private int numEventHandlers = 0, numLifeCycleHandlers = 0;
    private int numAsyncTasks = 0, numRunnables = 0, numMessageHandlers = 0, numIntentHandlers = 0;

    public static void main(String[] args) {
        // args[0]: directory from which to process classes
        // args[1]: path for finding the android.jar file
        // args[2]: package name of the app to be instrumented

        if(args.length < 3) {
            System.out.println("Please enter the args: <process-dir> <android-jars> <app-package-name>");
            return;
        }

        TraceRecBodyTransformer transformer = new TraceRecBodyTransformer(args[2]);

        PackManager.v().getPack("jtp").add(
                new Transform("jtp.myInstrumenter", transformer));

        soot.Main.main(new String[]{
                "-debug",
                "-prepend-classpath",
                "-process-dir", args[0],
                "-android-jars", args[1],
                "-src-prec", "apk",
                "-output-format", "dex",
                "-allow-phantom-refs"
        });

        transformer.printStats();
    }

    public TraceRecBodyTransformer(String packName) {
        packageName = packName;
    }

    public void initTransformer() {

        Scene.v().addBasicClass("recorder.EventMapUtilities", SootClass.BODIES);
        eventUtilitiesClass = Scene.v().getSootClass("recorder.EventMapUtilities");
        eventUtilitiesClass.setApplicationClass();

        Scene.v().addBasicClass("recorder.TraceRecorderUtilities", SootClass.BODIES);
        recorderUtilitiesClass = Scene.v().getSootClass("recorder.TraceRecorderUtilities");
        recorderUtilitiesClass.setApplicationClass();

        systemClass = Scene.v().getSootClass("java.lang.System");
        integerClass = Scene.v().getSootClass("java.lang.Integer");

        initRecorderUtilitiesMethod = recorderUtilitiesClass.getMethodByName("initRecorderUtilities");
        recordMethod = recorderUtilitiesClass.getMethodByName("invokeRecord");


        identityHashCodeMethod = systemClass.getMethodByName("identityHashCode");
        toHexMethod = integerClass.getMethodByName("toHexString");

        initEventUtilitiesMethod = eventUtilitiesClass.getMethodByName("initEventUtilities");

        getEventOfCurrentThreadMethod = eventUtilitiesClass.getMethodByName("getEventOfCurrentThread");

        setCurrentThreadToObjectMethod = eventUtilitiesClass.getMethodByName("invokeSetCurrentThreadToObject");
        setObjectToCurrentThreadMethod = eventUtilitiesClass.getMethodByName("invokeSetObjectToCurrentThread");
        resetCurrentThreadFromObjectMethod =  eventUtilitiesClass.getMethodByName("resetCurrentThreadFromObject");

        logObjectEventsMethod = eventUtilitiesClass.getMethodByName("invokeLogObjectEvents");
        logThreadEventsMethod = eventUtilitiesClass.getMethodByName("invokeLogThreadEvents");

        setRandomDelayMethod = eventUtilitiesClass.getMethodByName("setRandomDelay");
        setDefaultExceptionHandlerMethod = eventUtilitiesClass.getMethodByName("invokeSetDefaultExceptionHandler");

        preProcessEHForEventIdMethod = eventUtilitiesClass.getMethodByName("invokePreProcessEHForEventId");
        postProcessEHForEventIdMethod = eventUtilitiesClass.getMethodByName("invokePostProcessEHForEventId");

        fillLifeCycleMethods();
    }


    public void fillLifeCycleMethods() {
        lifeCycleMethods.add("onAttach");
        lifeCycleMethods.add("onCreate");
        lifeCycleMethods.add("onCreateView"); // draw view and return it
        lifeCycleMethods.add("onActivityCreated");
        lifeCycleMethods.add("onRestart");
        lifeCycleMethods.add("onStart");
        lifeCycleMethods.add("onResume");
        lifeCycleMethods.add("onPause");
        lifeCycleMethods.add("onStop");
        lifeCycleMethods.add("onDestroyView");
        lifeCycleMethods.add("onDestroy");
        lifeCycleMethods.add("onDetach");
    }

    @Override
    protected void internalTransform(final Body b, String phaseName,
                                     @SuppressWarnings("rawtypes") Map options) {

        if(!initiated) {
            initTransformer();
            initiated = true;
        }

        String className = b.getMethod().getDeclaringClass().toString();
        String methodName = b.getMethod().getName();
        SootClass clazz = b.getMethod().getDeclaringClass();

        if (!(className.startsWith(packageName)))
            return;

        SootClass activityClass = Scene.v().getSootClass("android.app.Activity");
        SootClass applicationClass = Scene.v().getSootClass("android.app.Application");
        SootClass asyncTaskClass = Scene.v().getSootClass("android.os.AsyncTask");
        SootClass handlerClass = Scene.v().getSootClass("android.os.Handler");
        SootClass serviceClass = Scene.v().getSootClass("android.app.Service");

        // NOTE: Problematic insert after last identity statement for the methods with synchronized blocks
        if(SootUtils.hasSynchronizedBlock(b))  return;

        if (isEventHandler(methodName, clazz)) {
            instrumentEventHandlerMethod(b, methodName, className);
            numEventHandlers++;

        } else if (isLifeCycleEventHandler(methodName, clazz)) {
            instrumentEventHandlerMethod(b, methodName, className);
            numLifeCycleHandlers++;

            if (methodName.equals("onCreate") && (SootUtils.hasParentClass(clazz, activityClass) || instrumentor.SootUtils.hasParentClass(clazz, applicationClass))) {
                instrumentOnCreateMethod(b);
            }
        }

        isRWLocalsDeclared = false;
        instrumentForRW(b);

        // instrument to assign event id of the object to the running thread:
            // instrument AsyncTask executions - set if of AsyncTask to the main thread
        if ((methodName.equals("doInBackground") || methodName.equals("onPostExecute") || methodName.equals("onProgressUpdate") || methodName.equals("onCancelled")) && instrumentor.SootUtils.hasParentClass(clazz, asyncTaskClass)
            // instrument runnable executions - set id of Runnable to the running thread
            || (methodName.equals("run") && clazz.implementsInterface("java.lang.Runnable"))
            // instrument handleMessage executions - set id of Message to the running thread
            || (methodName.equals("handleMessage") && SootUtils.hasParentClass(clazz, handlerClass))
            // instrument onHandleIntent executions - set id of Intent to the running thread
            || (methodName.equals("onHandleIntent") && SootUtils.hasParentClass(clazz, serviceClass))
            // instrument onStartCommand executions - set id of Intent to the running thread
            || (methodName.equals("onStartCommand") && SootUtils.hasParentClass(clazz, serviceClass))) {

            instrumentToSetThreadToObject(b, methodName, className);

            if(methodName.equals("doInBackground")) numAsyncTasks ++;
            else if(methodName.equals("run")) numRunnables ++;
            else if(methodName.equals("handleMessage")) numMessageHandlers ++;
            else if(methodName.equals("onHandleIntent")) numIntentHandlers ++;
        }

        // instrument to assign event id of the thread to an object: AsyncTask.execute, Handler.post, Handler.sendMessage, Message.sendToTarget, View.post
        // or any Runnable object creation
        isEventLocalsDeclared = false;
        instrumentToSetObjectToThread(b, methodName, className);

        b.validate();
    }

    private boolean isEventHandler(String methodName, SootClass clazz) {

        SootClass viewClass = Scene.v().getSootClass("android.view.View");

        if (methodName.startsWith("on")) {

            if (clazz.getName().endsWith("Listener")) {
                //System.out.println("Method of a listener: " + methodName + " ----");
                return true;
            }

            if (!(methodName.equals("onDraw")) && SootUtils.hasParentClass(clazz, viewClass)) {
                //System.out.println("Event handler of a View: " + methodName + " ----");
                return true;
            }

            SootClass activityClass = Scene.v().getSootClass("android.app.Activity");
            if(SootUtils.hasParentClass(clazz, activityClass) && !lifeCycleMethods.contains(methodName)) {
                //System.out.println("Event handler in an Activity: " + methodName);
                return true;
            }

            SootClass broadcastReceiverClass = Scene.v().getSootClass("android.content.BroadcastReceiver");
            if(methodName.equals("onReceive") && SootUtils.hasParentClass(clazz, broadcastReceiverClass)) {
                //System.out.println("Event handler of BroadcastReceiver: " + methodName);
                return true;
            }

            //System.out.println("-----Checking parent: " + methodName + " " + clazz.getName());
            // Checking parent: onItemClick com.vlille.checker.ui.fragment.StationsListFragment$1
            // android.widget.AdapterView$OnItemClickListener
            Chain<SootClass> cc = clazz.getInterfaces();
            for (SootClass sc : cc) {
                // if no second check, Activity that implements a listener -> onCreate as countered as an event
                // third check - Listeners onXXX defined in the app itself are synch
                if (sc.getName().endsWith("Listener") && (sc.declaresMethodByName(methodName) && !sc.getName().startsWith(packageName))) {
                    //System.out.println("Event in an interface listener: " + methodName + " " + sc.getName());
                    return true;
                }
            }
        }

        if(clazz.implementsInterface("android.text.TextWatcher") && (methodName.equals("beforeTextChanged") ||  methodName.equals("onTextChanged") || methodName.equals("afterTextChanged"))) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the method is a lifecycle method of
     * Activity, Fragment or Application classes
     * @param methodName
     * @param clazz
     * @return
     */
    public boolean isLifeCycleEventHandler(String methodName, SootClass clazz) {

        SootClass applicationClass = Scene.v().getSootClass("android.app.Application");
        SootClass activityClass = Scene.v().getSootClass("android.app.Activity");
        SootClass fragmentClass = Scene.v().getSootClass("android.app.Fragment");
        SootClass supportFragmentClass = Scene.v().getSootClass("android.app.ListFragment");
        SootClass supportFragmentListClass = Scene.v().getSootClass("android.support.v4.app.ListFragment");

        if (lifeCycleMethods.contains(methodName) && (SootUtils.hasParentClass(clazz, activityClass) || SootUtils.hasParentClass(clazz, applicationClass))) {
            System.out.println("Lifecycle callback of an Activity/Application: " + methodName + " ----");
            return true;
        }

        if (lifeCycleMethods.contains(methodName) && (SootUtils.hasParentClass(clazz, fragmentClass)
                || SootUtils.hasParentClass(clazz, supportFragmentClass) || SootUtils.hasParentClass(clazz, supportFragmentListClass))) {
            if(methodName.equals("onCreateView") || methodName.equals("onDestroyView")) { // view related
                return false;
            }
            System.out.println("Lifecycle callback of a Fragment: " + methodName + " -----");
            return true;
        }

        return false;
    }

    /**
     * Inserts initializer statements for the trace recorder and event utility classes
     * @param b Method body
     */
    public void instrumentOnCreateMethod(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();

        // NOTE: Insert before first nonidentity statement - inserts inside the label
        // (e.g. if the first stmt is in try/catch block)
        // Insert after last identity statement - inserts outside the label
        //Stmt stmt = ((JimpleBody) b).getFirstNonIdentityStmt();
        Stmt stmt = SootUtils.getLastIdentityStmt(b);

        units.insertAfter(SootUtils.staticInvocation(initRecorderUtilitiesMethod, b.getThisLocal()), stmt);
        units.insertAfter(SootUtils.staticInvocation(initEventUtilitiesMethod), stmt);
        System.out.println("=========== Initializing stmts added..");
    }


    /**
     * Reads the event id of the object (AsyncTask, Runnable, Message or Intent) and set it to the currently executing thread
     * @param b Method body
     * @param methodName Name of the asynchronous task method
     * @param className Class name that declares the asynchronous task method
     */
    private void instrumentToSetThreadToObject(final Body b, final String methodName, final String className) {
        final PatchingChain<Unit> units = b.getUnits();
        final Value objectHavingEventId;

        Iterator<Unit> iter = units.snapshotIterator();

        // if instrumenting handleMessage, then the object is the first parameter (Message)
        if(methodName.equals("handleMessage") || methodName.equals("onHandleIntent") || methodName.equals("onStartCommand")) {
            // get the first parameter, which is a Message (or Intent)
            // method: public void handleMessage(Message m)
            iter.next(); // the identity statement for the method
            // the identity statement for the first parameter - Message
            JIdentityStmt stmt = (JIdentityStmt) iter.next();
            objectHavingEventId = stmt.getLeftOp();

        // if instrumenting AsyncTask methods or a runnable, then the object is "this
        // also if instrumenting a Thread, then, the object is "this" e.g. "Thread"
        } else {
            objectHavingEventId = b.getThisLocal();
        }

        // NOTE: Insert before first nonidentity statement - inserts inside the label
        // (e.g. if the first stmt is in try/catch block)
        // Insert after last identity statement - inserts outside the label
        //Stmt stmt = ((JimpleBody) b).getFirstNonIdentityStmt();
        Stmt stmt = SootUtils.getLastIdentityStmt(b);

        final Local eventIdBeforeObject = SootUtils.addTmpPrimitiveInt(b, "rob_eventIdBeforeObject");
        InvokeExpr expr = Jimple.v().newStaticInvokeExpr(getEventOfCurrentThreadMethod.makeRef());
        final Local methodNameStr = SootUtils.addTmpString(b, "rob_methName");
        final Local classNameStr = SootUtils.addTmpString(b, "rob_className");

        // The stmt insertion order has changed
        units.insertAfter(SootUtils.staticInvocation(setCurrentThreadToObjectMethod, objectHavingEventId, methodNameStr, classNameStr), stmt);
        units.insertAfter(SootUtils.staticInvocation(setDefaultExceptionHandlerMethod), stmt);

        if(methodName.equals("doInBackground") || methodName.equals("run")) {
            units.insertAfter(SootUtils.staticInvocation(setRandomDelayMethod, objectHavingEventId, methodNameStr, classNameStr), stmt);
        }
        units.insertAfter(Jimple.v().newAssignStmt(classNameStr, StringConstant.v(className)), stmt);
        units.insertAfter(Jimple.v().newAssignStmt(methodNameStr, StringConstant.v(methodName)), stmt);
        units.insertAfter(Jimple.v().newAssignStmt(eventIdBeforeObject, expr), stmt);

        System.out.println("===========Stmt added to: Set thread event id to event id in method: " + methodName + " of class: " + className);

        // do not reset the event id after onPostExecute or onCancelled
        // it will be reset inside AsyncTask.finish after logging mStatus accesses
        if(methodName.equals("onPostExecute") || methodName.equals("onCancelled")) {
            return;
        }

        // reset the event id
        while (iter.hasNext()) {
            iter.next().apply(new AbstractStmtSwitch() {
            public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
                units.insertBefore(SootUtils.staticInvocation(logObjectEventsMethod), stmt);
                units.insertBefore(SootUtils.staticInvocation(logThreadEventsMethod), stmt);

                units.insertBefore(SootUtils.staticInvocation(resetCurrentThreadFromObjectMethod, eventIdBeforeObject, objectHavingEventId, methodNameStr, classNameStr), stmt);
                System.out.println("===========Event handler reset stmt added..");
            }

            public void caseReturnStmt(ReturnStmt stmt) {
                units.insertBefore(SootUtils.staticInvocation(resetCurrentThreadFromObjectMethod, eventIdBeforeObject, objectHavingEventId, methodNameStr, classNameStr), stmt);
                System.out.println("===========Event handler reset stmt added..");
            }

            public void caseRetStmt(RetStmt stmt) {
                units.insertBefore(SootUtils.staticInvocation(resetCurrentThreadFromObjectMethod, eventIdBeforeObject, objectHavingEventId, methodNameStr, classNameStr), stmt);
                System.out.println("===========Event handler reset stmt added..");
                }
            });
        }
    }


    /**
     * In the beginning of an event handler: assign a new event id
     * In the end of an event handler: reset back its event id
     * @param b Body of the event handler method
     * @param methodName Name of the event handler method
     * @param className Class name that declares the event handler method
     */
    public void instrumentEventHandlerMethod(final Body b, final String methodName, final String className) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        if(SootUtils.hasReturningTrap(b)) {
            System.out.println("=================Not instrumented method: " + methodName + " " + className);
            System.out.println("=================It ends with a try/catch block");
            return;
        }

        // NOTE: Insert before first nonidentity statement - inserts inside the label
        // (e.g. if the first stmt is in try/catch block)
        // Insert after last identity statement - inserts outside the label
        //Stmt stmt = ((JimpleBody) b).getFirstNonIdentityStmt();
        Stmt lastIdentityStmt = SootUtils.getLastIdentityStmt(b);

        // save the current event id before executing that event handler
        // i.e. 0 if not in event handler, >0 if already in event handler
        final Local eventIdBeforeNestedEvent = SootUtils.addTmpPrimitiveInt(b, "rob_eventIdBeforeNestedEvent");
        InvokeExpr expr = Jimple.v().newStaticInvokeExpr(getEventOfCurrentThreadMethod.makeRef());
        // check and set new event id to the main thread
        final Local methodNameLocalInEH = SootUtils.addTmpString(b, "rob_methNameStrEH");
        final Local classNameLocalInEH = SootUtils.addTmpString(b, "rob_classNameStrEH");

        units.insertAfter(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(preProcessEHForEventIdMethod.makeRef(), methodNameLocalInEH, classNameLocalInEH)), lastIdentityStmt);
        units.insertAfter(Jimple.v().newAssignStmt(classNameLocalInEH, StringConstant.v(className)), lastIdentityStmt);
        units.insertAfter(Jimple.v().newAssignStmt(methodNameLocalInEH, StringConstant.v(methodName)), lastIdentityStmt);
        units.insertAfter(Jimple.v().newAssignStmt(eventIdBeforeNestedEvent, expr), lastIdentityStmt);

        System.out.println("===========Event handler stmt to assign event id added for: " + methodName);

        // restore the old event id before returning
        while (iter.hasNext()) {
            iter.next().apply(new AbstractStmtSwitch() {
                public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
                    units.insertBefore(SootUtils.staticInvocation(logObjectEventsMethod), stmt);
                    units.insertBefore(SootUtils.staticInvocation(logThreadEventsMethod), stmt);

                    units.insertBefore(SootUtils.staticInvocation(postProcessEHForEventIdMethod, eventIdBeforeNestedEvent, methodNameLocalInEH, classNameLocalInEH), stmt);
                    System.out.println("===========Event handler reset stmt added..");
                }

                public void caseReturnStmt(ReturnStmt stmt) {
                    units.insertBefore(SootUtils.staticInvocation(postProcessEHForEventIdMethod, eventIdBeforeNestedEvent, methodNameLocalInEH, classNameLocalInEH), stmt);
                    System.out.println("===========Event handler reset stmt added..");
                }

                public void caseRetStmt(RetStmt stmt) {
                    units.insertBefore(SootUtils.staticInvocation(postProcessEHForEventIdMethod, eventIdBeforeNestedEvent, methodNameLocalInEH, classNameLocalInEH), stmt);
                    System.out.println("===========Event handler reset stmt added..");
                }
            });
        }
    }

    public void instrumentToSetObjectToThread(final Body b, final String methodName, final String className) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator<Unit> iter = units.snapshotIterator();

        while (iter.hasNext()) {

                    iter.next().apply(new AbstractStmtSwitch() {

                        public void caseInvokeStmt(InvokeStmt stmt) {
                            String invokedMethodName = stmt.getInvokeExpr().getMethod().getName();
                            String invokedClassName = stmt.getInvokeExpr().getMethod().getDeclaringClass().getName();
                            boolean instrument = false;
                            Value objValue = null;

                            // if a Runnable is posted via a View or a Handler
                            // if a Message is posted via a Handler
                            // if a Runnable is posted via runOnUiThread
                            // if a Runnable is executed on a ThreadPool
                            // if an Intent is sent via startService
                            // .post(Object)
                            if ((invokedMethodName.equals("post") || (invokedMethodName.equals("postDelayed")) && (invokedClassName.equals("android.view.View") || invokedClassName.equals("android.os.Handler")))
                                    || (invokedMethodName.equals("sendMessage") && (invokedClassName.equals("android.os.Handler")))
                                    || (invokedMethodName.equals("runOnUiThread") && (invokedClassName.equals("android.app.Activity")))
                                    || (invokedMethodName.equals("execute") && (invokedClassName.equals("java.util.concurrent.ThreadPoolExecutor")))
                                    || (invokedMethodName.equals("startService") && (invokedClassName.equals("android.app.Service")))){
                                objValue = stmt.getInvokeExpr().getArg(0);
                                instrument = true;

                            // if an AsyncTask is executed
                            // if a Thread is started
                            // if a Message is sent to its target
                            // Object.start
                            } else if ((invokedMethodName.equals("start") && (invokedClassName.equals("android.app.Thread")))
                                    // NOTE: execute AsyncTask is handled inside framework code -- if AsyncTask is already running, do not set its ID
                                    // This provides the eventId of the AsyncTask to remain in the last event and associate the AsyncTask accesses accordingly
                                    // Otherwise, the eventId of the AsyncTask is modified in an already-executing AsyncTask, preventing serializability checker cycles
                                    //|| (((invokedMethodName.equals("execute") || invokedMethodName.equals("executeOnExecutor"))) && (invokedClassName.equals("android.os.AsyncTask")))
                                    || ((invokedMethodName.equals("sendToTarget")) && (invokedClassName.equals("android.os.Message")))) {
                                InvokeExpr e = stmt.getInvokeExpr();
                                objValue = ((VirtualInvokeExpr) e).getBase();
                                instrument = true;
                            }

                            if (instrument) {

                                if (!isEventLocalsDeclared) {
                                    // check and set new event id to the main thread
                                    methodNameLocal = SootUtils.addTmpString(b, "rob_methNameStr");
                                    classNameLocal = SootUtils.addTmpString(b, "rob_classNameStr");
                                    isEventLocalsDeclared = true;
                                }

                                units.insertAfter(SootUtils.staticInvocation(setObjectToCurrentThreadMethod, objValue, methodNameLocal, classNameLocal), stmt);
                                System.out.println("In: " + methodName + " of " + className + "\n===========Instrumented invocation: " + invokedMethodName + " class: " + invokedClassName);

                                units.insertAfter(Jimple.v().newAssignStmt(methodNameLocal, StringConstant.v(methodName)), stmt);
                                units.insertAfter(Jimple.v().newAssignStmt(classNameLocal, StringConstant.v(className)), stmt);
                            }
                        }
            });
        }

    }

    /**
     * Logs R/W field accesses in the method
     * @param b Body of the instrumented method
     */
    public void instrumentForRW(final Body b) {
        final PatchingChain<Unit> units = b.getUnits();
        Iterator stmtIt = units.snapshotIterator();

        String methodName = b.getMethod().getName();

        while (stmtIt.hasNext()) {
            Stmt s = (Stmt) stmtIt.next();
            Iterator boxDef, boxUse;
            boxDef = s.getDefBoxes().iterator();
            boxUse = s.getUseBoxes().iterator();

            while (boxDef.hasNext()) {
                ValueBox vBox = (ValueBox) boxDef.next();
                Value v = vBox.getValue();

                if (v instanceof InstanceFieldRef) {
                    logFieldAccess(v, b, units, s, "PUT", methodName);
                    break;
                }
            }

            while (boxUse.hasNext()) {
                ValueBox vBox = (ValueBox) boxUse.next();
                Value v = vBox.getValue();

                if (v instanceof InstanceFieldRef) {
                    logFieldAccess(v, b, units, s, "GET", methodName);
                    break;
                }
            }
        }
    }

    private void logFieldAccess(Value v, Body b, PatchingChain<Unit> units, Stmt s, String accType, String methodName) {
        Value obj = null;
        SootField field = null;

        obj = ((InstanceFieldRef) v).getBase();
        field = ((InstanceFieldRef) v).getField();

        if(field.isFinal()) return;

        if(!isRWLocalsDeclared) {
            hashInt = SootUtils.addTmpPrimitiveInt(b, "rob_hashInt");
            hashStr = SootUtils.addTmpString(b, "rob_hashStr");
            accessTypeStr = SootUtils.addTmpString(b, "rob_accessTypeStr");
            objNameStr = SootUtils.addTmpString(b, "rob_objNameStr");
            fieldNameStr = SootUtils.addTmpString(b, "rob_fieldNameStr");
            methodNameStr = SootUtils.addTmpString(b, "rob_methodNameStr");
            isRWLocalsDeclared = true;
        }

        // hashInt = obj.identityHashCode()
        InvokeExpr expr = Jimple.v().newStaticInvokeExpr(identityHashCodeMethod.makeRef(), ((InstanceFieldRef) v).getBase());
        units.insertBefore(Jimple.v().newAssignStmt(hashInt, expr), s);
        // hashStr = Integer.toHexString(hasInt)
        InvokeExpr expr2 = Jimple.v().newStaticInvokeExpr(toHexMethod.makeRef(), hashInt);
        units.insertBefore(Jimple.v().newAssignStmt(hashStr, expr2), s);
        units.insertBefore(Jimple.v().newAssignStmt(accessTypeStr, StringConstant.v(accType)), s);
        units.insertBefore(Jimple.v().newAssignStmt(objNameStr, StringConstant.v(obj.getType().toString())), s);
        units.insertBefore(Jimple.v().newAssignStmt(fieldNameStr, StringConstant.v(field.getName())), s);
        units.insertBefore(Jimple.v().newAssignStmt(methodNameStr, StringConstant.v(methodName)), s);

        units.insertBefore(Jimple.v().newInvokeStmt(
                Jimple.v().newStaticInvokeExpr(recordMethod.makeRef(), accessTypeStr, hashStr, objNameStr, fieldNameStr, methodNameStr)), s);
    }

    private void printStats() {
        System.out.println("\t----------Number of instrumented methods:----------");
        System.out.println("\tEvent handlers: \t" + numEventHandlers);
        System.out.println("\tLifecycle handlers: \t" + numLifeCycleHandlers);
        System.out.println("\tAsyncTasks: \t\t" + numAsyncTasks/2);
        System.out.println("\tRunnables: \t\t" + numRunnables);
        System.out.println("\tMessage handlers: \t" + numMessageHandlers);
        System.out.println("\tIntent handlers: \t" + numIntentHandlers);
    }
}
