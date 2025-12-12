package org.ssssssss.magicapi.tcp.util.server.common;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

public class CertificateRevoke {
  public static X509CRL loadX509CRL() throws Exception {
    String crltext = "";
    if (crltext == "")
      return null; 
    InputStream in = new ByteArrayInputStream(crltext.getBytes());
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    X509CRL crl = (X509CRL)cf.generateCRL(in);
    in.close();
    return crl;
  }
  
  public static boolean isRevoked(Certificate certFile) throws Exception {
    X509CRL crl = loadX509CRL();
    if (crl != null)
      return crl.isRevoked(certFile); 
    return false;
  }
  
  public static String getCommonName(Certificate cert) throws Exception {
    if (isRevoked(cert))
      return ""; 
    X509Certificate certificate = (X509Certificate)cert;
    Principal dn = certificate.getSubjectDN();
    String[] arr = dn.getName().split(",");
    String commonName = arr[1].trim().substring(3);
    return commonName;
  }
}
