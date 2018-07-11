package org.openmrs.module.registrationcore.api;

import org.junit.Ignore;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@Ignore
@ContextConfiguration(locations={"classpath*:applicationContext-service.xml","classpath*:moduleApplicationContext.xml", "classpath:registrationcore-test-applicationContext.xml"}, inheritLocations=false)
public class RegistrationCoreSensitiveTestBase extends BaseModuleContextSensitiveTest {
}
