package app.simplestudio.com.mh;

import app.simplestudio.com.exception.SignException;
import java.nio.file.Path;

/**
 * Functional interface for signing XML documents using XAdES.
 * <p>
 * Implementations should take a KeyStore (PKCS#12), its password, an input XML path,
 * and an output XML path where the signed document will be written.
 */
@FunctionalInterface
public interface ISigner {

	/**
	 * Signs the XML document located at {@code xmlInputPath} using the provided
	 * keystore credentials, and writes the signed XML to {@code xmlOutputPath}.
	 *
	 * @param keyStorePath        path to the PKCS#12 keystore file
	 * @param keyStorePassword    password for the keystore and key entry
	 * @param xmlInputPath        path to the input XML to be signed
	 * @param xmlOutputPath       path where the signed XML will be written
	 * @throws SignException if any error occurs during parsing, signing, or writing
	 */
	void sign(Path keyStorePath,
			String keyStorePassword,
			Path xmlInputPath,
			Path xmlOutputPath) throws SignException;
}