package com.my.xposedbasedemo;

import java.net.InetSocketAddress;
import java.net.Proxy;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class Arrow implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        // 不是需要 Hook 的包直接返回
        if (!loadPackageParam.packageName.equals("com.my.judge_in_proxy"))
            return;

        XposedBridge.log("app包名：" + loadPackageParam.packageName);

        //自己设置的代理地址
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.4", 8888));

        //OkHttp 强制走代理
        XposedHelpers.findAndHookMethod("okhttp3.OkHttpClient$Builder", // 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "proxy", // 被Hook函数的名称proxy
                java.net.Proxy.class, // 被Hook函数的第一个参数Proxy
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start ================== okhttp3.OkHttpClient ===============");
                        XposedBridge.log("OkHttpClient$Builder  proxy : " + param.args[0]);
                        //将传入的参数  改成自己的代理
                        param.args[0] = proxy;
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.afterHookedMethod(param);
                        XposedBridge.log("end ================== okhttp3.OkHttpClient ===============");
                    }
                });

        /*URL url = new URL(urlPath);
        connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);//发起网络请求,并且不走代理的写法*/
        //URL 强制走代理
        XposedHelpers.findAndHookMethod("java.net.URL",// 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "openConnection",// 被Hook函数的名称 openConnection
                java.net.Proxy.class, // 被Hook函数的第一个参数Proxy
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start ============= java.net.URL ============");
                        XposedBridge.log("java.net.URL proxy : " + param.args[0]);
                        param.args[0] = proxy;
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        // TODO Auto-generated method stub
                        super.afterHookedMethod(param);
                        XposedBridge.log("end ============= java.net.URL ============");
                    }
                });

        //绕过代理检测 1
        XposedHelpers.findAndHookMethod("java.util.Properties",// 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "getProperty",// 被Hook函数的名称
                String.class, // 被Hook函数的第一个参数
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start ============= java.util.Properties  getProperty ============");
                        XposedBridge.log("java.util.Properties  proxy : " + param.args[0]);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        // TODO Auto-generated method stub
                        super.afterHookedMethod(param);
                        // 修改方法的返回值
                        if (param.args[0].equals("http.proxyHost") || param.args[0].equals("https.proxyHost")
                                || param.args[0].equals("http.proxyPort") || param.args[0].equals("https.proxyPort")) {
                            param.setResult(null);
                        }
                        XposedBridge.log("end ============= java.util.Properties  getProperty ============");
                    }
                });
        //绕过代理检测 2 (检测代理两种方法都可能用,绕过代理最好 1 2两种方法都用上)
        XposedHelpers.findAndHookMethod("java.lang.System",// 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "getProperty",// 被Hook函数的名称
                String.class, // 被Hook函数的第一个参数
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start ============= java.lang.System  getProperty ============");
                        XposedBridge.log("java.lang.System  proxy  :  " + param.args[0]);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        // TODO Auto-generated method stub
                        super.afterHookedMethod(param);
                        // 修改方法的返回值
                        if (param.args[0].equals("http.proxyHost") || param.args[0].equals("https.proxyHost")
                                || param.args[0].equals("http.proxyPort") || param.args[0].equals("https.proxyPort")) {
                            param.setResult(null);
                        }
                        XposedBridge.log("end ============= java.lang.System  getProperty ============");
                    }
                });

        //Base64 入参
        XposedHelpers.findAndHookMethod("android.util.Base64", // 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "encodeToString", // 被Hook函数的名称
                byte[].class,//参数 1
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start =========== Base64 encodeToString   =========");
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.afterHookedMethod(param);
                        byte[] param1 = (byte[]) param.args[0];
                        String param1_str = new String(param1);
                        XposedBridge.log("传入参数 : \n" + param1_str);

                        Object resultString = param.getResult();
                        XposedBridge.log("返回值：" + resultString.toString());
                        XposedBridge.log("end =========== Base64 encodeToString   =========");
                    }
                });

        //打印newCall
        XposedHelpers.findAndHookMethod("okhttp3.OkHttpClient", // 被Hook函数所在的类(包名+类名)
                loadPackageParam.classLoader,
                "newCall", // 被Hook函数的名称proxy
                okhttp3.Request.class, // 被Hook函数的第一个参数
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.beforeHookedMethod(param);
                        XposedBridge.log("start ================== newCall ===============");
                        XposedBridge.log("request : " + param.args[0]);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        super.afterHookedMethod(param);
                        XposedBridge.log("end ================== newCall ===============");
                    }
                });
    }

}
