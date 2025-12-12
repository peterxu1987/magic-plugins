package org.ssssssss.magicapi.tcp.util.server.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

public class SSLUtil {
	
    public static X509CRL loadCRL(InputStream fis) throws Exception {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509CRL) cf.generateCRL(fis);
    }
    
    public static X509CRL loadPem() throws Exception {
    	 InputStream inputStream = SSLUtil.class.getClassLoader().getResourceAsStream("cert/crl.pem");
    	return loadCRL(inputStream);
    }

    public static boolean isCertificateRevoked(X509Certificate cert, X509CRL crl) {
        // 检查证书是否被撤销
        return crl.isRevoked(cert);
    }
    
    public static File getResourceFile(String fileName) {
        // 获取资源的输入流
        InputStream inputStream = SSLUtil.class.getClassLoader().getResourceAsStream("cert/" + fileName);

        if (inputStream == null) {
            System.out.println("文件未找到！");
            return null;
        }

        try {
            // 创建临时文件
            Path tempFile = Files.createTempFile("tempFile", fileName);
            tempFile.toFile().deleteOnExit(); // 在程序退出时删除临时文件

            // 将输入流内容写入临时文件
            try (FileOutputStream outputStream = new FileOutputStream(tempFile.toFile())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

            return tempFile.toFile(); // 返回文件对象
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
