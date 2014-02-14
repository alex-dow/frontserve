package org.psikon.fpcache.cache;

import static org.junit.Assert.*;

import org.junit.Test;

class PackageCacheObjTest {

	private rootDir = 'target/test-tmp'
	private projectName = 'frontserve'
	private projectVersion = '1.0.0'
	
	@Test
	public void testConstructor() {
		PackageCacheObj obj1 = new PackageCacheObj(this.rootDir, this.projectName, this.projectVersion)
		
		File f1 = obj1.getCacheObj()
		
		assert f1.absolutePath == System.getProperty('user.dir') + '/' + this.rootDir + '/' + this.projectName + '/' + this.projectVersion + '/' + this.projectName + '.tgz'
	}

}
