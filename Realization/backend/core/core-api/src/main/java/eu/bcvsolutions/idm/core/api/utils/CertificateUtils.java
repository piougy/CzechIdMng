package eu.bcvsolutions.idm.core.api.utils;

import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Collections;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.springframework.util.Assert;

/**
 * Utilities class for certificates
 *
 * @since 10.8.0
 * @author Vít Švanda
 *
 */
public abstract class CertificateUtils {

	/**
	 * Parse uploaded certificate
	 *
	 * @param key
	 * @return
	 * @throws CertificateException
	 */
	public static X509Certificate getCertificate509(InputStream key) throws CertificateException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate cert = cf.generateCertificate(key);
		return (X509Certificate) cert;
	}

	/**
	 * Check if is given certificate signed with the given certificate authority.
	 * Does not supports check via certificate chain. Does not supports CRL check.
	 *
	 * @param certificate
	 * @param authority
	 * @throws CertificateException
	 */
	public static void verifyCertificate(X509Certificate certificate, X509Certificate authority)
			throws CertificateException {

		Assert.notNull(certificate, "Certificate cannot be null!");
		Assert.notNull(authority, "Authority cannot be null!");
		// Check if certificate send is your CA's
		if (!authority.equals(certificate)) {
			// verifyCertificate(certificate, Sets.newHashSet(authority));
			try { // Not your CA's. Check if it has been signed by your CA
				CertificateFactory cf = CertificateFactory.getInstance("X.509");

				CertPath cp = cf.generateCertPath(Lists.newArrayList(certificate));
				TrustAnchor anchor = new TrustAnchor((X509Certificate) authority, null);
				PKIXParameters params = new PKIXParameters(Collections.singleton(anchor));
				params.setRevocationEnabled(false);
				CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
				cpv.validate(cp, params);
			} catch (CertPathValidatorException ex) {
				if(ex.getCause() instanceof CertificateExpiredException) {
					throw (CertificateExpiredException)ex.getCause();
				}
				throw new CertificateException("Certificate not trusted", ex);
			} catch (Exception ex) {
				throw new CertificateException("Certificate not trusted", ex);
			}
		}
	}

	/**
	 * Checks whether given X.509 certificate is self-signed.
	 */
	public static boolean isSelfSigned(X509Certificate cert)
			throws CertificateException, NoSuchAlgorithmException, NoSuchProviderException {
		try {
			// Try to verify certificate signature with its own public key
			PublicKey key = cert.getPublicKey();
			cert.verify(key);
			return true;
		} catch (SignatureException sigEx) {
			// Invalid signature --> not self-signed
			return false;
		} catch (InvalidKeyException keyEx) {
			// Invalid key --> not self-signed
			return false;
		}
	}

	/**
	 * Convert certificate to PEM
	 *
	 * @param certificate
	 * @return
	 * @throws CertificateEncodingException
	 */
	public static InputStream certificateToPem(Certificate certificate) throws CertificateEncodingException {
		Assert.notNull(certificate, "Certificate cannot be null!");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (Writer osWriter = new OutputStreamWriter(baos); JcaPEMWriter pemWriter = new JcaPEMWriter(osWriter);) {
			pemWriter.writeObject(certificate);
		} catch (IOException e) {
			throw new CertificateEncodingException(e);
		}

		return new ByteArrayInputStream(baos.toByteArray());
	}

}
