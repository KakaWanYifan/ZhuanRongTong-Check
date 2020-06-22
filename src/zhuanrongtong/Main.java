package zhuanrongtong;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.sscc.fdep.*;
public class Main {
	
	public static String bytesToString(byte[] b)
	{
		StringBuffer result = new StringBuffer("");
		int length = b.length;
		for (int i = 0; i < length; i++)
		{
			result.append((char)(b[i] & 0xff));
		}
		return result.toString();
	}
	
	public static void main(String[] args) {
		
		/*
		 * 载入配置文件
		 */
		Properties properties = new Properties();
		try {
			String outpath = System.getProperty("user.dir")+File.separator;		
			InputStream inputStream = new FileInputStream(new File(outpath + "cfg.properties"));
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * 初始化代码
		 */
		String App = properties.getProperty("App");
		String AppPwd = properties.getProperty("AppPwd");
		String Ip = properties.getProperty("Ip");
		Short Port = Short.valueOf( properties.getProperty("Port"));
		String Ipbak = properties.getProperty("Ipbak");
		Short Portbak = Short.valueOf(properties.getProperty("Portbak"));
		
		System.out.println("App : " + App);
		System.out.println("AppPwd : " + AppPwd);
		System.out.println("Ip : " + Ip);
		System.out.println("Port : " + Port);
		System.out.println("Ipbak : " + Ipbak);
		System.out.println("Portbak : " + Portbak);
		
		System.out.println("MrInit");
		int initValue = mrapi.Mr2Init(App, AppPwd, Ip, Port, Ipbak, Portbak);
		System.out.println("initValue:" + initValue);
		
		/*
		 * 获取句柄
		 */
		System.out.println("PKG");
		byte[] szCreatePkgId = mrapi.Mr2CreatePkgID(App);
		String ssCreatePkgId = bytesToString(szCreatePkgId);
		System.out.println("CreatePkgId=" + ssCreatePkgId);
		System.out.println("END_PKG");
		
		/*
		 * 判断连接状态
		 */
		if (mrapi.Mr2IsLinkOK(App) != 0)
		{
			System.out.println("Link OK");
		}
		else
		{
			System.out.println("Link Failed");
		}
		
		/*
		 * 发送数据
		 */
		byte[] psPkg = properties.getProperty("psPkg").getBytes();
		String SourceUserID = properties.getProperty("SourceUserID");
		String SourceAppID = properties.getProperty("SourceAppID");
		String DestUserID = properties.getProperty("DestUserID");
		String DestAppID = properties.getProperty("DestAppID");
		String PkgID = ssCreatePkgId;
		String CorrPkgID = properties.getProperty("CorrPkgID");
		String UserData1 = properties.getProperty("UserData1");
		String UserData2 = properties.getProperty("UserData2");
		String MsgType = properties.getProperty("MsgType");
		byte flag = Byte.valueOf(properties.getProperty("Flag"));
		
		/*
		 * java发超过127的byte给c++接收
         * 原理：java中发送字节255到c++中的unsigned byte接收,255表示为1111 1111 ，在java中符号位1表示负数，负数存的是补码
         * 补码-1再全部取反为原码，所以~（1111 1111 - 1） = 0000 0001，所以java中把-1发给c++的unsigned byte接收就是255了
         * 总结：java发字节255，需要实际发-1（强转为byte）即可
		 */
		int bizTypeInteger = Integer.valueOf(properties.getProperty("BizType"));
		byte BizType = (byte)bizTypeInteger;
		
		byte Priority = Byte.valueOf(properties.getProperty("Priority"));
		byte SensitiveLevel = Byte.valueOf(properties.getProperty("SensitiveLevel"));
		Integer iMillSecTimeo = Integer.valueOf(properties.getProperty("iMillSecTimeo"));
		
		System.out.println("snedata : " + properties.getProperty("psPkg"));
		System.out.println("MrSend");
		
		String sendState = mrapi.Mr2Send(psPkg, SourceUserID, SourceAppID, DestUserID, DestAppID, PkgID, CorrPkgID, UserData1, UserData2, MsgType, flag, BizType, Priority, SensitiveLevel, iMillSecTimeo);
		if ("".equals(sendState)) {
			System.out.println("发送失败");
		}
		else {
			System.out.println("发送成功");
		}
		
		
		/*
		 * 接收数据
		 */
		String sAppID = App;
		System.out.println("MrReceiveready to receive");
		for (int i = 0; i < 10; i++) {
			byte[] respData = mrapi.Mr2Receive3(sAppID, SourceUserID, SourceAppID, DestUserID, DestAppID, PkgID, CorrPkgID, UserData1, UserData2, 2000);
			String result = bytesToString(respData);
			if(result.length()>9)
			{ 
				System.out.println("recv data:" + result+"^"+i);
				i++;
            }
		}
		
		/*
		 * 资源释放
		 */
		System.out.println("资源释放   Destroy");
		mrapi.Mr2Destroy(sAppID);
	}

}
