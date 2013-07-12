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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class DataServlet extends ServletBase {

	private static final long serialVersionUID = 1L;
	private static final String htmlHead = "<!doctype html><html><body><h1>Files</h1><ul>";
	private static final String htmlTail = "</ul></body></html>";
	private static final String htmlItem = "<li><a href=\"%s\">%s</a></li>";
	private File fsDataDirFile;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		fsDataDirFile = new File(fsDataDir);
		if (!(fsDataDirFile.exists() && fsDataDirFile.isDirectory()))
			throw new ServletException("Filesystem directory does not exist: " + fsDataDirFile.getAbsolutePath());
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {

			//URL is of the form "/filelogger/data/a.txt" or "/filelogger/data/" or "filelogger/data"
			//We want to either deliver the contents of the file or provide a directory listing.
			String[] pathAfterServletMapping = req.getRequestURI().split(webDataDir + "/");

			if (pathAfterServletMapping.length <= 1) {
				Writer out = resp.getWriter();
				out.write(htmlHead);
				for(File f: fsDataDirFile.listFiles()) {
					if (f.isFile()) {
						final String filename = f.getName();
						out.write(String.format(htmlItem, webDataDir + "/" + filename, filename));
					}
				}
				out.write(htmlTail);
			} else {
				String filePath = URLDecoder.decode(pathAfterServletMapping[1], "UTF-8");
				if (filePath.contains(File.separator))
					throw new ServletException("Access to directories is prohibited: " + filePath);

				File f = new File(fsDataDirFile.getAbsolutePath() + File.separator + filePath);
				if (f.isFile() && f.exists() && f.canRead()) {
					IOUtils.copyLarge(new FileInputStream(f), resp.getOutputStream());
				} else {
					throw new ServletException("Cannot access file " + f.getAbsolutePath());
				}
			}

		} catch(IOException e) {
			throw new ServletException("Cannot write to client.", e);
		}
	}
}
