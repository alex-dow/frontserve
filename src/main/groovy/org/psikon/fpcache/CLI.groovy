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
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Main entry point
 */
class CLI {
	public static void main(String[] args) {
		
		def cli = new CliBuilder();
		cli.help(argName:'help', 'Display help')
		cli.config(args:1, argName:'config', 'Location of fpcache.properties')
		
		def options = cli.parse(args)
		
		if(options.help) {
			cli.usage()
			return
		}
		
		def prop = new Properties()
		if (options.config) {
			prop.load(new FileInputStream(options.config))
		} else {
			prop.load(new FileInputStream("fpcache.properties"))
		}
		
		def config = new ConfigSlurper().parse(prop)
		
		def addr = new InetSocketAddress(config.fpcache.server.port.toInteger()?:8080, config.fpcache.server.ip?:'127.0.0.1')
		
		Server server = new Server(addr)
        server.setHandler(new Router(config));
        server.start();
        server.join();
	}
}
