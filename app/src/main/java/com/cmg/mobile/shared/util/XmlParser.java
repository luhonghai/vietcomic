/**
 * Copyright (c) CMG Ltd All rights reserved.
 *
 * This software is the confidential and proprietary information of CMG
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with CMG.
 */
package com.cmg.mobile.shared.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cmg.mobile.shared.data.Newsletter;
import com.cmg.mobile.shared.exception.NewsletterParserException;

/**
 * DOCME
 * 
 * @Creator LongNguyen
 * @author $Author$
 * @version $Revision$
 * @Last changed: $LastChangedDate$
 */
public class XmlParser {
	private final String xmlRes;
	private final String rootImageUrl;
	private final int categoryId;

	/**
	 * Constructor
	 * @param xmlRes
	 * @param categoryId
	 * @param rootImageUrl
	 */
	public XmlParser(String xmlRes, int categoryId, String rootImageUrl) {
		this.xmlRes = xmlRes;
		this.categoryId = categoryId;
		this.rootImageUrl = rootImageUrl;
	}

	/**
	 * get Value from XML
	 * @param item
	 * @param str
	 * @return
	 */
	private String getValue(Element item, String str) {
		NodeList n = item.getElementsByTagName(str);
		return this.getElementValue(n.item(0));
	}

	/**
	 * get TEXT node
	 * @param ele
	 * @return
	 */
	private final String getElementValue(Node ele) {
		Node child;
		if (ele != null) {
			if (ele.hasChildNodes()) {
				for (child = ele.getFirstChild(); child != null; child = child
						.getNextSibling()) {
					if (child.getNodeType() == Node.TEXT_NODE) {
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

	/**
	 * build XML document
	 * @param xml
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private Document getDomElement(String xml)
			throws ParserConfigurationException, SAXException, IOException {
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		/*InputSource input = new InputSource();
		input.setEncoding("UTF-8");
		input.setCharacterStream(new StringReader(xml));*/
		InputStream is = new ByteArrayInputStream(xml.getBytes("ISO-8859-1"));
		doc = builder.parse(is);
		return doc;
	}

	/**
	 * parse XML to list objects
	 * @return
	 * @throws NewsletterParserException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public List<Newsletter> parse() throws NewsletterParserException,
			ParserConfigurationException, SAXException, IOException {
		if (xmlRes == null || xmlRes.length() == 0) {
			throw new NewsletterParserException(
					NewsletterParserException.NO_XML_RESOURCE_FOUND);
		}		
		Document doc = getDomElement(xmlRes);
		List<Newsletter> newsletters = new ArrayList<Newsletter>();
		NodeList nodelist = doc.getElementsByTagName("Newsletter");
		for (int i = 0; i < nodelist.getLength(); i++) {
			Element e = (Element) nodelist.item(i);
			Newsletter letter = new Newsletter();
			letter.setId(e.getAttribute("id"));
			letter.setDate(this.getValue(e, "date"));
			letter.setFileUrl(this.getValue(e, "fileUrl"));
			String imageUrl = this.getValue(e, "imageUrl");
			if (!imageUrl.toLowerCase().contains("http")) {
				imageUrl = rootImageUrl + imageUrl;
			}
			letter.setImageUrl(imageUrl);
			String summary = this.getValue(e, "summary");
			//log.info("Summary : " + summary);
			//CharSequence styledSummary = Html.fromHtml(summary);
			letter.setSummary(summary);
			letter.setTitle(this.getValue(e, "title"));
			letter.setSize(Long.parseLong(this.getValue(e, "size")));
			letter.setCategoryId(categoryId);
			letter.setPage(Integer.parseInt(this.getValue(e, "page")));
			newsletters.add(letter);
		}
		return newsletters;
	}

}
