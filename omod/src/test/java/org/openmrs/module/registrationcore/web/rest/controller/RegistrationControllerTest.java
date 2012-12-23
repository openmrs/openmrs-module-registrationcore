package org.openmrs.module.registrationcore.web.rest.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.RegistrationCoreConstants;
import org.openmrs.module.registrationcore.web.rest.v1_0.controller.RegistrationController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseCrudControllerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class RegistrationControllerTest extends BaseCrudControllerTest {
	
	@Autowired
	@Qualifier("adminService")
	private AdministrationService adminService;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService patientService;
	
	@Before
	public void before() throws Exception {
		executeDataSet("org/openmrs/module/idgen/include/TestData.xml");
		adminService.saveGlobalProperty(new GlobalProperty(RegistrationCoreConstants.GP_IDENTIFIER_SOURCE_ID, "1"));
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Override
	public String getURI() {
		return null;
	}
	
	@Override
	public String getUuid() {
		return null;
	}
	
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
	}
	
	@Override
	public void shouldGetRefByUuid() throws Exception {
	}
	
	@Override
	public void shouldGetFullByUuid() throws Exception {
	}
	
	@Override
	public void shouldGetAll() throws Exception {
	}
	
	/**
	 * @see RegistrationController#create(SimpleObject, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 * @verifies register a new patient
	 */
	@Test
	public void registerPatient_shouldRegisterANewPatient() throws Exception {
		int before = Context.getPatientService().getAllPatients().size();
		String json = "{\"person\":\"ba1b19c2-3ed6-4f63-b8c0-f762dc8d7562\", \"identifierLocation\":\"9356400c-a5a2-4532-8f2b-2361b3446eb8\" }";
		SimpleObject post = new ObjectMapper().readValue(json, SimpleObject.class);
		Object result = new RegistrationController().create(post, new MockHttpServletRequest(),
		    new MockHttpServletResponse());
		Util.log("Created patient", result);
		assertEquals(before + 1, Context.getPatientService().getAllPatients().size());
		String patientUuid = PropertyUtils.getProperty(result, "uuid").toString();
		Patient createdPatient = patientService.getPatientByUuid(patientUuid);
		assertNotNull(createdPatient);
	}
}
