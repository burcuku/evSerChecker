package recorder;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by burcuozkan on 14/10/15.
 */
public class EventMapUtilities {

    //private static Application app = null;

    private static Class eventMapClass = null;

    private static Method logObjectEventsMethod = null;
    private static Method logThreadEventsMethod = null;

    private static Method getEventOfThreadMethod = null;
    private static Method getEventOfObjectMethod = null;
    private static Method setThreadToEventMethod = null;
    private static Method setObjectToEventMethod = null;

    private static Method initializeEventMapMethod = null;
    private static Method setCurrentThreadToObjectMethod = null;
    private static Method setObjectToCurrentThreadMethod = null;
    private static Method setDefaultExceptionHandlerMethod = null;

    private static Method preProcessEHForEventIdMethod = null;
    private static Method postProcessEHForEventIdMethod = null;

    private static boolean isInitiated = false;

    public static void initEventUtilities() {
        if(!isInitiated) {
            initClassMethods();
            invokeInitializeEventMap();
            isInitiated = true;
        }
    }

    private static void initClassMethods() {
        try {
            Class eventMapClass = Class.forName("android.eventMap.EventMap");

            getEventOfThreadMethod = eventMapClass.getDeclaredMethod("getEventOfThread", long.class);
            getEventOfThreadMethod.setAccessible(true);

            getEventOfObjectMethod = eventMapClass.getDeclaredMethod("getEventOfObject", int.class);
            getEventOfObjectMethod.setAccessible(true);

            setThreadToEventMethod = eventMapClass.getDeclaredMethod("setThreadToEvent", long.class, int.class);
            setThreadToEventMethod.setAccessible(true);

            logObjectEventsMethod = eventMapClass.getDeclaredMethod("logObjectEvents");
            logThreadEventsMethod = eventMapClass.getDeclaredMethod("logThreadEvents");

            initializeEventMapMethod = eventMapClass.getDeclaredMethod("initialize");
            initializeEventMapMethod.setAccessible(true);

            setCurrentThreadToObjectMethod = eventMapClass.getDeclaredMethod("setCurrentThreadToObject", Object.class);
            setCurrentThreadToObjectMethod.setAccessible(true);

            setObjectToCurrentThreadMethod = eventMapClass.getDeclaredMethod("setObjectToCurrentThread", Object.class);
            setObjectToCurrentThreadMethod.setAccessible(true);

            setObjectToEventMethod = eventMapClass.getDeclaredMethod("setObjectToEvent", int.class, int.class);
            setObjectToEventMethod.setAccessible(true);

            setDefaultExceptionHandlerMethod = eventMapClass.getDeclaredMethod("setDefaultExceptionHandler");
            setDefaultExceptionHandlerMethod.setAccessible(true);

            preProcessEHForEventIdMethod = eventMapClass.getDeclaredMethod("preProcessEHForEventId", String.class, String.class);
            preProcessEHForEventIdMethod.setAccessible(true);

            postProcessEHForEventIdMethod = eventMapClass.getDeclaredMethod("postProcessEHForEventId", int.class, String.class, String.class);
            postProcessEHForEventIdMethod.setAccessible(true);


        } catch (ClassNotFoundException ex) {
            Log.e("Reflection", ex.getMessage());
            Log.e("Reflection", "Class not found: android.eventMap.EventMap");
        } catch (NoSuchMethodException ex) {
            Log.e("Reflection", ex.getMessage());
            Log.e("Reflection", "Method not found in android.eventMap.EventMap");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void invokeSetDefaultExceptionHandler() {
        try {
            setDefaultExceptionHandlerMethod.invoke(eventMapClass);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke setDefaultExceptionHandler method in android.eventMap.EventMap");
        }
    }

    public static void invokePreProcessEHForEventId(String methodName, String className) {
        try {
            preProcessEHForEventIdMethod.invoke(eventMapClass, methodName, className);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke preProcessEHForEventId method in android.eventMap.EventMap");
        }
    }

    public static void invokePostProcessEHForEventId(int eventId, String methodName, String className) {
        try {
            postProcessEHForEventIdMethod.invoke(eventMapClass, eventId, methodName, className);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke postProcessEHForEventId method in android.eventMap.EventMap");
            Log.e("ROB", e.toString());

        }
    }

    public static void invokeSetCurrentThreadToObject(Object o, String methodName, String className) {
        String oid = Integer.toHexString(System.identityHashCode(o));
        long tid = Thread.currentThread().getId();

        try {
            Integer teid = (Integer) getEventOfThreadMethod.invoke(eventMapClass, tid);

            Log.i("ROB", "Currently it is set to: " + teid + " " + methodName + " " + className);

            if(teid > 0) { // already in event, probably called sync - in execute actions in Fragment, etc
                Log.v("ROB", "Thread " + Thread.currentThread().getId() + " with id: " + teid + " is NOT set to the object " + oid + " + in method " + methodName + " in " + className);
            } else {
                Log.v("ROB", "Thread " + Thread.currentThread().getId() + " is set to the object " + oid + " + in method " + methodName + " in " + className);
                setCurrentThreadToObjectMethod.invoke(eventMapClass, o);
            }
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke setCurrentThreadToObject method in android.eventMap.EventMap");
        }
    }

    /**
     * The end of doInBackground, run, handleMessage, onProgressUpdate, onPostExecute and onCancelled
     * @param o
     */
    public static void resetCurrentThreadFromObject(int oldEventId, Object o, String methodName, String className) {
        String oid = Integer.toHexString(System.identityHashCode(o));
        long tid = Thread.currentThread().getId();
        Log.i("ROB", "Thread " + tid + " is reset back from the object " + oid + " in method: " + methodName + " in class: " + className + " to eid: " + oldEventId);
        try {
            setThreadToEventMethod.invoke(eventMapClass, tid , oldEventId); // was 0 before, wrong reset in case of synchronously called run methods
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke setThreadToEvent method in android.eventMap.EventMap");
        }
    }

    public static void invokeSetObjectToCurrentThread(Object o, String methodName, String className) {
        String oid = Integer.toHexString(System.identityHashCode(o));

        Log.i("ROB", "Object " + oid + " is set to the thread " + Thread.currentThread().getId() + " + in method " + methodName + " in " + className);
        try {
            setObjectToCurrentThreadMethod.invoke(eventMapClass, o);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke setObjectToCurrentThread method in android.eventMap.EventMap");
        }
    }


    public static void resetEventOfObject(Object o) {
        int hashInt = System.identityHashCode(o);
        try {
            setObjectToEventMethod.invoke(eventMapClass, hashInt, 0);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke setObjectToEvent method in android.eventMap.EventMap");
        }
    }

    public static void setThreadToCurrentThread(Object o, String methodName, String className) {
        String oid = Integer.toHexString(System.identityHashCode(o));
        long tid = Thread.currentThread().getId();

        try {
            Integer teid = (Integer) getEventOfThreadMethod.invoke(eventMapClass, tid);

            Log.i("ROB", "Currently it is set to: " + teid + " " + methodName + " " + className);

            if(teid > 0) { // already in event, probably called sync - in execute actions in Fragment, etc
                Log.v("ROB", "Thread " + Thread.currentThread().getId() + " with id: " + teid + " is NOT set to the object " + oid + " + in method " + methodName + " in " + className);
            } else {
                Log.v("ROB", "Thread " + Thread.currentThread().getId() + " is set to the object " + oid + " + in method " + methodName + " in " + className);
                setCurrentThreadToObjectMethod.invoke(eventMapClass, o);
            }
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke setCurrentThreadToObject method in android.eventMap.EventMap");
        }
    }

    public static void setRandomDelay(Object o, String methodName, String className) {
        Thread currentThread = Thread.currentThread();
        // do not delay the main thread
        if(currentThread.getId() == 1) return;

        double toDelay = Math.random();
        if(toDelay < 0.50) {
            try {
                currentThread.sleep(1000);
                String oid = Integer.toHexString(System.identityHashCode(o));
                Log.i("ROB", "Thread " + currentThread.getId() + " was delayed in " + oid + " + in method " + methodName + " in " + className);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static int getEventOfCurrentThread() {
        return invokeGetEventOfThread(Thread.currentThread().getId());
    }

    public static void invokeLogObjectEvents() {
        try {
            logObjectEventsMethod.invoke(eventMapClass);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke logObjectEvents method in android.eventMap.EventMap");
        }
    }

    public static void invokeLogThreadEvents() {
        try {
            logThreadEventsMethod.invoke(eventMapClass);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke logThreadEvents method in android.eventMap.EventMap");
        }
    }

    /** privates **/

    private static void invokeInitializeEventMap() {
        try {
            initializeEventMapMethod.invoke(eventMapClass);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke initializeEventMap method in android.eventMap.EventMap");
        }
    }

    private static int invokeGetEventOfThread(long threadId) {
        Integer eventId = 0;

        try {
            eventId = (Integer) getEventOfThreadMethod.invoke(eventMapClass, (Long)threadId);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke getEventOfThread method in android.eventMap.EventMap");
        }

        return eventId.intValue();
    }

    private static void invokeSetThreadToEvent(long threadId, int eventId) {
        try {
            setThreadToEventMethod.invoke(eventMapClass, (long) threadId, eventId);
        } catch (Exception e) {
            Log.e("Reflection", "Cannot invoke setThreadToEvent method in android.eventMap.EventMap");
        }
    }


}
