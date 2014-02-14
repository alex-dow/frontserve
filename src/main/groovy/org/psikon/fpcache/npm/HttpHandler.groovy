/*
The MIT License (MIT)

Copyright (c) 2014 Alex Dowgailenko

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package org.psikon.fpcache.npm

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.JSON
import groovy.json.*

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import net.sf.json.JSONObject;

import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.psikon.fpcache.Handler


/**
 * NPM Request handler
 */
class HttpHandler extends Handler {

	final Logger logger = LoggerFactory.getLogger(HttpHandler.class)
	
	private config

	public HttpHandler(config) {
		this.config = config
	}
	
	public void handle(String target, Request baseRequest, 
		HttpServletRequest request, 
		HttpServletResponse response)
	throws IOException, ServletException {
		/**
		 * NPM Registry URL Structure
		 * 
		 * For metadata:
		 *   /{project-name}/{version}
		 *   
		 * For tarball:
		 *   /{project-name}/-/{project-name}-{version}.tgz
		 *   
		 * Example:
		 *   /underscore/1.5.2
		 *   /underscore/-/underscore-1.5.2.tgz
		 */
		
		def path = request.pathInfo
		path = path.substring(4)
		
		logger.debug("NPM Request: " + path)
		
		if (path.startsWith("/")) {
			path = path.substring(1)
		}
		
		def pathParts = path.split("/")
		
		def npm = new Registry(this.config)
		
		def projectName
		def projectVersion
		
		if (pathParts.length == 1 || pathParts.length == 2) {
			
			logger.info("Metadata Request")
			
			projectName = pathParts[0]
			
			if (pathParts.length == 2) {
				projectVersion = pathParts[1]
				logger.debug("Request for: " + projectName + "@" + projectVersion)
			} else {
				logger.debug("Request for: " + projectName)
			}
			
			def json = ""
			
			if (projectVersion != null) {
				json = npm.getProjectJson(projectName, projectVersion)
			} else {
				json = npm.getProjectJson(projectName)
			}
			
			response.setContentType("application/json;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write(json)
			baseRequest.setHandled(true);
			
		} else if (pathParts.length == 3) {
		
			response.setContentType("application/octet-stream")
			response.setStatus(HttpServletResponse.SC_OK)
			/**
			 * Package filename looks like this:
			 * 
			 * project-name-1.2.4-1.tgz
			 */
			logger.info("Package request")
			def pkg_filename = pathParts[2]
			
			def matcher = pkg_filename =~ /\-([0-9]+\.[0-9]+\.[0-9]+\-*[a-zA-Z0-9\._]*)\.tgz/
			
			pkg_filename = pkg_filename.replace(matcher[0][0], "")
			def pkg_version = matcher[0][1]
			
			logger.debug("Request for: " + pkg_filename + "@" + pkg_version)
			
			File file = npm.getProjectPackage(pkg_filename, pkg_version)
			
			FileInputStream fs = new FileInputStream(file)
			OutputStream rs = response.getOutputStream()
			 
			int curByte = -1
			
			while ((curByte = fs.read()) != -1) {
				rs.write(curByte)
			}
			
			rs.flush()
			
			fs.close()
			rs.close()
			
			baseRequest.setHandled(true)
			
		} else {
			logger.warn("Request unknown: " + path)
		}

	}	
}