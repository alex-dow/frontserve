package org.psikon.fpcache.cache

/**
 * Cache Object interface
 */
interface ICacheObj {

	/**
	 * Set the ID of the cache obj
	 * 
	 * @param id
	 */
	public void setId(String id);
	
	/**
	 * Check to see if object is cached or not
	 * 
	 * @return
	 */
	public boolean isCached();
	
	/**
	 * Get File instance of the cache object
	 * 
	 * @return
	 */
	public File getCacheObj();
	
	/**
	 * Get content of cache object as a string
	 * 
	 * @return
	 */
	public String getContent();
	
	/**
	 * Get content of cache object as a stream
	 * 
	 * @return
	 */
	public InputStream getStream();
	
	/**
	 * Set the content using a stream
	 * 
	 * @param stream
	 */
	public void setContent(InputStream stream);
	
	/**
	 * Set the content using a string
	 * 
	 * @param content
	 */
	public void setContent(String content);
	
	/**
	 * Set the content using a byte array
	 * 
	 * @param content
	 */
	public void setContent(byte[] content);
}
