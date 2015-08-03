package uk.co.creative74.springmvchibernate.controller;

import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.timeleaf.spring.support.Layout;

import uk.co.creative74.springmvchibernate.model.Employee;
import uk.co.creative74.springmvchibernate.service.EmployeeService;


@Controller
public class AppController {

	@Autowired
	EmployeeService service;
	
	@Autowired
	MessageSource messageSource;
	
	static final Logger appLog = LoggerFactory.getLogger("application-log");

	/*
	 * This method will list all existing employees.
	 */
	@RequestMapping(value = { "/admin/list" }, method = RequestMethod.GET)
	public String listEmployees(ModelMap model) {
		
		appLog.debug("GET /list");

		List<Employee> employees = service.findAllEmployees();
		model.addAttribute("employees", employees);
		return "demo/allemployees";
	}

	/*
	 * This method will provide the medium to add a new employee.
	 */
	@RequestMapping(value = { "/admin/new" }, method = RequestMethod.GET)
	public String newEmployee(ModelMap model) {
		
		Employee employee = new Employee();
		model.addAttribute("employee", employee);
		model.addAttribute("create", true);
		return "demo/registration";
		
	}

	/*
	 * This method will be called on form submission, handling POST request for
	 * saving employee in database. It also validates the user input
	 */
	@RequestMapping(value = { "/admin/createOrUpdateEmployee" }, method = RequestMethod.POST)
	public String saveEmployee(	@Valid Employee employee,
								BindingResult result,
								ModelMap model) {

		appLog.debug("POST /createOrUpdateEmployee");
		
		if (result.hasErrors()) {
			model.addAttribute("create", true);
			return "demo/registration";
		}
		
		if (employee.getId() < 1) {

			/*
			 * Preferred way to achieve uniqueness of field [ssn] should be implementing custom @Unique annotation 
			 * and applying it on field [ssn] of Model class [Employee].
			 * 
			 * Below mentioned peace of code [if block] is to demonstrate that you can fill custom errors outside the validation
			 * framework as well while still using internationalized messages.
			 */
			if(!service.isEmployeeSsnUnique(employee.getId(), employee.getSsn())){
				FieldError ssnError =new FieldError("employee","ssn",messageSource.getMessage("non.unique.ssn", new String[]{employee.getSsn()}, Locale.getDefault()));
			    result.addError(ssnError);
			    model.addAttribute("create", true);
				return "demo/registration";
			}
			
			service.saveEmployee(employee);		
			
			model.addAttribute("success", "Employee " + employee.getName() + " registered successfully");

		} else {

			if(!service.isEmployeeSsnUnique(employee.getId(), employee.getSsn())){
				FieldError ssnError =new FieldError("employee","ssn",messageSource.getMessage("non.unique.ssn", new String[]{employee.getSsn()}, Locale.getDefault()));
			    result.addError(ssnError);
				return "demo/registration";
			}

			service.updateEmployee(employee);

			model.addAttribute("success", "Employee " + employee.getName()	+ " updated successfully");
			
		}
		
		return "demo/success";
	}


	/*
	 * This method will provide the medium to update an existing employee.
	 */
	@RequestMapping(value = { "/admin/edit-{ssn}-employee" }, method = RequestMethod.GET)
	public String editEmployee(@PathVariable String ssn, ModelMap model) {
		
		appLog.debug("GET /edit-{ssn}-employee");
		
		Employee employee = service.findEmployeeBySsn(ssn);
		//Employee employee = new Employee();
		model.addAttribute("employee", employee);
		model.addAttribute("create", false);
		return "demo/registration";
	}
	

	/*
	 * This method will delete an employee by it's SSN value.
	 */
	@RequestMapping(value = { "/admin/delete-{ssn}-employee" }, method = RequestMethod.GET)
	public String deleteEmployee(@PathVariable String ssn) {
		service.deleteEmployeeBySsn(ssn);
		return "redirect:/admin/list";
	}

}
