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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class ServletBase extends HttpServlet {

	private static final long serialVersionUID = 1L;

	String fsDataDir; //Directory used to store data in the file system
	String webDataDir; //URL to list or download files
	String webUploadDir; //URL to upload files

	String resolveInitParameter(ServletConfig c, String param) throws ServletException {
		String result = c.getInitParameter(param);
		if (result == null)
			throw new ServletException("Cannot find '" + param + "' in web.xml for this servlet.");
		return result;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		//In Tomcat, configure by writing the following into conf/context.xml:
		//<Context>
		//<Parameter name="uk.ac.cam.arb33.filelogger.fs.dir" value="/tmp/data/"/>
		//</Context>
		fsDataDir = getServletContext().getInitParameter("uk.ac.cam.arb33.filelogger.fs.dir");
		if (fsDataDir == null)
			throw new ServletException("Cannot find 'uk.ac.cam.arb33.filelogger.fs.dir' as a parameter to conf/context.xml");

		webDataDir = getServletContext().getContextPath() + resolveInitParameter(config, "web.data.directory");
		webUploadDir = getServletContext().getContextPath() + resolveInitParameter(config, "web.upload.directory");
	}
}
