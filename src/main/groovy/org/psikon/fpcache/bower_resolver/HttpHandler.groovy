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
package org.psikon.fpcache.bower_resolver

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.eclipse.jgit.lib.RefDatabase.ALL;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.ReflogEntry
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.StoredConfig
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.psikon.fpcache.Handler;
import org.psikon.fpcache.cache.DefaultCacheObj;
import org.psikon.fpcache.cache.MetaCacheObj
import org.psikon.fpcache.cache.PackageCacheObj
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for git requests coming from bower.
 * 
 * Bower will make git requests using the git protocol. Those requests
 * will be made to the proper git url, and the responses cached by a generated
 * id, and returned verbatim back to bower.
 */
class HttpHandler extends Handler {

	final Logger logger = LoggerFactory.getLogger(HttpHandler.class)
	
	private config

	public HttpHandler(config) {
		this.config = config
	}
	
	class GitReq extends HttpRequestBase {

		protected git_method
		
		public GitReq(cmd) {
			
			this.git_method = cmd
		}
		public String getMethod() {
			// TODO Auto-generated method stub
			return this.git_method
		}
	
	}
	
	private getCacheId(requestMethod, requestUrl, requestBody)
	{
		def cache_string = requestMethod + requestUrl + requestBody
		
		MessageDigest md = MessageDigest.getInstance('SHA-256')
		
		def digestBytes = md.digest(cache_string.getBytes())
		def StringBuffer sb = new StringBuffer()
		
		for (int i = 0; i < digestBytes.length; i++)
		{
			sb.append(Integer.toString((digestBytes[i] & 0xff) + 0x100, 16).substring(1))
		}
		
		return sb.toString()

	}
	
	protected getGitUrl(String request_path) {
		this.logger.debug("rp: " + request_path)
		def pathParts = request_path.substring(16).split('git://')
		def git_http_url = 'https://' + pathParts[1]
		return git_http_url
	}
	
	public void handle(String target, Request baseRequest, 
		HttpServletRequest request, 
		HttpServletResponse response)
	throws IOException, ServletException {
		
		
		
		HttpClient git_client = HttpClients.createDefault()
		String git_method = new String(request.getMethod())
		String git_query = request.queryString
		String git_url = this.getGitUrl(request.pathInfo)
		
		if (git_query) { 
			git_url += '?' + git_query
		}
		
		HttpRequestBase git_request
		
		if (request.getMethod() == 'GET') {
			git_request = new HttpGet()
		} else if (request.getMethod() == 'POST') {
			git_request = new HttpPost()
		}
		
		logger.debug("--- git request")
		logger.debug('--method')
		logger.debug(request.getMethod() + ": " + request.pathInfo)
		logger.debug('--headers')
		
		this.getAllHeaders(request).each { k, v ->
			this.logger.debug(k + ": " + v)
			git_request.setHeader(k,v)
		}
		
		logger.debug('--query')
		logger.debug(git_query)
		
		String git_reqbody = ""
		
		if (request.getMethod() == 'POST') {
			
			StringWriter writer = new StringWriter()
			IOUtils.copy(request.getInputStream(), writer)
			git_reqbody = writer.toString()
			
			git_request.removeHeaders('Content-Length')
			git_request.setEntity(new StringEntity(git_reqbody))
			
			def refid = git_reqbody.split(" ")[1]
			logger.debug('refid: ' + refid)
		}
		
		logger.info("Getting refs from " + git_url)
		
		def cacheid = this.getCacheId(git_method, git_url, git_reqbody)
		
		logger.debug("cache id" + cacheid)
		
		def cacheObj = new MetaCacheObj('cache/bowergit', cacheid)
		
		if (cacheObj.isCached())
		{
			logger.debug("cache hit")
			
			response.setHeader('Content-Type', 'application/x-git-upload-pack-advertisement')
			response.setHeader('Transfer-Encoding','chunked')
			response.setHeader('Vary','Accept-Encoding')
			response.setHeader('Pragma','no-cache')
			response.setHeader('Expires','Fri, 01 Jan 1980 00:00:00 GMT')
			response.setHeader('Cache-Control','no-cache, max-age=0, must-revalidate')
			
			def is = cacheObj.getStream()
			
			IOUtils.copy(is, response.getOutputStream())
			
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true)
		} else {
			logger.debug("cache miss")
		
			git_request.setURI(new URI(git_url))
			HttpResponse git_response = git_client.execute(git_request)
			
			logger.debug("--- git response")
			logger.debug("-- headers")
			
			git_response.getAllHeaders().each { Header header ->
				logger.debug(header.name + ": " + header.value)
				response.setHeader(header.name, header.value) 
			}
			
			logger.debug('Fetching data')
			cacheObj.setContent(git_response.getEntity().getContent())
			IOUtils.copy(cacheObj.getStream(), response.getOutputStream())
			
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true)
		}
		
//		if (cmd != "info/refs") {
//			this.logger.info("Checking out repo?")
//		} else {
//		
//			// path = path.replace('git://','https://')
//			logger.info("GIT URL: " + path)
//			
//			File gitDest = new File('cache/bower/git');
//			gitDest.deleteDir()
//			
//			FileRepositoryBuilder builder = new FileRepositoryBuilder()
//			Repository repository = builder.setGitDir(gitDest).readEnvironment().findGitDir().build()
//			
//			StoredConfig config = repository.getConfig()
//			config.setString('remote', 'origin', 'url', path)
//			config.save()
//			
//			Git localGit = new Git(repository)
//			
//			this.logger.info("Listing remote repository " + path);
//			Collection<Ref> refs = Git.lsRemoteRepository()
//					.setHeads(true)
//					.setTags(true)
//					.setRemote(path.replace('git://','https://'))
//					.call();
//	
//			for (Ref ref : refs) {
//				response.getWriter().write(ref.objectId.name + "\t" + ref.name + "\n")
//				// this.logger.info(ref.objectId.name + "\t" + ref.name);
//			}
//		}

//		

//		
//		StoredConfig config = repository.getConfig()
//		config.setString('remote', 'origin', 'url', path)
//		
//		this.logger.info("Updating repo")
//		repository.updateRef(ALL)
//		
//		def localGit = new Git(repository)
//		def fetchCmd = localGit.fetch()
//		fetchCmd.setRemote('origin')
//		fetchCmd.call()
//
//		
//		repository.close()
//		
//		Map<String, Ref> allRefs = git.getRepository().getAllRefs();
//		Collection<Ref> values = allRefs.values();
//		for (Ref ref : values) {
//		  refs.add(ref.getName());
//		}
//		
	}
	

}
