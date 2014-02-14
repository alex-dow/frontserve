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

import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Handler Class
 * 
 * Requests to the API are made by http://localhost/[handler_name].
 * Those handlers are configured to use a class that extends this
 * one.
 */
abstract class Handler extends AbstractHandler {
	
	/**
	 * Get all headers from an HttpServletRequest object as map
	 * 
	 * @param request
	 * @return Map list of headers
	 */
	protected getAllHeaders(HttpServletRequest request) {
		
		def allHeaders = [:]
		def headerNames = request.getHeaderNames();

		headerNames.each { headerName ->
			
			allHeaders[headerName] = ""
			
			request.getHeaders(headerName).each { headerValue ->
				allHeaders[headerName] = allHeaders[headerName] + headerValue + ", " 
			}
		}

		return allHeaders;
	}
}
