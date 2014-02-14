package org.psikon.fpcache.cache

import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import groovy.util.GroovyTestCase

class DefaultCacheObjTest extends GroovyTestCase {
	
	final Logger logger = LoggerFactory.getLogger(DefaultCacheObjTest.class)

	void testObjectCreationByStream() {
		
		def cacheFolder = "./target/tmp/level1/level3"
		def cacheId=cacheFolder + "/obj.txt"
		def content1 = "hello"
		def content2 = "world"
		
		def is1 = new ByteArrayInputStream(content1.getBytes())
		def cacheObj1 = new DefaultCacheObj(cacheId)
		cacheObj1.setContent(is1)
		
		File file1 = new File(cacheId)
		assert file1.text == content1
		
		def is2 = new ByteArrayInputStream(content2.getBytes())
		def cacheObj2 = new DefaultCacheObj(cacheId)
		cacheObj2.setContent(is2)
		
		File file2 = new File(cacheId)
		assert file2.text == content2
	}
		
	void testObjectCreationByString() {
		def cacheFolder = "./target/tmp/level1/level2"
		def cacheId=cacheFolder + "/obj.txt"
		def content1="hello"
		def content2="world"
		
		def cacheObj1 = new DefaultCacheObj(cacheId)
		cacheObj1.setContent(content1)
		
		File file1 = new File(cacheId)

		assert file1.getText() == content1
		
		def cacheObj2 = new DefaultCacheObj(cacheId)
		cacheObj2.setContent(content2)
		
		File file2 = new File(cacheId)
		assert file2.getText() == content2
		
	}
}
