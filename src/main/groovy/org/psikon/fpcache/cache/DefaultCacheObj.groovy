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
package org.psikon.fpcache.cache

import java.io.File;
import java.io.InputStream;

/**
 * Default cache object
 * 
 * A very basic cache object. The cache ID is the path to the file
 * used to store the cache data.
 */
class DefaultCacheObj implements ICacheObj {

	private String id
	
	public DefaultCacheObj(String id) {
		this.id = id
	}
	
	public void setId(String id) {
		this.id = id
		
	}

	public File getCacheObj() {
		return new File(this.id)
	}
	
	public boolean isCached() {
		return this.getCacheObj().exists()
	}
	
	public String getContent() {
	    InputStream is = this.getStream()
		
		def buf;
		
		byte[] buffer = new byte[1024];
		
		def content = ""
		
		while ((buf = is.read(buffer, 0, buffer.length)) > 0) {
			if (buf != buffer.length) {
				byte[] smallBuffer = new byte[buf]
				System.arraycopy(buffer, 0, smallBuffer, 0, buf)
				
				def bufstr = new String(smallBuffer)
				content = content + bufstr
			} else {
				def bufstr = new String(buffer)
				content = content + bufstr
			}
		}
		
		return content.trim()
	}

	public InputStream getStream() {
		return new FileInputStream(this.getCacheObj())
	}

	public void setContent(InputStream stream) {
		
		def buf;
		def buffer = new byte[1024]
		
		File cacheObj = this.getCacheObj()
		
		def cacheDir = new File(cacheObj.parent)
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs()
		}
		
		FileOutputStream fo = new FileOutputStream(cacheObj)
		
		while ((buf = stream.read(buffer, 0, buffer.length)) > 0) {
			if (buf != buffer.length) {
				byte[] smallBuffer = new byte[buf]
				System.arraycopy(buffer, 0, smallBuffer, 0, buf)
				fo.write(smallBuffer)
			} else {
				fo.write(buffer)
			}
		}
		
		fo.close()
	}

	public void setContent(String content) {

		this.setContent(new ByteArrayInputStream(content.getBytes()))
	}

	public void setContent(byte... content) {
		this.setContent(new ByteArrayInputStream(content))
		// TODO Auto-generated method stub
		
	}
}
