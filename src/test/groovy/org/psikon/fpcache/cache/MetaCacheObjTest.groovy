package org.psikon.fpcache.cache;

import static org.junit.Assert.*;

import org.junit.Test;

class MetaCacheObjTest {

	private rootDir = 'target/test-tmp'
	private projectName = 'frontserve'
	private projectVersion = '1.0.0'
	
	@Test
	public void testContstructor() {
		
		MetaCacheObj obj1 = new MetaCacheObj(this.rootDir, this.projectName)
		MetaCacheObj obj2 = new MetaCacheObj(this.rootDir, this.projectName, this.projectVersion)
		
		File f1 = obj1.getCacheObj()
		File f2 = obj2.getCacheObj()
		
		assert f1.absolutePath == System.getProperty('user.dir') + '/' + this.rootDir + '/' + this.projectName + '/' + this.projectName + '.json'
		assert f2.absolutePath == System.getProperty('user.dir') + '/' + this.rootDir + '/' + this.projectName + '/' + this.projectVersion + '/' + this.projectName + '.json'
	}

}
