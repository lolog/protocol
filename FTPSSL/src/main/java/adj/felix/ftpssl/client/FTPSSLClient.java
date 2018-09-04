package adj.felix.ftpssl.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;

public class FTPSSLClient {
	private FTPSClient ftpsClient;
	private Properties properties = new Properties();
	
	private String users_prop_path = "ssl/users.properties";
	private String keystore_path = "ssl/domain.keystore";
	
	private String serverIP = "127.0.0.1";
	private int serverPort = 211;
	private int defaultTimeout = 10000;
	private int soTimeout = 60000;
	private int dataTimeout = 5000;
	
	public void init () throws FileNotFoundException, IOException {
		properties.load(new FileInputStream(users_prop_path));
	}
	
	public boolean connect(String active) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		// 创建FTPSClient
		ftpsClient = new FTPSClient(true);
		// 协议命令监听, 并且输出到命令行
		ftpsClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
		// key验证
		ftpsClient.setKeyManager(getKeyManager());
		ftpsClient.setTrustManager(getTrustManager());
		
		ftpsClient.setDefaultTimeout(defaultTimeout);
		ftpsClient.connect(serverIP, serverPort);
		System.out.println("已链接FTP");
		
		ftpsClient.setSoTimeout(soTimeout);
		ftpsClient.getReplyCode();
		ftpsClient.execPBSZ(0);
		ftpsClient.execPROT("P");
		// FTP登录
		ftpsClient.login("admin", "2011180062");
		ftpsClient.changeWorkingDirectory("/");
		ftpsClient.setDataTimeout(dataTimeout);
		if (active.equalsIgnoreCase("active")) {
			ftpsClient.enterLocalActiveMode();
		} else {
			ftpsClient.enterLocalPassiveMode();
		}
		
		System.out.println("已登陆FTP");
		return testLink();
	}
	/** 验证FTP链接是否成功 */
	private boolean testLink () throws IOException {
		ftpsClient.listFiles();
		return true;
	}
	
	/**遍历FTP文件  */
	public boolean scanDirectory() {
		long t1 = System.currentTimeMillis();
		try {
			System.out.println("遍历FTP的文件");
			FTPFile[] listFiles = ftpsClient.listFiles();
			for (FTPFile ftpFile: listFiles) {
				String file = ftpFile.getRawListing();
				System.out.println(file);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			long t2 = System.currentTimeMillis();
			long t = (t2 - t1) / 1000;
			System.out.println("t: " + t);
			try {
				ftpsClient.disconnect();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}
	
	/** SSL证书认证  */
	private KeyManager getKeyManager () throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException {
		String keyPassword = properties.get("keyPassword").toString();
		// 认证证书
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keystore_path), keyPassword.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, keyPassword.toCharArray());
		KeyManager[] km = kmf.getKeyManagers();
		System.out.println("km len: " + km.length);
		
		return km[0];
	}
	
	private TrustManager getTrustManager() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		String storePassword = properties.get("storePassword").toString();
		
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keystore_path), storePassword.toCharArray());
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);
		TrustManager[] tm = trustManagerFactory.getTrustManagers();
		System.out.println("tm len: " + tm.length);
		
		return tm[0];
	}
	
	public void close() {
		try {
			ftpsClient.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FTPSSLClient ftpsslClient = new FTPSSLClient();
		try {
			ftpsslClient.init();
			ftpsslClient.connect("active");
			ftpsslClient.scanDirectory();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			ftpsslClient.close();
		}
	}
}