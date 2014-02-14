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

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.CloseableHttpClient
import org.psikon.fpcache.cache.MetaCacheObj
import org.psikon.fpcache.cache.PackageCacheObj
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
 * Interactions with the bower registry
 * 
 * Bower does not have a centralized repository. Instead it fetches
 * packages directly from git repos.
 * 
 * Since FrontServe wants to intercept those packages, we rename
 * the git urls in the registry to FrontServe urls with the
 * git url added as part of the path.
 * 
 * Example, if the git url for a package is:
 * 
 * git://github.com/jquery/jquery.git
 * 
 * Then the new repository url will be
 * 
 * http://localhost:8080/bower_resolver/git://github.com/jquery/jquery.git
 */
class Registry {

	final Logger logger = LoggerFactory.getLogger(Registry.class)
	
	private config
	
	HttpResponse previousResponse
	
	public Registry(config) {
		this.config = config
	}

	public String getPackages(packageName)
	{
		def cache = new MetaCacheObj(this.config.fpcache.bower.rootdir, packageName)
		
		def url = this.config.fpcache.bower.registry + "/packages/" + packageName
		
		if (cache.isCached()) {
			logger.info("cache hit for " + packageName)
			return cache.getContent()
		} else {
			HttpClient client = HttpClients.createDefault()
			logger.info("cache miss for " + packageName)
			logger.debug("Downloading json from " + url)
			HttpGet http = new HttpGet(url)
			http.setHeader("Accept", "application/json")
			HttpResponse response = client.execute(http)
			
			this.previousResponse = response
			
			
			def handler = new BasicResponseHandler()
			def json    = handler.handleResponse(response)
			
			def slurper = new JsonSlurper()
			def json_obj = slurper.parseText(json)

			print json_obj
			
			json_obj.url = "http://" + this.config.fpcache.server.ip + ":" + this.config.fpcache.server.port + "/bower_resolver/" + URI.encode(json_obj.url)

			def builder = new JsonBuilder(json_obj)
			
			json = builder.toPrettyString()
			
			cache.setContent(json)
			
			return json
		}
	}
}
