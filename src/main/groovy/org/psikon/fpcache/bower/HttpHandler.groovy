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
package org.psikon.fpcache.bower

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
 * Handler for Bower requests
 * 
 * This handles specifically requests to the Bower registry.
 * 
 * @author v0idnull
 * @see org.psikon.fpcache.bower_resolver.HttpHandler
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
		
		def path = request.pathInfo.substring(7)
		def packageName = path.split('/')[1]
		def registry = new Registry(this.config)
		
		logger.debug(request.getMethod() + ": " + request.getPathInfo())
		logger.debug("Bower path: " + path)
		
		def json = registry.getPackages(packageName)
		
		response.setContentType("application/json")
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(json)
		baseRequest.setHandled(true)
		
	}
	

}
