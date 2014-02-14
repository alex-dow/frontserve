package org.psikon.fpcache.npm

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import org.apache.http.HttpResponse
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.CloseableHttpClient
import org.psikon.fpcache.cache.ICacheObj;
import org.psikon.fpcache.cache.MetaCacheObj
import org.psikon.fpcache.cache.PackageCacheObj
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NPM Registry
 * 
 * Makes requestst to npm registry and caches them
 */
class Registry {

	private registry;
	private rootdir;
	
	private config;
	
	final Logger logger = LoggerFactory.getLogger(Registry.class)
	
	public Registry(config) {
		this.config = config
	}

	/**
	 * Get the project package
	 * 
	 * Returns the file of the tarball for the requested project
	 * 
	 * @param projectName
	 * @param projectVersion
	 * @return
	 */
	public File getProjectPackage(projectName, projectVersion) {

		File file
		def cacheObj = new PackageCacheObj(this.config.fpcache.npm.rootdir, projectName, projectVersion)
		
		if (cacheObj.isCached()) {		
			this.logger.debug("Cache hit for package " + projectName + "@" + projectVersion)
			file = cacheObj.getCacheObj()
		} else {
			this.logger.debug("Cache miss for package " + projectName + "@" + projectVersion)
			
			def url = this.config.fpcache.npm.registry + "/" + projectName + "/-/" + projectName + "-" + projectVersion + ".tgz"
			
			HttpClient client = HttpClients.createDefault()
			HttpGet http = new HttpGet(url)
			HttpResponse response = client.execute(http)
			
			logger.info("Downloading " + url)
			cacheObj.setContent(response.getEntity().getContent())
			file = cacheObj.getCacheObj()
			logger.debug("Download of " + url + " done.")
		}
		
		return file
	}
	
	/**
	 * Returns the project's metadata as json
	 * 
	 * @param projectName
	 * @param projectVersion
	 * @return
	 */
	public getProjectJson(String projectName, String projectVersion = null) {
		
		String json
		String url
		ICacheObj cacheObj
		
		if (projectVersion == null) {
			url = this.config.fpcache.npm.registry + '/' + projectName
			cacheObj = new MetaCacheObj(this.config.fpcache.npm.rootdir, projectName)
		} else {
			url = this.config.fpcache.npm.registry + '/' + projectName + '/' + projectVersion
			cacheObj = new MetaCacheObj(this.config.fpcache.npm.rootdir, projectName, projectVersion)
		}
		
		if (cacheObj.isCached()) {
			logger.debug("cache hit for meta data of " + projectName)
			json = cacheObj.getContent()
		} else {
			logger.debug("cache miss for meta data of " + projectName)
			CloseableHttpClient http_client = HttpClients.createDefault()
			this.logger.debug("Requesting " + url)
			HttpGet http = new HttpGet(url)
			CloseableHttpResponse response = http_client.execute(http)
			
			def handler = new BasicResponseHandler()
			json    = handler.handleResponse(response)
			
			def slurper = new JsonSlurper()
			def result = slurper.parseText(json)
			
			if (projectVersion != null)
			{
				result['dist']['tarball'] = result['dist']['tarball'].replace(this.config.fpcache.npm.registry, 'http://' + this.config.fpcache.server.ip + ':' + this.config.fpcache.server.port + '/npm')
			} else {
				result['versions'].each { k, v ->
					logger.debug("Changing tarbal location for " + projectName + "@" + k)
					v['dist']['tarball'] = v['dist']['tarball'].replace(this.config.fpcache.npm.registry, 'http://' + this.config.fpcache.server.ip + ':' + this.config.fpcache.server.port + '/npm')
					result['versions'][k] = v
				}
			}
			
			def builder = new JsonBuilder()
			builder.content = result
			json = builder.toString()
			cacheObj.setContent(json)
		}
		return json
	}
}
