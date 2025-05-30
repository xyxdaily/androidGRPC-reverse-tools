package com.hepta.androidgrpc;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;

public class dump {


    public static void Entry(Context ctx,String source, String argument) {
        PreLoadNativeSO(ctx,source);
    }

    public static void PreLoadNativeSO(Context context, String source) {
        try {
            String abi= "arm64-v8a";
            if(!android.os.Process.is64Bit()){
                abi = "armeabi-v7a";
            }
            String libdump = source+"!/lib/"+abi+"/libDavilkRuntime.so";
            System.load(libdump);
        }catch (Exception e){
            Log.e("LoadEntry","LoadSo error");
        }
    }

    public static void dumpClass(String className){
        ClassLoader[] classLoaders =  getClassLoaderList();
        for (ClassLoader classLoader:classLoaders) {
            try {
                Class LoadEntry_cls =  classLoader.loadClass(className);
                Method[] Declaredmethods =  LoadEntry_cls.getDeclaredMethods();
                for (Method method :Declaredmethods ) {
                    dumpMethodByMember(method);
                }
                Constructor[] DeclaredConstructors =  LoadEntry_cls.getDeclaredConstructors();
                for (Constructor method :DeclaredConstructors ) {
                    dumpMethodByMember(method);
                }
                Log.e("LoadEntry",className);
            } catch (ClassNotFoundException e) {
            }
        }
    }
    public static AndroidClassLoaderInfo getClassLoaderCookie(Context context,Class cls) {

        BaseDexClassLoader[] classLoaders = (BaseDexClassLoader[]) getBaseDexClassLoaderList();
        try {
            //private final DexPathList pathList;
            Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");

            //private Element[] dexElements;
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Class<?> Element = Class.forName("dalvik.system.DexPathList$Element");

            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            Field DexFile_mCookie = DexFile.class.getDeclaredField("mCookie");
            Field DexFile_mFileName = DexFile.class.getDeclaredField("mFileName");
            Field path_filed = Element.getDeclaredField("path");
            Field dexFile_filed = Element.getDeclaredField("dexFile");
            pathListField.setAccessible(true);
            DexFile_mCookie.setAccessible(true);
            DexFile_mFileName.setAccessible(true);
            dexElementsField.setAccessible(true);
            dexFile_filed.setAccessible(true);
            for (ClassLoader classLoader:classLoaders) {
                AndroidClassLoaderInfo loaderInfo = new AndroidClassLoaderInfo();
                loaderInfo.setClassType(classLoader.getClass().getName());
                if (classLoader instanceof BaseDexClassLoader) {
                    Object BaseDexClassLoad_PathList = pathListField.get(classLoader);
                    Object[] DexPathList_dexElements = (Object[]) dexElementsField.get(BaseDexClassLoad_PathList);

                    if (DexPathList_dexElements != null) {
                        for (Object dexElement : DexPathList_dexElements) {

                            DexFile dexFile = (DexFile) dexFile_filed.get(dexElement);
                            if (dexFile != null) {
                                //这个cookie 在android 13是一个智能指针，保存的是一个native 的 DexFile 指针
                                Class<?> find_cla = dexFile.loadClass(cls.getName(), cls.getClassLoader());
                                if (cls.equals(find_cla)){
                                    long[] cookie = (long[]) DexFile_mCookie.get(dexFile);
                                    String fileName = (String) DexFile_mFileName.get(dexFile);
                                    if (fileName == null){
                                        loaderInfo.setFilePatch("null");
                                    }
                                    loaderInfo.setCookie(cookie);
                                    return loaderInfo;
                                }
                            }
                        }
                    }
                } else {
                    Log.e("dump", "class instanceof is not BaseDexClassLoader type :"+classLoader.getClass().getName());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<AndroidClassLoaderInfo> getDexClassLoaderCookieList(Context context) {
        List<AndroidClassLoaderInfo> DexClassLoaderCookieList =new ArrayList<>();

        BaseDexClassLoader[] classLoaders = (BaseDexClassLoader[]) getBaseDexClassLoaderList();
        try {
            //private final DexPathList pathList;
            Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");

            //private Element[] dexElements;
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Class<?> Element = Class.forName("dalvik.system.DexPathList$Element");

            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            Field DexFile_mCookie = DexFile.class.getDeclaredField("mCookie");
            Field DexFile_mFileName = DexFile.class.getDeclaredField("mFileName");
            Field path_filed = Element.getDeclaredField("path");
            Field dexFile_filed = Element.getDeclaredField("dexFile");
            pathListField.setAccessible(true);
            DexFile_mCookie.setAccessible(true);
            DexFile_mFileName.setAccessible(true);
            dexElementsField.setAccessible(true);
            dexFile_filed.setAccessible(true);
            for (ClassLoader classLoader:classLoaders) {
                AndroidClassLoaderInfo loaderInfo = new AndroidClassLoaderInfo();
                loaderInfo.setClassType(classLoader.getClass().getName());
                if (classLoader instanceof BaseDexClassLoader) {
                    Object BaseDexClassLoad_PathList = pathListField.get(classLoader);
                    Object[] DexPathList_dexElements = (Object[]) dexElementsField.get(BaseDexClassLoad_PathList);

                    if (DexPathList_dexElements != null) {
                        for (Object dexElement : DexPathList_dexElements) {

                            DexFile dexFile = (DexFile) dexFile_filed.get(dexElement);
                            if (dexFile != null) {
                                //这个cookie 在android 13是一个智能指针，保存的是一个native 的 DexFile 指针
                                long[] cookie = (long[]) DexFile_mCookie.get(dexFile);
                                String fileName = (String) DexFile_mFileName.get(dexFile);
                                if (fileName == null){
                                    loaderInfo.setFilePatch("null");
                                }

                                loaderInfo.setCookie(cookie);
                                DexClassLoaderCookieList.add(loaderInfo);
                            }
                        }
                    }
                } else {
                    Log.e("dump", "class not instanceof BaseDexClassLoader");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return DexClassLoaderCookieList;
    }
    public static void dumpdexToLocal(Context context) {

        File pathFile=new File(context.getFilesDir().getAbsolutePath()+"/dump");
        if(!pathFile.exists()){
            pathFile.mkdirs();
        }
        BaseDexClassLoader[] classLoaders = (BaseDexClassLoader[]) getBaseDexClassLoaderList();
        try {
            //TODO:to get 'pathList' field and 'dexElements' field by reflection.
            //private final DexPathList pathList;
            Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListField = baseDexClassLoaderClass.getDeclaredField("pathList");

            //private Element[] dexElements;
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Class<?> Element = Class.forName("dalvik.system.DexPathList$Element");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            Field DexFile_mCookie = DexFile.class.getDeclaredField("mCookie");
            Field DexFile_mFileName = DexFile.class.getDeclaredField("mFileName");
            Field path_filed = Element.getDeclaredField("path");
            Field dexFile_filed = Element.getDeclaredField("dexFile");
            pathListField.setAccessible(true);
            DexFile_mCookie.setAccessible(true);
            DexFile_mFileName.setAccessible(true);
            dexElementsField.setAccessible(true);
            dexFile_filed.setAccessible(true);
            for (ClassLoader classLoader:classLoaders) {
                if (classLoader instanceof BaseDexClassLoader) {
                    Object BaseDexClassLoad_PathList = pathListField.get(classLoader);
                    Object[] DexPathList_dexElements = (Object[]) dexElementsField.get(BaseDexClassLoad_PathList);
                    int i = 0;
                    if (DexPathList_dexElements != null) {
                        for (Object dexElement : DexPathList_dexElements) {
                            DexFile dexFile = (DexFile) dexFile_filed.get(dexElement);
                            if (dexFile != null) {
                                //这个cookie 在android 13是一个智能指针，保存的是一个native 的 DexFile 指针
                                long[] cookie = (long[]) DexFile_mCookie.get(dexFile);
                                dumpDexToLocalByCookie(cookie,pathFile.getAbsolutePath());
                            }
                        }
                    }
                } else {
                    Log.e("dump", "class not instanceof BaseDexClassLoader");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static native List<byte[]> dumpDexBuffListByCookie(long[] cookie);
    public static native void dumpDexToLocalByCookie(long[] cookie,String dumpDir);
    public static native byte[] getDexBuffbyCookieLong(long cookie);
    public static native ClassLoader[] getBaseDexClassLoaderList();
    public static native byte[] dumpMethodByMember(Member method);
    public static native byte[] dumpMethodByString(Class<?> cls, String methodName, String methodSign);
    public static native ClassLoader[] getClassLoaderList();
    public static native String[] getSoNameList();
    public static native byte[] dumpSoMemByName(String soName);
    public static native byte[] dumpMemByaddr(long addr ,long size);


}
