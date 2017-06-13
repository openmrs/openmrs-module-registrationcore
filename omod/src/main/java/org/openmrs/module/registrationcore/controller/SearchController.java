package org.openmrs.module.registrationcore.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.registrationcore.api.RegistrationCoreService;
import org.openmrs.module.registrationcore.api.impl.RegistrationCoreProperties;
import org.openmrs.module.registrationcore.model.PatientSearchModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/module/registrationcore/search")
@SessionAttributes("patientSearch")
public class SearchController {

    protected final Log log = LogFactory.getLog(this.getClass());

    private RegistrationCoreProperties registrationCoreProperties;

    @RequestMapping(method = RequestMethod.GET)
    public void index(ModelMap model) {
        if(model.get("patientSearch") == null)
            model.put("patientSearch", new PatientSearchModel());
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView searchPatient(ModelMap model, @ModelAttribute("patientSearch") PatientSearchModel search) {

         RegistrationCoreService service = Context.getService(RegistrationCoreService.class);

        service.searchPatientsByPDQ(search.getFamilyName(),search.getGivenName());

        return new ModelAndView("/module/registrationcore/search", model);
    }

}
