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
package org.psikon.fpcache

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Routes incoming requests to the proper handler
 *
 * Routing works by taking the first item in the URL's path, and
 * mapping it to the config object. Example:
 * 
 * http://localhost/npm
 * 
 * Will look for handler class in configuration
 * 
 * fpcache.handlers.npm
 */
class Router extends AbstractHandler {

	final Logger logger = LoggerFactory.getLogger(Router.class)
	
	private config
	
	public Router(config)
	{
		this.config = config
	}
	
	public void handle(String target, Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
			
		def pathParts = request.pathInfo.substring(1).split("/")
		logger.debug("Request for " + pathParts[0])
		
		def className = this.config['fpcache']['handlers'][pathParts[0]]
		
		logger.info("Routing request to " + className)
		
		Handler handler = Class.forName(className).newInstance(this.config)
		handler.handle(target, baseRequest, request, response)
	}
}
