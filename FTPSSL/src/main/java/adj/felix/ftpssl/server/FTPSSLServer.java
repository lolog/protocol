package adj.felix.ftpssl.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

public class FTPSSLServer {
	public static void main(String[] args) throws FtpException {
		String userDir = System.getProperty("user.dir");
		
		// 创建一个ftpServerFactory
		FtpServerFactory serverFactory = new FtpServerFactory();
		
		ListenerFactory factory = new ListenerFactory();
		// 设置监听端口
		factory.setPort(211);
		// SSL证书Configure
		SslConfigurationFactory ssl = new SslConfigurationFactory();
		// 指定keystore证书
		ssl.setKeystoreFile(new File(userDir + "/ssl/domain.keystore"));
		// keystore密码
		ssl.setKeystorePassword("2011180062");
		// keyAlias
		ssl.setKeyAlias("domain");
		
		// 设置SSL的配置
		factory.setSslConfiguration(ssl.createSslConfiguration());
		// 此工厂创建的侦听器是自动处于SSL模式还是客户端必须明确请求使用SSL
		// true: 此工厂创建的侦听器应自动处于SSL模式, 否则为false
		factory.setImplicitSsl(true);

		// 替换默认的监听器
		serverFactory.addListener("default", factory.createListener());

		//PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		//userManagerFactory.setFile(new File("d:/ftp/users.properties"));
		//serverFactory.setUserManager(userManagerFactory.createUserManager());
		
		// 用户
		BaseUser user = new BaseUser();
		// 用户名
		user.setName("admin");
		// 密码, 如果不设置密码就是匿名用户
		user.setPassword("2011180062");
		// 用户主目录
		user.setHomeDirectory(userDir);
		// 权限
		List<Authority> authorities = new ArrayList<Authority>();
		// 增加写权限
		authorities.add(new WritePermission());
		user.setAuthorities(authorities);
		// 增加该用户
		serverFactory.getUserManager().save(user);

		// 创建Server
		FtpServer server = serverFactory.createServer();
		
		// 启动ftp
		server.start();
	}
}
