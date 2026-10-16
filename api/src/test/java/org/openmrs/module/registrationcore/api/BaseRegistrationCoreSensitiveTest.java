package org.openmrs.module.registrationcore.api;

import org.junit.Before;
import org.openmrs.module.idgen.IdgenModuleActivator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = { "classpath*:applicationContext-service.xml", "classpath*:moduleApplicationContext.xml",
        "classpath:registrationcore-test-applicationContext.xml" }, inheritLocations = false)
public abstract class BaseRegistrationCoreSensitiveTest extends BaseModuleContextSensitiveTest {
	
	/**
	 * Since idgen 5+ the identifier source processors are registered by the module activator, which is
	 * not run in tests.
	 */
	@Before
	public void startIdgen() {
		new IdgenModuleActivator().started();
	}
}
