/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.mobile.shared.util;

import com.cmg.android.util.SimpleAppLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;

/**
 * DOCME
 * 
 * @Creator LongNguyen
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */
public final class FileHelper {
	public static final int BYTE_LENGTH = 4096;

	public static final int CONNECTION_TIMEOUT = 10000;

	/**
	 * Constructor
	 */
	private FileHelper() {

	}

	/**
	 * download file XML and synchronize
	 * 
	 * @param inputUrl
	 * @return
	 * @throws Exception
	 */
	public static String readURLContent(String inputUrl) throws Exception {
		InputStream is = null;
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		try {
			URL url = new URL(inputUrl);
			// URLConnection conn = url.openConnection();

			HttpURLConnection conn = null;
			if (url.getProtocol().toLowerCase().equals("https")) {
				trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection) url
						.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				conn = https;
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
			conn.setConnectTimeout(CONNECTION_TIMEOUT);
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
			conn.connect();
			is = conn.getInputStream();
			byte[] b = new byte[BYTE_LENGTH];
			int read;
			while ((read = is.read(b)) != -1) {
				bs.write(b, 0, read);
			}
			String xml = new String(bs.toByteArray());
			return xml;
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ex) {
					// Silent
				}
			}
			if (bs != null) {
				try {
					bs.close();
				} catch (Exception ex) {
					// Silent
				}
			}
		}
	}

	/**
	 * download and read file XML
	 * 
	 * @param inputUrl
	 * @param fos
	 * @param isDownload
	 * @throws Exception
	 */
	public static void downloadFile(String inputUrl, FileOutputStream fos,
			boolean isDownload) throws Exception {
		BufferedReader bis = null;
		BufferedWriter bw = null;
		try {
			if (isDownload) {
				URL url = new URL(inputUrl);
				// URLConnection conn = url.openConnection();

				HttpURLConnection conn = null;
				if (url.getProtocol().toLowerCase().equals("https")) {
					trustAllHosts();
					HttpsURLConnection https = (HttpsURLConnection) url
							.openConnection();
					https.setHostnameVerifier(DO_NOT_VERIFY);
					conn = https;
				} else {
					conn = (HttpURLConnection) url.openConnection();
				}
				conn.setConnectTimeout(CONNECTION_TIMEOUT);
				conn.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
				conn.connect();
				bis = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "ISO-8859-1"));
				String line;
				bw = new BufferedWriter(new OutputStreamWriter(fos,
						"ISO-8859-1"));
				while ((line = bis.readLine()) != null) {
					// fos.write(line.getBytes("ISO-8859-1"));
					bw.write(line);
					bw.newLine();
					bw.flush();
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
				if (bis != null) {
					bis.close();
				}
			} catch (Exception ex) {
				// Silent
			}
			if (bw != null) {
				try {
					bw.close();
				} catch (Exception ex) {

				}
			}
		}
	}

	/**
	 * download File
	 * 
	 * @param inputUrl
	 * @param fileName
	 * @param folderName
	 * @param isDownload
	 * @throws Exception
	 */
	public static void downloadFile(String inputUrl, String fileName,
			String folderName, boolean isDownload) throws Exception {
		try {
			folderName = folderName.endsWith(File.separator) ? folderName
					: (folderName + File.separator);
			File folder = new File(folderName);
			if (!folder.exists()) {
				folder.mkdirs();
			}
			String filePath = folderName + fileName;
			File file = new File(filePath);
			if (isDownload || !file.exists()) {
				downloadFile(inputUrl, new FileOutputStream(file), isDownload);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * read file XML
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static String readFileContent(FileInputStream is)
			throws IOException, FileNotFoundException {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"ISO-8859-1"));

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			// SimpleAppLog.info("XML String : " + sb.toString());
			return sb.toString();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception ex) {
					// Silent
				}
			}
		}
	}

	/**
	 * read file XML
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static String readFileContent(String file) throws IOException,
			FileNotFoundException {
		return readFileContent(new FileInputStream(file));
	}

	// always verify the host - dont check for certificate
	public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - dont check for any certificate
	 */
	public static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveFileToTxt(String folder, String pathFile,
			InputStream stream) {
		OutputStream outputStream = null;
		try {
			SimpleAppLog.info("coming to save file txt");
			File fod = new File(folder);
			if (!fod.exists()) {
				fod.mkdir();
			}
			outputStream = new FileOutputStream(new File(folder + pathFile
					+ ".txt"));

			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = stream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
		} catch (Exception e) {
			SimpleAppLog.error("error save file temp : " + e.getMessage(), e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					SimpleAppLog.error("IOEXCEPTION : " + e.getMessage(), e);
				}
			}
			if (outputStream != null) {
				try {
					// outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					SimpleAppLog.error("IOEXCEPTION : " + e.getMessage(), e);
				}

			}
		}

	}

	public static void saveFileToImage(String folder, String pathFile,
			InputStream stream) {
		byte[] imagebytes;
		try {
			SimpleAppLog.info("coming to save file image");
			File fod = new File(folder);
			if (!fod.exists()) {
				fod.mkdirs();
			}
			SimpleAppLog.info("creat folder succsess");
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			fos = new FileOutputStream(new File(folder + File.separator + pathFile));
			bos = new BufferedOutputStream(fos);
			byte[] aByte = new byte[1024];
			int bytesRead;

			while ((bytesRead = stream.read(aByte)) != -1) {
				bos.write(aByte, 0, bytesRead);
			}
			bos.flush();
			bos.close();
			/*
			 * // Process the input stream ByteArrayOutputStream out = new
			 * ByteArrayOutputStream(); byte[] buf = new byte[8192]; int len =
			 * 0; while(-1 != (len=stream.read(buf))) { out.write(buf, 0, len);
			 * } ByteArrayOutputStream outs = new ByteArrayOutputStream();
			 * byte[] bufe = new byte[8192]; int lens = 0; while(-1 !=
			 * (lens=stream.read(bufe))) { outs.write(bufe, 0, lens); }
			 * imagebytes = out.toByteArray(); InputStream in = new
			 * ByteArrayInputStream(imagebytes); BufferedImage bImageFromConvert
			 * = ImageIO.read(in); ImageIO.write(bImageFromConvert, "png", new
			 * File(folder+File.separator+pathFile));
			 */
		} catch (Exception e) {
			SimpleAppLog.error("error save file temp : " + e.getMessage(), e);
		}

	}

	public static void moveFile(File fileMove, String destFolder,
			String FileName) {
		try {
			SimpleAppLog.info("move file begin");
			File fod = new File(destFolder);
			if (!fod.exists()) {
				fod.mkdirs();
			}
			FileUtils.moveFile(fileMove, new File(destFolder + File.separator
					+ FileName));
		} catch (Exception e) {
			SimpleAppLog.error("error when move file image : " + e.getMessage(), e);
		}
	}



}
