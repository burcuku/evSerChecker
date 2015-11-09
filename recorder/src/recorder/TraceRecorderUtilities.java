package recorder;

import java.lang.reflect.Method;
import android.content.Context;

/**
 * Created by burcuozkan
 */
public class TraceRecorderUtilities {

    private static Class recorderClass = null;
    private static boolean isInitiated = false;

    public static void initRecorderUtilities(Context context) {
       if(!isInitiated) {
           try {
               recorderClass = Class.forName("android.eventMap.TraceRecorder");
               invokeInitRecorder(context);
               isInitiated = true;
           } catch (ClassNotFoundException e) {
               e.printStackTrace();
           }
       }
    }

    private static void invokeInitRecorder(Context context) {
        Method initRecorderMethod = null;
        try {
            initRecorderMethod = recorderClass.getDeclaredMethod("initRecorder", Context.class);
            initRecorderMethod.setAccessible(true);
            initRecorderMethod.invoke(recorderClass, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void invokeRecord(String accessType, String objId, String className, String fieldName, String methodName) {
        Method recordMethod = null;
        try {
            recordMethod = recorderClass.getDeclaredMethod("record", String.class, String.class, String.class, String.class, String.class);
            recordMethod.setAccessible(true);
            // null check items
            recordMethod.invoke(recorderClass, accessType, objId, className, fieldName, methodName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void invokeClear() {
        Method clearMethod = null;
        try {
            clearMethod = recorderClass.getDeclaredMethod("clear");
            clearMethod.setAccessible(true);
            clearMethod.invoke(recorderClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
