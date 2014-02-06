/*
 * Copyright (c) 2013 Alastair R. Beresford
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.cam.arb33.filelogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class UploadServlet extends ServletBase {

	private static final long serialVersionUID = 1L;

	private final String htmlUploadForm = 
			"<!doctype html><html><body>" +
					"<h1>Upload file</h1>" +
					"%s" +
					"<p>View <a href=\"%s\">all previous uploads</a>.</p>" +
					"<form action=\"upload\" method=\"post\" enctype=\"multipart/form-data\">" +
					"<table><tr><td>Token: </td><td><input type=\"text\" name=\"token\" /></td></tr>" +
					"<tr><td>File: </td><td><input type=\"file\" name=\"file\" /></td></tr>" +
					"<tr><td></td><td><input type=\"submit\" name=\"submit\"/></td></tr></table>" +
					"</form></body></html>";
	private final String htmlCompleteMsg = 
			"<p>Uploaded: <a href=\"%s/%s\">%s</a></p>";

	private String uploadToken;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		//In Tomcat, configure by writing the following into conf/context.xml:
		//<Context>
		//<Parameter name="uk.ac.cam.arb33.filelogger.upload.token" value="XXXXXXX"/>
		//</Context>
		uploadToken = getServletContext().getInitParameter("uk.ac.cam.arb33.filelogger.upload.token");
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
			String complete = req.getParameter("complete");
			if (complete == null)
				complete = "";
			else
				complete = String.format(htmlCompleteMsg, webDataDir, complete, complete);
			resp.getWriter().write(String.format(htmlUploadForm, complete, webDataDir));
		} catch (IOException e) {
			throw new ServletException("Cannot write data to client.", e);
		}
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		
		try {
			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);

			String authToken = null;
			for (FileItem item : items) {
				if (item.isFormField()) {
					if (item.getFieldName().equals("token")) {
						authToken = item.getString();
						break;
					}
				} 
			}
			if (authToken == null || !authToken.equals(uploadToken)) {
				throw new ServletException("Invalid authentication token");
			}
			
			for(FileItem item: items) {
				if (!item.isFormField()){ //<input type="file" />
					final String filename = FilenameUtils.getName(item.getName());
					if (filename.contains(File.separator)) {
						throw new ServletException("Filename cannot contain file separator: " + File.separator);
					}
					final File outputPath = new File(fsDataDir + File.separator + filename);
					final InputStream fileInput = item.getInputStream();
					final OutputStream fileOutput = new FileOutputStream(outputPath);
					IOUtils.copyLarge(fileInput, fileOutput); //IOUtils has internal buffer; no need for external one.
					fileOutput.flush();
					fileOutput.close();

					//redirect to a GET request to reload the form and accept a new upload
					String encodedFilename = URLEncoder.encode(filename, "UTF-8");
					resp.sendRedirect(webUploadDir + "?complete=" + encodedFilename);
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException("Cannot parse multipart request.", e);
		} catch (IOException e) {
			throw new ServletException("Cannot open file content stream.", e);
		}

	}
}
