/*
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 */
package webServer.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * HttpDigest is a miniature version of Digest algorithm which does not support
 * qop tag, and some other options, but it works for simple case. The
 * implementation is borrowed from followed website:
 * 
 * http://hc.apache.org/httpclient-3.x/xref/org/apache/commons/httpclient/util/
 * EncodingUtil.html
 * 
 * http://bethecoder.com/open-source/commons-httpclient/commons-httpclient-3.1/
 * org/apache/commons/httpclient/ util/EncodingUtil.java.html
 * 
 * Both references are licensed under Apache Software Foundation license
 * agreement.
 * </p>
 */
public class HttpDigest {

	/**
	 * Hexa values used when creating 32 character long digest in HTTP
	 * DigestScheme in case of authentication.
	 * 
	 * @see #encode(byte[])
	 */
	private static final char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
			'c', 'd', 'e', 'f' };

	/**
	 * Creates an MD5 response digest.
	 * 
	 * @param uname
	 *            Username
	 * @param pwd
	 *            Password
	 * 
	 * @return The created digest as string. This will be the response tag's
	 *         value in the Authentication HTTP header.
	 * @throws NoSuchAlgorithmException
	 */
	public static String createDigest( final String uname, final String pwd, final String uri,
			final String realm, final String nonce, final String method, String algorithm )
			throws NoSuchAlgorithmException {

		final String digAlg = "MD5";
		if ( algorithm == null ) {
			algorithm = "MD5";
		}
		// If an charset is not specified, default to ISO-8859-1.
		String charset = "ISO-8859-1";

		MessageDigest md5Helper;

		md5Helper = MessageDigest.getInstance( digAlg );

		// 3.2.2.2: Calculating digest
		StringBuffer tmp = new StringBuffer( uname.length() + realm.length() + pwd.length() + 2 );
		tmp.append( uname );
		tmp.append( ':' );
		tmp.append( realm );
		tmp.append( ':' );
		tmp.append( pwd );
		// unq(username-value) ":" unq(realm-value) ":" passwd
		String a1 = tmp.toString();
		String md5a1 = encode( md5Helper.digest( getBytes( a1, charset ) ) );

		String a2 = method + ":" + uri;
		String md5a2 = encode( md5Helper.digest( getAsciiBytes( a2 ) ) );

		// 3.2.2.1
		String serverDigestValue;
		// null qop
		StringBuffer tmp2 = new StringBuffer( md5a1.length() + nonce.length() + md5a2.length() );
		tmp2.append( md5a1 );
		tmp2.append( ':' );
		tmp2.append( nonce );
		tmp2.append( ':' );
		tmp2.append( md5a2 );
		serverDigestValue = tmp2.toString();

		String serverDigest = encode( md5Helper.digest( getAsciiBytes( serverDigestValue ) ) );

		return serverDigest;
	}

	/**
	 * Encodes the 128 bit (16 bytes) MD5 digest into a 32 characters long
	 * <CODE>String</CODE> according to RFC 2617.
	 * 
	 * @param binaryData
	 *            array containing the digest
	 * @return encoded MD5, or <CODE>null</CODE> if encoding failed
	 */
	private static String encode( byte[] binaryData ) {

		if ( binaryData.length != 16 ) {
			return null;
		}

		char[] buffer = new char[32];
		for ( int i = 0; i < 16; i++ ) {
			int low = ( int ) ( binaryData[i] & 0x0f );
			int high = ( int ) ( ( binaryData[i] & 0xf0 ) >> 4 );
			buffer[i * 2] = HEXADECIMAL[high];
			buffer[( i * 2 ) + 1] = HEXADECIMAL[low];
		}

		return new String( buffer );
	}

	/***
	 * Converts the specified string to byte array of ASCII characters.
	 * 
	 * @param data
	 *            the string to be encoded
	 * @return The string as a byte array.
	 * 
	 * @since 3.0
	 */
	public static byte[] getAsciiBytes( final String data ) {

		if ( data == null ) {
			throw new IllegalArgumentException( "Parameter may not be null" );
		}

		try {
			return data.getBytes( "US-ASCII" );
		} catch ( Exception e ) {
			return null;
		}

	}

	/***
	 * Converts the specified string to a byte array. If the charset is not
	 * supported the default system charset is used.
	 * 
	 * @param data
	 *            the string to be encoded
	 * @param charset
	 *            the desired character encoding
	 * @return The resulting byte array.
	 * 
	 * @since 3.0
	 */
	public static byte[] getBytes( final String data, String charset ) {

		if ( data == null ) {
			throw new IllegalArgumentException( "data may not be null" );
		}

		if ( charset == null || charset.length() == 0 ) {
			throw new IllegalArgumentException( "charset may not be null or empty" );
		}

		try {
			return data.getBytes( charset );
		} catch ( Exception e ) {
			return data.getBytes();
		}
	}

}
